package com.resumeforge.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Rule-based AI fallback — works without any API key.
 * Provides keyword extraction, skill matching, section completeness, and basic scoring.
 */
@Component
public class RuleBasedAiProvider implements AiProvider {

    // Common tech keywords to look for
    private static final Set<String> TECH_KEYWORDS = Set.of(
            "java", "python", "javascript", "typescript", "react", "angular", "vue",
            "spring", "spring boot", "hibernate", "jpa", "jdbc", "servlet", "jsp",
            "html", "css", "bootstrap", "tailwind", "jquery", "node.js", "express",
            "mysql", "postgresql", "mongodb", "redis", "h2",
            "git", "github", "gitlab", "docker", "kubernetes", "aws", "azure", "gcp",
            "rest", "restful", "api", "microservices", "soap",
            "maven", "gradle", "intellij", "eclipse", "vscode",
            "linux", "windows", "agile", "scrum", "jira",
            "c", "c++", "c#", ".net", "php", "ruby", "go", "rust", "scala",
            "machine learning", "ai", "deep learning", "tensorflow", "pytorch",
            "data analysis", "sql", "nosql", "etl", "tableau", "power bi",
            "html5", "css3", "sass", "less", "webpack", "vite",
            "junit", "mockito", "testing", "tdd", "ci/cd", "jenkins",
            "android", "ios", "flutter", "react native",
            "xml", "json", "yaml", "csv",
            "oauth", "jwt", "security", "encryption",
            "cloud", "serverless", "lambda", "s3",
            "talent", "leadership", "communication", "team", "collaboration"
    );

    private static final Set<String> SOFT_SKILLS = Set.of(
            "communication", "leadership", "teamwork", "problem solving",
            "analytical", "creative", "adaptability", "time management",
            "critical thinking", "attention to detail", "collaboration",
            "mentoring", "presentation", "negotiation"
    );

    @Override
    public Map<String, Object> analyzeJobDescription(String jobDescription) {
        String lower = jobDescription.toLowerCase();

        Map<String, Object> result = new HashMap<>();
        result.put("jobTitle", extractJobTitle(jobDescription));
        result.put("requiredSkills", extractKeywords(lower, "requirements", "required", "must have"));
        result.put("preferredSkills", extractKeywords(lower, "preferred", "nice to have", "bonus"));
        result.put("technologies", extractTechKeywords(lower));
        result.put("responsibilities", extractBullets(jobDescription, "responsibilities", "what you'll do", "duties"));
        result.put("experienceRequired", extractPattern(lower, "(\\d+\\+?\\s*(?:years?|yrs?)\\s*(?:of)?\\s*experience)"));
        result.put("educationRequired", extractPattern(lower, "(bachelor|master|degree|phd|mca|bca|btech|mtech|mba)[^.]*"));
        result.put("keywords", extractTechKeywords(lower));
        result.put("softSkills", extractSoftSkills(lower));
        return result;
    }

    @Override
    public Map<String, Object> analyzeResume(String resumeText) {
        String lower = resumeText.toLowerCase();
        Map<String, Object> result = new HashMap<>();
        result.put("personalInfo", extractPersonalInfo(resumeText));
        result.put("summary", extractSection(resumeText, "summary", "objective", "profile"));
        result.put("skills", extractTechKeywords(lower));
        result.put("experience", extractBullets(resumeText, "experience", "work experience", "employment"));
        result.put("education", extractBullets(resumeText, "education", "academic"));
        result.put("projects", extractBullets(resumeText, "projects", "project"));
        result.put("certifications", extractBullets(resumeText, "certification", "certificate"));
        result.put("achievements", extractBullets(resumeText, "achievement", "award"));
        result.put("languages", extractBullets(resumeText, "language"));
        return result;
    }

    @Override
    public Map<String, Object> matchResumeToJob(String resumeText, String jobDescription) {
        String resumeLower = resumeText.toLowerCase();
        String jobLower = jobDescription.toLowerCase();

        Set<String> resumeSkills = extractTechKeywords(resumeLower);
        Set<String> jobSkills = extractTechKeywords(jobLower);

        Set<String> matched = new HashSet<>(resumeSkills);
        matched.retainAll(jobSkills);

        Set<String> missing = new HashSet<>(jobSkills);
        missing.removeAll(resumeSkills);

        Set<String> resumeKw = extractAllKeywords(resumeLower);
        Set<String> jobKw = extractAllKeywords(jobLower);

        Set<String> matchedKw = new HashSet<>(resumeKw);
        matchedKw.retainAll(jobKw);
        Set<String> missingKw = new HashSet<>(jobKw);
        missingKw.removeAll(resumeKw);

        int skillsScore = jobSkills.isEmpty() ? 100 : (int) ((double) matched.size() / jobSkills.size() * 100);
        int kwScore = jobKw.isEmpty() ? 100 : (int) ((double) matchedKw.size() / jobKw.size() * 100);

        // Section completeness
        int sectionScore = calculateSectionScore(resumeText);

        int overall = (int) (skillsScore * 0.4 + kwScore * 0.3 + sectionScore * 0.3);
        overall = Math.min(100, Math.max(0, overall));

        Map<String, Object> result = new HashMap<>();
        result.put("overallScore", overall);
        result.put("skillsMatch", Math.min(100, skillsScore));
        result.put("keywordMatch", Math.min(100, kwScore));
        result.put("experienceMatch", sectionScore);
        result.put("educationMatch", hasSection(resumeText, "education") ? 95 : 40);
        result.put("matchedSkills", sorted(matched));
        result.put("missingSkills", sorted(missing));
        result.put("matchedKeywords", sorted(matchedKw));
        result.put("missingKeywords", sorted(missingKw));
        result.put("suggestions", generateSuggestions(matched, missing, resumeText));
        return result;
    }

