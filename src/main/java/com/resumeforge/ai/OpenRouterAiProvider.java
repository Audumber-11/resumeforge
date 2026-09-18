package com.resumeforge.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

/**
 * OpenRouter AI Provider — calls OpenRouter API with configurable free models.
 * Falls back to RuleBasedAiProvider on any failure.
 */
@Component
public class OpenRouterAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterAiProvider.class);

    @Value("${resumeforge.ai.api-key:}")
    private String apiKey;

    @Value("${resumeforge.ai.model:openrouter/free}")
    private String model;

    @Value("${resumeforge.ai.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${resumeforge.ai.max-tokens:2000}")
    private int maxTokens;

    @Value("${resumeforge.ai.temperature:0.3}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
        restTemplate = new RestTemplate(factory);
    }

    private boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    private String callChat(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            log.warn("OpenRouter API key not configured — returning null");
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://resumeforge.local");
            headers.set("X-Title", "ResumeForge");

            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("temperature", temperature);

            ArrayNode messages = body.putArray("messages");
            ObjectNode sys = messages.addObject();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            ObjectNode usr = messages.addObject();
            usr.put("role", "user");
            usr.put("content", userPrompt);

            HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/chat/completions", HttpMethod.POST, entity, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode msg = root.path("choices").path(0).path("message");
            String content = msg.path("content").asText(null);
            if (content == null || content.isBlank()) {
                // Some models return reasoning-only responses; fall back to reasoning text.
                content = msg.path("reasoning").asText(null);
                if (content == null || content.isBlank()) {
                    log.warn("OpenRouter returned empty content for model {}", model);
                    return null;
                }
            }
            return content;

        } catch (Exception e) {
            log.error("OpenRouter API call failed: {}", e.getMessage());
            return null;
        }
    }

    private String extractJson(String response) {
        if (response == null) return null;
        // Try to extract JSON from markdown code blocks
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }

    @Override
    public Map<String, Object> analyzeJobDescription(String jobDescription) {
        String system = "You are an expert job description analyzer. Extract structured information from job descriptions. Return ONLY valid JSON with these fields: { \"jobTitle\": \"...\", \"requiredSkills\": [...], \"preferredSkills\": [...], \"technologies\": [...], \"responsibilities\": [...], \"experienceRequired\": \"...\", \"educationRequired\": \"...\", \"keywords\": [...], \"softSkills\": [...] }. Never include anything outside the JSON object.";

        String response = callChat(system, "Analyze this job description:\n\n" + jobDescription);
        String json = extractJson(response);
        if (json != null) {
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse AI job analysis: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> analyzeResume(String resumeText) {
        String system = "You are an expert resume analyzer. Extract structured information from resumes. Return ONLY valid JSON with these fields: { \"personalInfo\": { \"name\": \"...\", \"title\": \"...\", \"email\": \"...\", \"phone\": \"...\" }, \"summary\": \"...\", \"skills\": [...], \"experience\": [...], \"education\": [...], \"projects\": [...], \"certifications\": [...], \"achievements\": [...], \"languages\": [...] }. Never include anything outside the JSON object.";

        String response = callChat(system, "Analyze this resume:\n\n" + resumeText);
        String json = extractJson(response);
        if (json != null) {
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse AI resume analysis: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> matchResumeToJob(String resumeText, String jobDescription) {
        String system = "You are an expert resume-job matching analyzer. Compare a resume against a job description. Return ONLY valid JSON with: { \"overallScore\": 82, \"skillsMatch\": 88, \"keywordMatch\": 79, \"experienceMatch\": 76, \"educationMatch\": 95, \"matchedSkills\": [...], \"missingSkills\": [...], \"matchedKeywords\": [...], \"missingKeywords\": [...], \"suggestions\": [...] }. Never include anything outside the JSON object.";

        String response = callChat(system, "RESUME:\n" + resumeText + "\n\nJOB DESCRIPTION:\n" + jobDescription);
        String json = extractJson(response);
        if (json != null) {
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse AI match result: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public String generateSummary(String resumeText, String jobDescription, String targetTitle) {
        String system = "You are a professional resume writer. Generate a 2-4 sentence professional summary tailored for the specific job. Only use facts from the provided resume. Do NOT invent experience, skills, or qualifications. Return ONLY the summary text, no JSON, no explanation.";

        String prompt = "Resume:\n" + resumeText + "\n\nJob Description:\n" + jobDescription + "\n\nTarget Title: " + (targetTitle != null ? targetTitle : "Not specified") + "\n\nGenerate a tailored professional summary:";
        String response = callChat(system, prompt);
        if (response != null) {
            return response.trim().replaceAll("^\"|\"$", "");
        }
        return null;
    }

    @Override
    public String improveExperience(String experienceText, String jobDescription) {
        String system = "You are a professional resume writer. Improve experience descriptions using strong action verbs and measurable outcomes. Do NOT invent new experience, companies, roles, or dates. Only improve the wording of what is provided. Return ONLY the improved text, no JSON.";

        String response = callChat(system, "Original experience:\n" + experienceText + "\n\nJob context:\n" + (jobDescription != null ? jobDescription : "General professional") + "\n\nImprove this experience description:");
        if (response != null) {
            return response.trim().replaceAll("^\"|\"$", "");
        }
        return null;
    }

    @Override
    public String improveProject(String projectText, String jobDescription) {
        String system = "You are a professional resume writer. Improve project descriptions to be more professional and impactful. Do NOT invent technologies, features, or outcomes not mentioned. Return ONLY the improved text, no JSON.";

        String response = callChat(system, "Original project:\n" + projectText + "\n\nJob context:\n" + (jobDescription != null ? jobDescription : "General professional") + "\n\nImprove this project description:");
        if (response != null) {
            return response.trim().replaceAll("^\"|\"$", "");
        }
        return null;
    }

    @Override
    public Map<String, Object> careerRecommendation(String resumeText) {
        String system = "You are a career advisor. Based on a resume, recommend suitable roles with match percentages. Return ONLY valid JSON: { \"recommendations\": [{ \"role\": \"...\", \"matchPercent\": 92, \"reason\": \"...\" }] }. Maximum 5 recommendations. Never include anything outside the JSON object.";

        String response = callChat(system, "Analyze this resume and recommend suitable career roles:\n\n" + resumeText);
        String json = extractJson(response);
        if (json != null) {
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse career recommendations: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> recommendSkills(String resumeText, String jobDescription) {
        String system = "You are a skill advisor. Compare resume skills against job requirements. Return ONLY valid JSON: { \"alreadyHave\": [...], \"recommended\": [...], \"niceToHave\": [...] }. Label recommended skills as skills to consider learning. Never include anything outside the JSON object.";

        String response = callChat(system, "RESUME:\n" + resumeText + "\n\nJOB:\n" + jobDescription + "\n\nRecommend skills:");
        String json = extractJson(response);
        if (json != null) {
            try {
                return mapper.readValue(json, Map.class);
            } catch (Exception e) {
                log.warn("Failed to parse skill recommendations: {}", e.getMessage());
            }
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "OpenRouter (" + model + ")";
    }
}
