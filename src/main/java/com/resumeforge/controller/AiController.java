package com.resumeforge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeforge.ai.AiManager;
import com.resumeforge.entity.*;
import com.resumeforge.repository.AiAnalysisRepository;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.ResumeParserService;
import com.resumeforge.service.ResumeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/ai")
public class AiController {

    private static final Logger log = LoggerFactory.getLogger(AiController.class);

    private final AiManager aiManager;
    private final ResumeParserService parserService;
    private final ResumeService resumeService;
    private final AiAnalysisRepository analysisRepository;
    private final ObjectMapper objectMapper;

    public AiController(AiManager aiManager, ResumeParserService parserService,
                        ResumeService resumeService, AiAnalysisRepository analysisRepository,
                        ObjectMapper objectMapper) {
        this.aiManager = aiManager;
        this.parserService = parserService;
        this.resumeService = resumeService;
        this.analysisRepository = analysisRepository;
        this.objectMapper = objectMapper;
    }

    // ── Page Routes ──────────────────────────────────────────

    @GetMapping("/tools")
    public String aiToolsPage(Model model,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        model.addAttribute("user", userDetails.getUser());
        long analysisCount = analysisRepository.countByUserId(userDetails.getUser().getId());
        model.addAttribute("analysisCount", analysisCount);
        model.addAttribute("aiProvider", aiManager.getActiveProvider().getProviderName());
        return "ai-tools";
    }

    @GetMapping("/tailor")
    public String tailorPage(Model model,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Resume> resumes = resumeService.getUserResumes(userDetails.getUser().getId());
        model.addAttribute("resumes", resumes);
        model.addAttribute("user", userDetails.getUser());
        return "ai-tailor";
    }

    // ── API Endpoints ────────────────────────────────────────

    /** Upload a resume file and extract text */
    @PostMapping("/upload-resume")
    @ResponseBody
    public Map<String, Object> uploadResume(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        String error = parserService.validateFile(file);
        if (error != null) {
            result.put("success", false);
            result.put("error", error);
            return result;
        }
        try {
            String text = parserService.extractText(file);
            result.put("success", true);
            result.put("text", text);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
        } catch (Exception e) {
            log.error("Resume parsing failed: {}", e.getMessage());
            result.put("success", false);
            result.put("error", "Failed to parse file: " + e.getMessage());
        }
        return result;
    }

