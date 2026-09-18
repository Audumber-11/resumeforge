package com.resumeforge.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * AI Manager — delegates to the configured provider (OpenRouter or Rule-Based fallback).
 * All AI calls go through this class.
 */
@Component
public class AiManager {

    private static final Logger log = LoggerFactory.getLogger(AiManager.class);

    private final OpenRouterAiProvider openRouter;
    private final RuleBasedAiProvider ruleBased;

    @Value("${resumeforge.ai.provider:openrouter}")
    private String configuredProvider;

    public AiManager(OpenRouterAiProvider openRouter, RuleBasedAiProvider ruleBased) {
        this.openRouter = openRouter;
        this.ruleBased = ruleBased;
    }

    private AiProvider getProvider() {
        if ("openrouter".equalsIgnoreCase(configuredProvider)) {
            return openRouter;
        }
        return ruleBased;
    }

    /** Get the active provider for status display */
    public AiProvider getActiveProvider() {
        return getProvider();
    }

    /** Get rule-based provider for fallback operations */
    public RuleBasedAiProvider getRuleBased() {
        return ruleBased;
    }

    // ── Delegate Methods ──────────────────────────────────────

    public Map<String, Object> analyzeJobDescription(String jobDescription) {
        try {
            Map<String, Object> result = getProvider().analyzeJobDescription(jobDescription);
            if (result != null) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for job analysis, using fallback: {}", e.getMessage());
        }
        return ruleBased.analyzeJobDescription(jobDescription);
    }

    public Map<String, Object> analyzeResume(String resumeText) {
        try {
            Map<String, Object> result = getProvider().analyzeResume(resumeText);
            if (result != null) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for resume analysis, using fallback: {}", e.getMessage());
        }
        return ruleBased.analyzeResume(resumeText);
    }

    public Map<String, Object> matchResumeToJob(String resumeText, String jobDescription) {
        try {
            Map<String, Object> result = getProvider().matchResumeToJob(resumeText, jobDescription);
            if (result != null) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for matching, using fallback: {}", e.getMessage());
        }
        return ruleBased.matchResumeToJob(resumeText, jobDescription);
    }

    public String generateSummary(String resumeText, String jobDescription, String targetTitle) {
        try {
            String result = getProvider().generateSummary(resumeText, jobDescription, targetTitle);
            if (result != null && !result.isBlank()) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for summary, using fallback: {}", e.getMessage());
        }
        return ruleBased.generateSummary(resumeText, jobDescription, targetTitle);
    }

    public String improveExperience(String experienceText, String jobDescription) {
        try {
            String result = getProvider().improveExperience(experienceText, jobDescription);
            if (result != null && !result.isBlank()) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for experience, using fallback: {}", e.getMessage());
        }
        return ruleBased.improveExperience(experienceText, jobDescription);
    }

    public String improveProject(String projectText, String jobDescription) {
        try {
            String result = getProvider().improveProject(projectText, jobDescription);
            if (result != null && !result.isBlank()) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for project, using fallback: {}", e.getMessage());
        }
        return ruleBased.improveProject(projectText, jobDescription);
    }

    public Map<String, Object> careerRecommendation(String resumeText) {
        try {
            Map<String, Object> result = getProvider().careerRecommendation(resumeText);
            if (result != null) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for career, using fallback: {}", e.getMessage());
        }
        return ruleBased.careerRecommendation(resumeText);
    }

    public Map<String, Object> recommendSkills(String resumeText, String jobDescription) {
        try {
            Map<String, Object> result = getProvider().recommendSkills(resumeText, jobDescription);
            if (result != null) return result;
        } catch (Exception e) {
            log.warn("Primary AI failed for skills, using fallback: {}", e.getMessage());
        }
        return ruleBased.recommendSkills(resumeText, jobDescription);
    }
}