    @Override
    public String generateSummary(String resumeText, String jobDescription, String targetTitle) {
        Set<String> skills = extractTechKeywords(resumeText.toLowerCase());
        String topSkills = skills.stream().limit(5).collect(Collectors.joining(", "));
        String title = (targetTitle != null && !targetTitle.isBlank()) ? targetTitle : "professional";

        return "Motivated " + title + " with experience in " +
                (topSkills.isEmpty() ? "software development" : topSkills) +
                ". Strong problem-solving skills and passion for building efficient solutions. " +
                "Seeking to contribute to a dynamic team.";
    }

    @Override
    public String improveExperience(String experienceText, String jobDescription) {
        // Rule-based: add action verbs and structure
        String[] verbs = {"Developed", "Implemented", "Designed", "Built", "Optimized", "Managed", "Led", "Collaborated"};
        String improved = experienceText;

        // Capitalize first letter and ensure proper structure
        if (!improved.isEmpty()) {
            improved = improved.substring(0, 1).toUpperCase() + improved.substring(1);
        }

        // Add period if missing
        if (!improved.endsWith(".") && !improved.endsWith("!")) {
            improved += ".";
        }

        return improved;
    }

    @Override
    public String improveProject(String projectText, String jobDescription) {
        String improved = projectText;
        if (!improved.isEmpty()) {
            improved = improved.substring(0, 1).toUpperCase() + improved.substring(1);
        }
        if (!improved.endsWith(".")) {
            improved += ".";
        }
        return improved;
    }

    @Override
    public Map<String, Object> careerRecommendation(String resumeText) {
        String lower = resumeText.toLowerCase();
        List<Map<String, Object>> recs = new ArrayList<>();

        String[][] roles = {
                {"Java Developer", "java", "spring boot", "hibernate", "mysql"},
                {"Backend Developer", "java", "python", "sql", "api", "rest"},
                {"Full Stack Developer", "javascript", "react", "html", "css", "node"},
                {"Frontend Developer", "javascript", "react", "html", "css", "angular", "vue"},
                {"Data Analyst", "sql", "python", "tableau", "excel", "data"},
                {"Software Engineer", "java", "python", "git", "api"},
                {"DevOps Engineer", "docker", "kubernetes", "aws", "jenkins", "ci/cd"},
                {"Mobile Developer", "android", "ios", "flutter", "react native"}
        };

        for (String[] role : roles) {
            int match = 0;
            int total = role.length - 1;
            for (int i = 1; i < role.length; i++) {
                if (lower.contains(role[i])) match++;
            }
            int pct = (int) ((double) match / total * 100);
            if (pct >= 30) {
                Map<String, Object> r = new HashMap<>();
                r.put("role", role[0]);
                r.put("matchPercent", Math.min(98, pct));
                r.put("reason", "Based on skills found in your resume");
                recs.add(r);
            }
        }

        recs.sort((a, b) -> (int) b.get("matchPercent") - (int) a.get("matchPercent"));
        if (recs.size() > 5) recs = recs.subList(0, 5);

        Map<String, Object> result = new HashMap<>();
        result.put("recommendations", recs);
        return result;
    }

    @Override
    public Map<String, Object> recommendSkills(String resumeText, String jobDescription) {
        String resumeLower = resumeText.toLowerCase();
        String jobLower = jobDescription.toLowerCase();

        Set<String> resumeSkills = extractTechKeywords(resumeLower);
        Set<String> jobSkills = extractTechKeywords(jobLower);

        Set<String> alreadyHave = new HashSet<>(resumeSkills);
        alreadyHave.retainAll(jobSkills);

        Set<String> recommended = new HashSet<>(jobSkills);
        recommended.removeAll(resumeSkills);

        Set<String> niceToHave = new HashSet<>(recommended);
        niceToHave.removeAll(Set.of("java", "python", "javascript", "sql", "git", "html", "css"));

        Map<String, Object> result = new HashMap<>();
        result.put("alreadyHave", sorted(alreadyHave));
        result.put("recommended", sorted(recommended));
        result.put("niceToHave", sorted(niceToHave));
        return result;
    }

    @Override
    public String getProviderName() {
        return "Rule-Based (No API)";
    }

    // ── Helper Methods ────────────────────────────────────────