    /** Get resume text from a stored resume */
    @PostMapping("/get-resume-text")
    @ResponseBody
    public Map<String, Object> getResumeText(@RequestParam Long resumeId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();
        try {
            Resume resume = resumeService.getResume(resumeId);
            if (!resume.getUser().getId().equals(userDetails.getUser().getId())) {
                result.put("success", false);
                result.put("error", "Unauthorized");
                return result;
            }
            String text = buildResumeText(resume);
            result.put("success", true);
            result.put("text", text);
            result.put("resumeId", resumeId);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /** Analyze a job description */
    @PostMapping("/analyze-job")
    @ResponseBody
    public Map<String, Object> analyzeJob(@RequestBody Map<String, String> body) {
        String jobDesc = body.getOrDefault("jobDescription", "");
        if (jobDesc.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Please paste a job description.");
            return err;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", aiManager.analyzeJobDescription(jobDesc));
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Match resume against a job description */
    @PostMapping("/match")
    @ResponseBody
    public Map<String, Object> matchResume(@RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        String resumeText = body.getOrDefault("resumeText", "");
        String jobDesc = body.getOrDefault("jobDescription", "");
        if (resumeText.isBlank() || jobDesc.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Both resume text and job description are required.");
            return err;
        }

        Map<String, Object> matchResult = aiManager.matchResumeToJob(resumeText, jobDesc);

        // Save analysis
        try {
            AiAnalysis analysis = new AiAnalysis(userDetails.getUser(), "JOB_MATCH");
            analysis.setJobDescription(jobDesc);
            analysis.setResultJson(objectMapper.writeValueAsString(matchResult));
            if (matchResult.containsKey("overallScore")) {
                analysis.setOverallScore((Integer) matchResult.get("overallScore"));
            }
            analysis.setProviderUsed(aiManager.getActiveProvider().getProviderName());
            analysisRepository.save(analysis);
        } catch (Exception e) {
            log.warn("Failed to save AI analysis: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", matchResult);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Generate a tailored professional summary */
    @PostMapping("/generate-summary")
    @ResponseBody
    public Map<String, Object> generateSummary(@RequestBody Map<String, String> body) {
        String resumeText = body.getOrDefault("resumeText", "");
        String jobDesc = body.getOrDefault("jobDescription", "");
        String title = body.getOrDefault("targetTitle", "");

        String summary = aiManager.generateSummary(resumeText, jobDesc, title);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("summary", summary);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Improve an experience entry */
    @PostMapping("/improve-experience")
    @ResponseBody
    public Map<String, Object> improveExperience(@RequestBody Map<String, String> body) {
        String expText = body.getOrDefault("experienceText", "");
        String jobDesc = body.getOrDefault("jobDescription", "");

        String improved = aiManager.improveExperience(expText, jobDesc);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("original", expText);
        result.put("improved", improved);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Improve a project description */
    @PostMapping("/improve-project")
    @ResponseBody
    public Map<String, Object> improveProject(@RequestBody Map<String, String> body) {
        String projectText = body.getOrDefault("projectText", "");
        String jobDesc = body.getOrDefault("jobDescription", "");

        String improved = aiManager.improveProject(projectText, jobDesc);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("original", projectText);
        result.put("improved", improved);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Get career recommendations based on resume */
    @PostMapping("/career-recommendation")
    @ResponseBody
    public Map<String, Object> careerRecommendation(@RequestBody Map<String, String> body) {
        String resumeText = body.getOrDefault("resumeText", "");
        if (resumeText.isBlank()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Resume text is required.");
            return err;
        }

        Map<String, Object> recs = aiManager.careerRecommendation(resumeText);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", recs);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Recommend skills for a job */
    @PostMapping("/recommend-skills")
    @ResponseBody
    public Map<String, Object> recommendSkills(@RequestBody Map<String, String> body) {
        String resumeText = body.getOrDefault("resumeText", "");
        String jobDesc = body.getOrDefault("jobDescription", "");

        Map<String, Object> skills = aiManager.recommendSkills(resumeText, jobDesc);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", skills);
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        return result;
    }

    /** Get AI provider status */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> status(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();
        result.put("provider", aiManager.getActiveProvider().getProviderName());
        result.put("analysisCount", analysisRepository.countByUserId(userDetails.getUser().getId()));
        return result;
    }

    // ── Helper: Build resume text from entity ────────────────

    private String buildResumeText(Resume resume) {
        StringBuilder sb = new StringBuilder();

        // Personal Info
        if (resume.getFirstName() != null && !resume.getFirstName().isBlank()) {
            sb.append("Name: ").append(resume.getFirstName()).append(" ").append(resume.getLastName()).append("\n");
        }
        if (resume.getProfessionalTitle() != null && !resume.getProfessionalTitle().isBlank()) {
            sb.append("Title: ").append(resume.getProfessionalTitle()).append("\n");
        }
        if (resume.getPersonalEmail() != null && !resume.getPersonalEmail().isBlank()) {
            sb.append("Email: ").append(resume.getPersonalEmail()).append("\n");
        }
        if (resume.getPhone() != null && !resume.getPhone().isBlank()) {
            sb.append("Phone: ").append(resume.getPhone()).append("\n");
        }
        if (resume.getLocation() != null && !resume.getLocation().isBlank()) {
            sb.append("Location: ").append(resume.getLocation()).append("\n");
        }

        // Summary
        if (resume.getSummary() != null && !resume.getSummary().isBlank()) {
            sb.append("\nSUMMARY\n").append(resume.getSummary()).append("\n");
        }

        // Education
        List<Education> eduList = resumeService.getEducationList(resume.getId());
        if (!eduList.isEmpty()) {
            sb.append("\nEDUCATION\n");
            for (Education e : eduList) {
                sb.append(e.getDegree()).append(" in ").append(e.getField())
                  .append(" - ").append(e.getInstitution())
                  .append(" (").append(e.getStartDate()).append(" - ").append(e.getEndDate()).append(")");
                if (e.getGrade() != null && !e.getGrade().isBlank()) sb.append(" CGPA: ").append(e.getGrade());
                sb.append("\n");
            }
        }

        // Experience
        List<Experience> expList = resumeService.getExperienceList(resume.getId());
        if (!expList.isEmpty()) {
            sb.append("\nEXPERIENCE\n");
            for (Experience e : expList) {
                sb.append(e.getJobTitle()).append(" at ").append(e.getCompany())
                  .append(", ").append(e.getLocation())
                  .append(" (").append(e.getStartDate()).append(" - ")
                  .append(Boolean.TRUE.equals(e.getCurrentlyWorking()) ? "Present" : e.getEndDate()).append(")\n");
                if (e.getDescription() != null && !e.getDescription().isBlank()) sb.append(e.getDescription()).append("\n");
                if (e.getAchievements() != null && !e.getAchievements().isBlank()) sb.append("Achievements: ").append(e.getAchievements()).append("\n");
            }
        }

        // Skills
        List<Skill> skills = resumeService.getSkills(resume.getId());
        if (!skills.isEmpty()) {
            sb.append("\nSKILLS\n");
            for (Skill s : skills) {
                sb.append(s.getName());
                if (s.getCategory() != null && !s.getCategory().isBlank()) sb.append(" (").append(s.getCategory()).append(")");
                if (s.getProficiency() != null && !s.getProficiency().isBlank()) sb.append(" - ").append(s.getProficiency());
                sb.append("\n");
            }
        }

        // Projects
        List<Project> projList = resumeService.getProjects(resume.getId());
        if (!projList.isEmpty()) {
            sb.append("\nPROJECTS\n");
            for (Project p : projList) {
                sb.append(p.getName()).append("\n");
                if (p.getDescription() != null && !p.getDescription().isBlank()) sb.append(p.getDescription()).append("\n");
                if (p.getTechnologies() != null && !p.getTechnologies().isBlank()) sb.append("Technologies: ").append(p.getTechnologies()).append("\n");
                if (p.getRole() != null && !p.getRole().isBlank()) sb.append("Role: ").append(p.getRole()).append("\n");
                if (p.getKeyContributions() != null && !p.getKeyContributions().isBlank()) sb.append("Contributions: ").append(p.getKeyContributions()).append("\n");
            }
        }

        // Certifications
        List<Certification> certs = resumeService.getCertifications(resume.getId());
        if (!certs.isEmpty()) {
            sb.append("\nCERTIFICATIONS\n");
            for (Certification c : certs) {
                sb.append(c.getName()).append(" - ").append(c.getIssuingOrganization())
                  .append(" (").append(c.getDate()).append(")\n");
            }
        }

        // Achievements
        List<Achievement> achievements = resumeService.getAchievements(resume.getId());
        if (!achievements.isEmpty()) {
            sb.append("\nACHIEVEMENTS\n");
            for (Achievement a : achievements) {
                sb.append("- ").append(a.getDescription()).append("\n");
            }
        }

        // Languages
        List<Language> langs = resumeService.getLanguages(resume.getId());
        if (!langs.isEmpty()) {
            sb.append("\nLANGUAGES\n");
            for (Language l : langs) {
                sb.append(l.getName()).append(" - ").append(l.getProficiency()).append("\n");
            }
        }

        return sb.toString();
    }
}
