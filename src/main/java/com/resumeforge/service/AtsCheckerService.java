package com.resumeforge.service;

import com.resumeforge.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AtsCheckerService {

    public Map<String, Object> analyze(Resume resume) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();
        int passed = 0;
        int total = 0;

        // Contact Info Check
        total++;
        boolean hasContact = isNotEmpty(resume.getFirstName()) && isNotEmpty(resume.getLastName())
                && isNotEmpty(resume.getPersonalEmail()) && isNotEmpty(resume.getPhone());
        items.add(createItem("Contact Information",
                hasContact ? "Complete — name, email, phone present" : "Missing — add your full name, email, and phone number",
                hasContact ? "pass" : "fail"));
        if (hasContact) passed++;

        // Email Format Check
        total++;
        boolean validEmail = isNotEmpty(resume.getPersonalEmail()) && resume.getPersonalEmail().contains("@");
        items.add(createItem("Email Format",
                validEmail ? "Valid email format detected" : "Invalid or missing email address",
                validEmail ? "pass" : "fail"));
        if (validEmail) passed++;

        // Professional Summary
        total++;
        boolean hasSummary = isNotEmpty(resume.getSummary()) && resume.getSummary().length() >= 50;
        boolean shortSummary = isNotEmpty(resume.getSummary()) && resume.getSummary().length() < 50 && resume.getSummary().length() > 0;
        items.add(createItem("Professional Summary",
                hasSummary ? "Good length — " + resume.getSummary().length() + " characters" :
                        shortSummary ? "Too short — aim for 50+ characters for better ATS readability" :
                                "Missing — add a 2-4 sentence professional summary",
                hasSummary ? "pass" : shortSummary ? "warn" : "fail"));
        if (hasSummary) passed++;

        // Skills Check
        total++;
        boolean hasSkills = resume.getSkills() != null && !resume.getSkills().isEmpty();
        boolean enoughSkills = hasSkills && resume.getSkills().size() >= 5;
        items.add(createItem("Skills Section",
                enoughSkills ? resume.getSkills().size() + " skills listed — good coverage" :
                        hasSkills ? resume.getSkills().size() + " skills — add more (aim for 5-15)" :
                                "Missing — add relevant technical and soft skills",
                enoughSkills ? "pass" : hasSkills ? "warn" : "fail"));
        if (enoughSkills) passed++;

        // Education
        total++;
        boolean hasEdu = resume.getEducationList() != null && !resume.getEducationList().isEmpty();
        items.add(createItem("Education",
                hasEdu ? "Education listed — include degree, institution, and dates" :
                        "Missing — add your educational background",
                hasEdu ? "pass" : "warn"));
        if (hasEdu) passed++;

        // Experience
        total++;
        boolean hasExp = resume.getExperienceList() != null && !resume.getExperienceList().isEmpty();
        items.add(createItem("Work Experience",
                hasExp ? resume.getExperienceList().size() + " position(s) listed" :
                        "Missing — add work experience, internships, or volunteer work",
                hasExp ? "pass" : "warn"));
        if (hasExp) passed++;

        // Projects
        total++;
        boolean hasProjects = resume.getProjects() != null && !resume.getProjects().isEmpty();
        items.add(createItem("Projects",
                hasProjects ? resume.getProjects().size() + " project(s) — include technologies used" :
                        "Missing — add relevant projects to demonstrate skills",
                hasProjects ? "pass" : "warn"));
        if (hasProjects) passed++;

        // Keywords Check (check for common ATS keywords in summary + skills)
        total++;
        String allText = buildAllText(resume);
        boolean hasKeywords = containsTechKeywords(allText);
        items.add(createItem("ATS Keywords",
                hasKeywords ? "Technical keywords detected in your resume" :
                        "Add industry-specific keywords to improve ATS matching",
                hasKeywords ? "pass" : "warn"));
        if (hasKeywords) passed++;

        // Date Formatting
        total++;
        boolean hasDates = (isNotEmpty(resume.getEducationList().isEmpty() ? null : "x") ||
                resume.getEducationList().stream().anyMatch(e -> isNotEmpty(e.getStartDate())));
        items.add(createItem("Date Formatting",
                "Use consistent date format (e.g., Jan 2023 - Present) for ATS readability",
                "info"));
        passed++; // Always give this a pass since it's just advice

        // Length Check
        total++;
        int sectionCount = 0;
        if (hasSummary) sectionCount++;
        if (hasEdu) sectionCount++;
        if (hasExp) sectionCount++;
        if (hasSkills) sectionCount++;
        if (hasProjects) sectionCount++;
        boolean goodLength = sectionCount >= 4;
        items.add(createItem("Resume Length",
                goodLength ? sectionCount + " sections filled — good resume depth" :
                        "Only " + sectionCount + " section(s) filled — add more for a complete resume",
                goodLength ? "pass" : "warn"));
        if (goodLength) passed++;

        result.put("score", (passed * 100) / total);
        result.put("passed", passed);
        result.put("total", total);
        result.put("items", items);
        result.put("grade", getGrade(passed, total));
        return result;
    }

    private boolean containsTechKeywords(String text) {
        String lower = text.toLowerCase();
        String[] keywords = {"java", "python", "javascript", "sql", "html", "css", "react",
                "spring", "database", "api", "rest", "git", "agile", "testing",
                "development", "analysis", "management", "design", "programming"};
        for (String kw : keywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    private String buildAllText(Resume resume) {
        StringBuilder sb = new StringBuilder();
        if (isNotEmpty(resume.getSummary())) sb.append(resume.getSummary());
        if (resume.getSkills() != null) {
            for (var s : resume.getSkills()) sb.append(" ").append(s.getName());
        }
        if (resume.getProjects() != null) {
            for (var p : resume.getProjects()) sb.append(" ").append(p.getTechnologies());
        }
        return sb.toString();
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Map<String, Object> createItem(String title, String detail, String status) {
        Map<String, Object> item = new HashMap<>();
        item.put("title", title);
        item.put("detail", detail);
        item.put("status", status); // pass, fail, warn, info
        return item;
    }

    private String getGrade(int passed, int total) {
        double pct = (passed * 100.0) / total;
        if (pct >= 80) return "A";
        if (pct >= 60) return "B";
        if (pct >= 40) return "C";
        return "D";
    }
}