    private Set<String> extractTechKeywords(String text) {
        Set<String> found = new LinkedHashSet<>();
        for (String kw : TECH_KEYWORDS) {
            if (text.contains(kw)) found.add(kw);
        }
        return found;
    }

    private Set<String> extractAllKeywords(String text) {
        Set<String> found = new LinkedHashSet<>();
        for (String kw : TECH_KEYWORDS) {
            if (text.contains(kw)) found.add(kw);
        }
        for (String sk : SOFT_SKILLS) {
            if (text.contains(sk)) found.add(sk);
        }
        return found;
    }

    private Set<String> extractSoftSkills(String text) {
        Set<String> found = new LinkedHashSet<>();
        for (String sk : SOFT_SKILLS) {
            if (text.contains(sk)) found.add(sk);
        }
        return found;
    }

    private String extractJobTitle(String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && line.length() < 80 && !line.toLowerCase().startsWith("http")) {
                return line;
            }
        }
        return "Unknown Position";
    }

    private List<String> extractKeywords(String text, String... sectionHeaders) {
        List<String> keywords = new ArrayList<>();
        for (String header : sectionHeaders) {
            int idx = text.indexOf(header);
            if (idx >= 0) {
                String chunk = text.substring(idx, Math.min(idx + 500, text.length()));
                String[] lines = chunk.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && trimmed.length() < 100
                            && !trimmed.toLowerCase().contains(header)) {
                        keywords.add(trimmed.replaceAll("^[•\\-*]+\\s*", ""));
                    }
                    if (keywords.size() > 20) break;
                }
            }
        }
        return keywords.stream().distinct().limit(15).collect(Collectors.toList());
    }

    private List<String> extractBullets(String text, String... headers) {
        List<String> bullets = new ArrayList<>();
        String lower = text.toLowerCase();
        for (String header : headers) {
            int idx = lower.indexOf(header.toLowerCase());
            if (idx >= 0) {
                String chunk = text.substring(idx, Math.min(idx + 1000, text.length()));
                String[] lines = chunk.split("\n");
                for (String line : lines) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && trimmed.length() > 10 && trimmed.length() < 300) {
                        bullets.add(trimmed.replaceAll("^[•\\-*]+\\s*", ""));
                    }
                    if (bullets.size() > 10) break;
                }
                if (!bullets.isEmpty()) break;
            }
        }
        return bullets;
    }

    private String extractSection(String text, String... headers) {
        String lower = text.toLowerCase();
        for (String header : headers) {
            int idx = lower.indexOf(header.toLowerCase());
            if (idx >= 0) {
                String chunk = text.substring(idx, Math.min(idx + 500, text.length()));
                String[] lines = chunk.split("\n");
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i < lines.length && i < 6; i++) {
                    String trimmed = lines[i].trim();
                    if (!trimmed.isEmpty()) sb.append(trimmed).append(" ");
                }
                return sb.toString().trim();
            }
        }
        return "";
    }

    private Map<String, String> extractPersonalInfo(String text) {
        Map<String, String> info = new HashMap<>();
        // Email
        Matcher emailM = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").matcher(text);
        if (emailM.find()) info.put("email", emailM.group());

        // Phone
        Matcher phoneM = Pattern.compile("[+]?\\d[\\d\\s-]{8,13}\\d").matcher(text);
        if (phoneM.find()) info.put("phone", phoneM.group().trim());

        // Name — first non-empty line that looks like a name
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && trimmed.length() < 50
                    && trimmed.matches("[A-Za-z .]+")
                    && !trimmed.toLowerCase().contains("resume")
                    && !trimmed.toLowerCase().contains("cv")) {
                info.put("name", trimmed);
                break;
            }
        }

        return info;
    }

    private String extractPattern(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) return m.group();
        return "";
    }

    private boolean hasSection(String text, String section) {
        return text.toLowerCase().contains(section);
    }

    private int calculateSectionScore(String resumeText) {
        String lower = resumeText.toLowerCase();
        int score = 0;
        String[] sections = {"summary", "experience", "education", "skills", "projects", "certification"};
        for (String s : sections) {
            if (lower.contains(s)) score += 17;
        }
        // Bonus for length
        if (resumeText.length() > 500) score += 8;
        if (resumeText.length() > 1000) score += 5;
        return Math.min(100, score);
    }

    private List<String> generateSuggestions(Set<String> matched, Set<String> missing, String resumeText) {
        List<String> suggestions = new ArrayList<>();
        if (missing.isEmpty()) {
            suggestions.add("Your resume covers the key technical skills for this role.");
        } else {
            suggestions.add("Consider learning: " + String.join(", ", missing.stream().limit(3).toList()));
        }
        if (!resumeText.toLowerCase().contains("summary")) {
            suggestions.add("Add a professional summary section.");
        }
        if (!resumeText.toLowerCase().contains("project")) {
            suggestions.add("Add a projects section to showcase your work.");
        }
        if (resumeText.length() < 500) {
            suggestions.add("Your resume content is quite short. Add more detail.");
        }
        return suggestions;
    }

    private List<String> sorted(Set<String> set) {
        return set.stream().sorted().collect(Collectors.toList());
    }
}
