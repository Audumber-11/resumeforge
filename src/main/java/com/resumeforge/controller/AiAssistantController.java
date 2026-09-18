package com.resumeforge.controller;

import com.resumeforge.ai.AiManager;
import com.resumeforge.entity.*;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.ChatService;
import com.resumeforge.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequestMapping("/ai-assistant")
public class AiAssistantController {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantController.class);
    private final AiManager aiManager;
    private final ResumeService resumeService;
    private final ChatService chatService;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public AiAssistantController(AiManager aiManager, ResumeService resumeService,
                                  ChatService chatService) {
        this.aiManager = aiManager;
        this.resumeService = resumeService;
        this.chatService = chatService;
    }

    @GetMapping
    public String assistantPage(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("user", userDetails.getUser());
        model.addAttribute("resumes", resumeService.getUserResumes(userDetails.getUser().getId()));
        model.addAttribute("conversations", chatService.getUserConversations(userDetails.getUser().getId()));
        return "ai-assistant";
    }

    // ── Conversation Management ──

    @PostMapping("/conversation/create")
    @ResponseBody
    public Map<String, Object> createConversation(@AuthenticationPrincipal CustomUserDetails userDetails) {
        AiConversation conv = chatService.createConversation(
            userDetails.getUser().getId(),
            userDetails.getUser(),
            "New Conversation"
        );
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversationId", conv.getId());
        result.put("title", conv.getTitle());
        return result;
    }

    @GetMapping("/conversation/{id}/messages")
    @ResponseBody
    public Map<String, Object> getMessages(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        AiConversation conv = chatService.getConversation(id);
        Map<String, Object> result = new HashMap<>();
        if (conv != null && conv.getUser().getId().equals(userDetails.getUser().getId())) {
            List<AiMessage> messages = chatService.getConversationMessages(id);
            result.put("success", true);
            result.put("messages", messages);
        } else {
            result.put("success", false);
            result.put("error", "Conversation not found");
        }
        return result;
    }

    @DeleteMapping("/conversation/{id}")
    @ResponseBody
    public Map<String, Object> deleteConversation(@PathVariable Long id) {
        chatService.deleteConversation(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/conversations")
    @ResponseBody
    public Map<String, Object> listConversations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AiConversation> convs = chatService.getUserConversations(userDetails.getUser().getId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversations", convs);
        return result;
    }

    // ── Chat Endpoints ──

    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> chat(@RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = (String) body.getOrDefault("message", "");
        Long resumeId = body.get("resumeId") != null ? ((Number) body.get("resumeId")).longValue() : null;
        String jobDescription = (String) body.getOrDefault("jobDescription", "");
        String action = (String) body.getOrDefault("action", "chat");
        Long conversationId = body.get("conversationId") != null ? ((Number) body.get("conversationId")).longValue() : null;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("provider", aiManager.getActiveProvider().getProviderName());

        try {
            String resumeText = "";
            if (resumeId != null) {
                Resume resume = resumeService.getResume(resumeId);
                if (resume != null && resume.getUser().getId().equals(userDetails.getUser().getId())) {
                    resumeText = buildResumeText(resume);
                }
            }

            // Save user message
            if (conversationId != null) {
                chatService.addMessage(conversationId, "user", message, action);
            }

            String reply = "";
            switch (action.toLowerCase()) {
                case "improve-summary":
                    reply = aiManager.generateSummary(resumeText, jobDescription, "");
                    response.put("type", "suggestion");
                    response.put("result", reply);
                    break;
                case "improve-experience":
                    String expText = (String) body.getOrDefault("sectionText", message);
                    reply = aiManager.improveExperience(expText, jobDescription);
                    response.put("type", "suggestion");
                    response.put("result", reply);
                    response.put("original", expText);
                    break;
                case "improve-project":
                    String projText = (String) body.getOrDefault("sectionText", message);
                    reply = aiManager.improveProject(projText, jobDescription);
                    response.put("type", "suggestion");
                    response.put("result", reply);
                    response.put("original", projText);
                    break;
                case "analyze-job":
                    reply = formatJobAnalysis(aiManager.analyzeJobDescription(
                        !jobDescription.isBlank() ? jobDescription : message));
                    response.put("type", "analysis");
                    break;
                case "match":
                    if (!resumeText.isBlank() && !jobDescription.isBlank()) {
                        reply = formatMatchResult(aiManager.matchResumeToJob(resumeText, jobDescription));
                        response.put("type", "match");
                    } else {
                        reply = "To analyze resume compatibility, I need both your resume and a job description. Please paste a job description and try again.";
                        response.put("type", "chat");
                    }
                    break;
                case "recommend-skills":
                    reply = formatSkillsResult(aiManager.recommendSkills(resumeText, jobDescription));
                    response.put("type", "skills");
                    break;
                case "career":
                    reply = formatCareerResult(aiManager.careerRecommendation(resumeText));
                    response.put("type", "career");
                    break;
                default:
                    reply = generateChatResponse(message, resumeText, jobDescription);
                    response.put("type", "chat");
                    break;
            }

            response.put("reply", reply);

            // Save assistant message
            if (conversationId != null) {
                chatService.addMessage(conversationId, "assistant", reply, action);
            }
        } catch (Exception e) {
            log.error("AI Assistant error: {}", e.getMessage());
            response.put("success", false);
            response.put("error", "ResumeCraft AI is temporarily unavailable. Please try again.");
        }
        return response;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam String message,
                                 @RequestParam(required = false) Long resumeId,
                                 @RequestParam(required = false, defaultValue = "") String jobDescription,
                                 @RequestParam(required = false, defaultValue = "chat") String action,
                                 @RequestParam(required = false, defaultValue = "") String history,
                                 @RequestParam(required = false) Long conversationId,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {

        SseEmitter emitter = new SseEmitter(60000L);
        executor.execute(() -> {
            try {
                String resumeText = "";
                if (resumeId != null) {
                    Resume resume = resumeService.getResume(resumeId);
                    if (resume != null && resume.getUser().getId().equals(userDetails.getUser().getId())) {
                        resumeText = buildResumeText(resume);
                    }
                }

                // Save user message
                if (conversationId != null) {
                    chatService.addMessage(conversationId, "user", message, action);
                }

                emitter.send(SseEmitter.event().name("status").data("Thinking..."));
                Thread.sleep(200);

                String result = "";
                String type = "chat";

                switch (action.toLowerCase()) {
                    case "improve-summary":
                        result = aiManager.generateSummary(resumeText, jobDescription, "");
                        type = "suggestion";
                        break;
                    case "improve-experience":
                        result = aiManager.improveExperience(message, jobDescription);
                        type = "suggestion";
                        break;
                    case "improve-project":
                        result = aiManager.improveProject(message, jobDescription);
                        type = "suggestion";
                        break;
                    case "analyze-job":
                        result = formatJobAnalysis(aiManager.analyzeJobDescription(jobDescription));
                        type = "analysis";
                        break;
                    case "match":
                        result = formatMatchResult(aiManager.matchResumeToJob(resumeText, jobDescription));
                        type = "match";
                        break;
                    case "recommend-skills":
                        result = formatSkillsResult(aiManager.recommendSkills(resumeText, jobDescription));
                        type = "skills";
                        break;
                    case "career":
                        result = formatCareerResult(aiManager.careerRecommendation(resumeText));
                        type = "career";
                        break;
                    default:
                        result = generateChatResponse(message, resumeText, jobDescription);
                        break;
                }

                // Stream word by word
                String[] words = result.split(" ");
                StringBuilder chunk = new StringBuilder();
                for (int i = 0; i < words.length; i++) {
                    chunk.append(words[i]);
                    if (i < words.length - 1) chunk.append(" ");
                    if ((i + 1) % 4 == 0 || i == words.length - 1) {
                        emitter.send(SseEmitter.event().name("chunk").data(chunk.toString()));
                        chunk.setLength(0);
                        Thread.sleep(25);
                    }
                }

                // Save assistant message
                if (conversationId != null) {
                    chatService.addMessage(conversationId, "assistant", result, type);
                }

                emitter.send(SseEmitter.event().name("done").data(type));
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE error: {}", e.getMessage());
                try {
                    emitter.send(SseEmitter.event().name("error").data("ResumeCraft AI is temporarily unavailable."));
                    emitter.complete();
                } catch (Exception ex) { emitter.completeWithError(ex); }
            }
        });
        return emitter;
    }

    // ── Smart Chat Response ──

    private String generateChatResponse(String message, String resumeText, String jobDescription) {
        String lower = message.toLowerCase();

        boolean wantsImprove = lower.contains("improve") || lower.contains("enhance") || lower.contains("better") || lower.contains("rewrite") || lower.contains("upgrade");
        boolean wantsSummary = lower.contains("summary") || lower.contains("about me");
        boolean wantsExperience = lower.contains("experience") || lower.contains("work") || lower.contains("intern");
        boolean wantsProject = lower.contains("project");
        boolean wantsSkill = lower.contains("skill") || lower.contains("technolog");
        boolean wantsCareer = lower.contains("career") || lower.contains("role") || lower.contains("position") || lower.contains("suggest");
        boolean wantsMatch = lower.contains("match") || lower.contains("compatib") || lower.contains("ats") || lower.contains("fit");
        boolean wantsJobAnalyze = lower.contains("analyze") && (lower.contains("job") || lower.contains("description"));
        boolean wantsHelp = lower.contains("help") || lower.contains("what can you") || lower.contains("how do");
        boolean wantsGreet = lower.startsWith("hello") || lower.startsWith("hi") || lower.startsWith("hey");

        if (wantsGreet && !wantsImprove && !wantsSkill && !wantsCareer) {
            if (!resumeText.isBlank()) {
                return "Hello! I can see you have a resume loaded. Here's what I can do:\n\n"
                    + "- **\"Improve my summary\"** -- I'll rewrite your professional summary\n"
                    + "- **\"Improve my experience\"** -- I'll polish your work descriptions\n"
                    + "- **\"Improve my projects\"** -- I'll enhance project descriptions\n"
                    + "- **\"Analyze my skills\"** -- I'll find skill gaps for your target role\n"
                    + "- **\"Suggest career roles\"** -- I'll recommend matching positions\n\n"
                    + "Paste a **job description** in the sidebar for job-specific analysis.";
            }
            return "Hello! I'm ResumeCraft AI. Select a resume from the dropdown above to get personalized advice, "
                + "or ask me anything about resume writing and career tips!";
        }

        if (wantsHelp && !wantsImprove) {
            return "Here's everything I can do:\n\n"
                + "**Resume Improvement**\n"
                + "- \"Improve my summary\" -- Rewrite your professional summary\n"
                + "- \"Improve my experience\" -- Enhance work descriptions\n"
                + "- \"Improve my projects\" -- Polish project descriptions\n\n"
                + "**Analysis**\n"
                + "- \"Analyze my skills\" -- Find missing skills\n"
                + "- \"Match my resume\" -- Get compatibility score (paste job description first)\n"
                + "- \"Analyze this job\" -- Extract job requirements\n\n"
                + "**Career**\n"
                + "- \"Suggest career roles\" -- Get role recommendations\n"
                + "- \"How to pass ATS\" -- ATS optimization tips";
        }

        if (resumeText.isBlank()) {
            return "I'd love to help, but I need your resume first.\n\n"
                + "**Select a resume** from the dropdown in the top-right, then I can give you personalized advice.\n\n"
                + "Or ask general questions like:\n"
                + "- \"How should I write a resume summary?\"\n"
                + "- \"What skills should a Java Developer have?\"";
        }

        if ((wantsImprove || wantsSummary) && !wantsExperience && !wantsProject) {
            String generated = aiManager.generateSummary(resumeText, jobDescription, "");
            return "Here's an improved professional summary:\n\n---\n\n" + generated + "\n\n---\n\n"
                    + "Want me to adjust the tone, make it shorter, or focus on a specific role?";
        }

        if (wantsExperience) {
            String improved = aiManager.improveExperience(resumeText, jobDescription);
            return "Here's an improved experience section:\n\n---\n\n" + improved + "\n\n---\n\n"
                    + "Want me to make it more technical, add achievements, or target a specific role?";
        }

        if (wantsProject) {
            String improved = aiManager.improveProject(resumeText, jobDescription);
            return "Here's an improved project description:\n\n---\n\n" + improved + "\n\n---\n\n"
                    + "Want me to add more technical details or quantify results?";
        }

        if (wantsJobAnalyze) {
            String jd = !jobDescription.isBlank() ? jobDescription : message;
            return formatJobAnalysis(aiManager.analyzeJobDescription(jd));
        }

        if (wantsMatch) {
            if (!jobDescription.isBlank()) {
                return formatMatchResult(aiManager.matchResumeToJob(resumeText, jobDescription));
            }
            return "To check compatibility, I need a job description.\n\n"
                + "**Paste a job description** in the sidebar, then ask me to match your resume against it.";
        }

        if (wantsSkill) {
            String jd = !jobDescription.isBlank() ? jobDescription : message;
            return formatSkillsResult(aiManager.recommendSkills(resumeText, jd));
        }

        if (wantsCareer) {
            return formatCareerResult(aiManager.careerRecommendation(resumeText));
        }

        if (wantsImprove) {
            String summary = aiManager.generateSummary(resumeText, jobDescription, "");
            String experience = aiManager.improveExperience(resumeText, jobDescription);
            String project = aiManager.improveProject(resumeText, jobDescription);
            return "Here are my suggestions:\n\n"
                    + "**Improved Summary:**\n" + summary + "\n\n"
                    + "**Improved Experience:**\n" + experience + "\n\n"
                    + "**Improved Projects:**\n" + project + "\n\n"
                    + "Would you like me to focus on any specific section?";
        }

        if (lower.contains("ats") || lower.contains("pass") || lower.contains("screening")) {
            return "**Tips to pass ATS (Applicant Tracking Systems):**\n\n"
                + "1. **Use standard section headings** -- Summary, Experience, Education, Skills\n"
                + "2. **Include keywords from the job description**\n"
                + "3. **Use a clean format** -- Avoid tables and images\n"
                + "4. **Save as PDF**\n"
                + "5. **Use action verbs** -- Developed, Implemented, Optimized\n"
                + "6. **Include measurable results** -- \"Reduced load time by 40%\"\n\n"
                + "Paste a job description and I'll match your resume against it.";
        }

        return "I can help you improve your resume! Try:\n\n"
            + "- **\"Improve my summary\"** -- Rewrite your professional summary\n"
            + "- **\"Improve my experience\"** -- Enhance work descriptions\n"
            + "- **\"Improve my projects\"** -- Polish project descriptions\n"
            + "- **\"Analyze my skills\"** -- Find skill gaps\n"
            + "- **\"Suggest career roles\"** -- Get recommendations\n"
            + "- **\"How to pass ATS\"** -- ATS tips\n\n"
            + "Or just ask me anything about resume writing!";
    }

    // ── Formatters ──

    private String formatJobAnalysis(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Job Analysis Results**\n\n");
        if (data.containsKey("jobTitle")) sb.append("**Title:** ").append(data.get("jobTitle")).append("\n\n");
        if (data.containsKey("requiredSkills")) {
            List<?> skills = (List<?>) data.get("requiredSkills");
            if (!skills.isEmpty()) { sb.append("**Required Skills:**\n"); skills.forEach(s -> sb.append("- ").append(s).append("\n")); sb.append("\n"); }
        }
        if (data.containsKey("preferredSkills")) {
            List<?> skills = (List<?>) data.get("preferredSkills");
            if (!skills.isEmpty()) { sb.append("**Preferred Skills:**\n"); skills.forEach(s -> sb.append("- ").append(s).append("\n")); sb.append("\n"); }
        }
        if (data.containsKey("technologies")) {
            Set<?> techs = (Set<?>) data.get("technologies");
            if (!techs.isEmpty()) sb.append("**Technologies:** ").append(String.join(", ", techs.stream().map(Object::toString).toList())).append("\n\n");
        }
        return sb.toString();
    }

    private String formatMatchResult(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Resume Compatibility Score**\n\n");
        if (data.containsKey("overallScore")) {
            int score = (Integer) data.get("overallScore");
            String grade = score >= 80 ? "Excellent" : score >= 60 ? "Good" : score >= 40 ? "Fair" : "Needs Improvement";
            sb.append("**Overall:** ").append(score).append("/100 (").append(grade).append(")\n\n");
        }
        if (data.containsKey("matchedSkills")) {
            List<?> matched = (List<?>) data.get("matchedSkills");
            if (!matched.isEmpty()) { sb.append("**Matched:** "); sb.append(String.join(", ", matched.stream().map(s -> "" + s).toList())); sb.append("\n\n"); }
        }
        if (data.containsKey("missingSkills")) {
            List<?> missing = (List<?>) data.get("missingSkills");
            if (!missing.isEmpty()) { sb.append("**Missing:** "); sb.append(String.join(", ", missing.stream().map(s -> "" + s).toList())); sb.append("\n\n"); }
        }
        if (data.containsKey("suggestions")) {
            List<?> suggestions = (List<?>) data.get("suggestions");
            if (!suggestions.isEmpty()) { sb.append("**Recommendations:**\n"); suggestions.forEach(s -> sb.append("- ").append(s).append("\n")); }
        }
        return sb.toString();
    }

    private String formatSkillsResult(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Skills Analysis**\n\n");
        if (data.containsKey("alreadyHave")) {
            List<?> have = (List<?>) data.get("alreadyHave");
            if (!have.isEmpty()) { sb.append("**Skills You Have:**\n"); have.forEach(s -> sb.append("- ").append(s).append("\n")); sb.append("\n"); }
        }
        if (data.containsKey("recommended")) {
            List<?> rec = (List<?>) data.get("recommended");
            if (!rec.isEmpty()) { sb.append("**Skills to Consider Learning:**\n"); rec.forEach(s -> sb.append("- ").append(s).append("\n")); sb.append("\n*Add these to your resume only if you genuinely have experience with them.*\n"); }
        }
        return sb.toString();
    }

    private String formatCareerResult(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Career Recommendations**\n\n");
        if (data.containsKey("recommendations")) {
            List<Map<String, Object>> recs = (List<Map<String, Object>>) data.get("recommendations");
            for (Map<String, Object> rec : recs) {
                sb.append("**").append(rec.get("role")).append("** -- ").append(rec.get("matchPercent")).append("% match\n");
                if (rec.containsKey("reason")) sb.append("  ").append(rec.get("reason")).append("\n");
                sb.append("\n");
            }
        }
        if (sb.length() < 40) sb.append("Add more details to your resume for better recommendations.");
        return sb.toString();
    }

    private String buildResumeText(Resume resume) {
        StringBuilder sb = new StringBuilder();
        if (resume.getFirstName() != null && !resume.getFirstName().isBlank())
            sb.append("Name: ").append(resume.getFirstName()).append(" ").append(resume.getLastName()).append("\n");
        if (resume.getProfessionalTitle() != null && !resume.getProfessionalTitle().isBlank())
            sb.append("Title: ").append(resume.getProfessionalTitle()).append("\n");
        if (resume.getPersonalEmail() != null && !resume.getPersonalEmail().isBlank())
            sb.append("Email: ").append(resume.getPersonalEmail()).append("\n");
        if (resume.getPhone() != null && !resume.getPhone().isBlank())
            sb.append("Phone: ").append(resume.getPhone()).append("\n");
        if (resume.getLocation() != null && !resume.getLocation().isBlank())
            sb.append("Location: ").append(resume.getLocation()).append("\n");
        if (resume.getSummary() != null && !resume.getSummary().isBlank())
            sb.append("\nSUMMARY\n").append(resume.getSummary()).append("\n");

        List<Education> eduList = resumeService.getEducationList(resume.getId());
        if (!eduList.isEmpty()) {
            sb.append("\nEDUCATION\n");
            eduList.forEach(e -> sb.append(e.getDegree()).append(" - ").append(e.getInstitution())
                    .append(" (").append(e.getStartDate()).append("-").append(e.getEndDate()).append(")\n"));
        }
        List<Experience> expList = resumeService.getExperienceList(resume.getId());
        if (!expList.isEmpty()) {
            sb.append("\nEXPERIENCE\n");
            expList.forEach(e -> sb.append(e.getJobTitle()).append(" at ").append(e.getCompany())
                    .append(" (").append(e.getStartDate()).append("-").append(
                            Boolean.TRUE.equals(e.getCurrentlyWorking()) ? "Present" : e.getEndDate()).append(")\n")
                    .append(e.getDescription() != null ? e.getDescription() + "\n" : ""));
        }
        List<Skill> skills = resumeService.getSkills(resume.getId());
        if (!skills.isEmpty()) {
            sb.append("\nSKILLS\n");
            skills.forEach(s -> sb.append(s.getName()).append(", "));
            sb.setLength(sb.length() - 2);
            sb.append("\n");
        }
        List<Project> projList = resumeService.getProjects(resume.getId());
        if (!projList.isEmpty()) {
            sb.append("\nPROJECTS\n");
            projList.forEach(p -> sb.append(p.getName()).append(": ").append(
                    p.getDescription() != null ? p.getDescription() : "").append("\n"));
        }
        List<Certification> certs = resumeService.getCertifications(resume.getId());
        if (!certs.isEmpty()) {
            sb.append("\nCERTIFICATIONS\n");
            certs.forEach(c -> sb.append(c.getName()).append(" - ").append(c.getIssuingOrganization()).append("\n"));
        }
        List<Achievement> achievements = resumeService.getAchievements(resume.getId());
        if (!achievements.isEmpty()) {
            sb.append("\nACHIEVEMENTS\n");
            achievements.forEach(a -> sb.append("- ").append(a.getTitle()).append(": ")
                    .append(a.getDescription() != null ? a.getDescription() : "").append("\n"));
        }
        List<Language> langs = resumeService.getLanguages(resume.getId());
        if (!langs.isEmpty()) {
            sb.append("\nLANGUAGES\n");
            langs.forEach(l -> sb.append(l.getName()).append(" (").append(l.getProficiency()).append(")\n"));
        }
        return sb.toString();
    }
}
