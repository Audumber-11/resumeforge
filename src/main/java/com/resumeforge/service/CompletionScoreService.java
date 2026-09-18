package com.resumeforge.service;

import com.resumeforge.entity.Resume;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CompletionScoreService {

    public Map<String, Object> calculateScore(Resume resume) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> checks = new ArrayList<>();
        int totalScore = 0;

        // Personal Information (20 points)
        int personalScore = 0;
        if (isNotEmpty(resume.getFirstName()) && isNotEmpty(resume.getLastName())) personalScore += 6;
        if (isNotEmpty(resume.getProfessionalTitle())) personalScore += 4;
        if (isNotEmpty(resume.getPersonalEmail())) personalScore += 4;
        if (isNotEmpty(resume.getPhone())) personalScore += 3;
        if (isNotEmpty(resume.getLocation())) personalScore += 3;
        totalScore += personalScore;
        checks.add(createCheck("Personal Information", personalScore, 20));

        // Summary (15 points)
        int summaryScore = isNotEmpty(resume.getSummary()) ? 15 : 0;
        if (isNotEmpty(resume.getSummary()) && resume.getSummary().length() > 50) summaryScore = 15;
        else if (isNotEmpty(resume.getSummary())) summaryScore = 8;
        totalScore += summaryScore;
        checks.add(createCheck("Professional Summary", summaryScore, 15));

        // Education (15 points)
        int eduScore = 0;
        if (resume.getEducationList() != null && !resume.getEducationList().isEmpty()) {
            eduScore = Math.min(15, 5 + resume.getEducationList().size() * 5);
        }
        totalScore += eduScore;
        checks.add(createCheck("Education", eduScore, 15));

        // Experience (15 points)
        int expScore = 0;
        if (resume.getExperienceList() != null && !resume.getExperienceList().isEmpty()) {
            expScore = Math.min(15, 5 + resume.getExperienceList().size() * 5);
        }
        totalScore += expScore;
        checks.add(createCheck("Experience", expScore, 15));

        // Skills (15 points)
        int skillScore = 0;
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            skillScore = Math.min(15, 3 + resume.getSkills().size() * 2);
        }
        totalScore += skillScore;
        checks.add(createCheck("Skills", skillScore, 15));

        // Projects (10 points)
        int projectScore = 0;
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            projectScore = Math.min(10, 5 + resume.getProjects().size() * 5);
        }
        totalScore += projectScore;
        checks.add(createCheck("Projects", projectScore, 10));

        // Certifications (5 points)
        int certScore = 0;
        if (resume.getCertifications() != null && !resume.getCertifications().isEmpty()) {
            certScore = Math.min(5, resume.getCertifications().size() * 5);
        }
        totalScore += certScore;
        checks.add(createCheck("Certifications", certScore, 5));

        // Social Links (5 points)
        int socialScore = isNotEmpty(resume.getLinkedin()) ? 3 : 0;
        socialScore += isNotEmpty(resume.getGithub()) ? 2 : 0;
        totalScore += socialScore;
        checks.add(createCheck("Social Links", socialScore, 5));

        result.put("score", Math.min(100, totalScore));
        result.put("checks", checks);
        result.put("label", getScoreLabel(totalScore));
        return result;
    }

    private boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private Map<String, Object> createCheck(String name, int score, int max) {
        Map<String, Object> check = new HashMap<>();
        check.put("name", name);
        check.put("score", score);
        check.put("max", max);
        check.put("percentage", max > 0 ? (score * 100 / max) : 0);
        check.put("status", score >= max * 0.8 ? "complete" : score > 0 ? "partial" : "missing");
        return check;
    }

    private String getScoreLabel(int score) {
        if (score >= 90) return "Excellent";
        if (score >= 70) return "Good";
        if (score >= 50) return "Fair";
        if (score >= 30) return "Needs Improvement";
        return "Just Getting Started";
    }
}
