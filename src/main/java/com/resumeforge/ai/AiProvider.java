package com.resumeforge.ai;

import java.util.Map;

/**
 * Provider abstraction for AI resume analysis.
 * Implementations: OpenRouterAiProvider, RuleBasedAiProvider
 */
public interface AiProvider {

    /** Analyze job description and extract structured info */
    Map<String, Object> analyzeJobDescription(String jobDescription);

    /** Analyze resume text and extract structured info */
    Map<String, Object> analyzeResume(String resumeText);

    /** Match a resume against a job description */
    Map<String, Object> matchResumeToJob(String resumeText, String jobDescription);

    /** Generate a tailored professional summary */
    String generateSummary(String resumeText, String jobDescription, String targetTitle);

    /** Improve a single experience entry */
    String improveExperience(String experienceText, String jobDescription);

    /** Improve a project description */
    String improveProject(String projectText, String jobDescription);

    /** Get career recommendations based on resume */
    Map<String, Object> careerRecommendation(String resumeText);

    /** Generate skill recommendations for a job */
    Map<String, Object> recommendSkills(String resumeText, String jobDescription);

    /** Get provider name */
    String getProviderName();
}
