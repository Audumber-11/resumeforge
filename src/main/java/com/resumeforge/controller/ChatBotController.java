package com.resumeforge.controller;

import com.resumeforge.entity.AiConversation;
import com.resumeforge.entity.AiMessage;
import com.resumeforge.security.CustomUserDetails;
import com.resumeforge.service.ChatService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
public class ChatBotController {

    @Value("${resumeforge.ai.api-key:}")
    private String apiKey;

    @Value("${resumeforge.ai.model:nvidia/nemotron-3-super-120b-a12b:free}")
    private String model;

    private final ChatService chatService;

    public ChatBotController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Chat endpoint with conversation persistence.
     * Pass conversationId: null to start a new conversation — the response includes the id
     * so the client can keep sending it and reload history later.
     */
    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> body,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        String message = body.getOrDefault("message", "");
        String resumeContext = body.getOrDefault("resumeContext", "");
        String conversationIdStr = body.get("conversationId");

        Map<String, Object> response = new HashMap<>();

        if (apiKey == null || apiKey.isBlank()) {
            response.put("success", false);
            response.put("error", "AI API key not configured. Set OPENROUTER_API_KEY environment variable.");
            return response;
        }

        try {
            // ── Load or create the persisted conversation ──
            AiConversation conversation = null;
            Long conversationId = null;
            if (conversationIdStr != null && !conversationIdStr.isBlank()) {
                try {
                    conversationId = Long.parseLong(conversationIdStr);
                    conversation = chatService.getConversation(conversationId);
                    // Only reuse conversations owned by the current user
                    if (conversation != null && conversation.getUser() != null
                            && !conversation.getUser().getId().equals(userDetails.getUser().getId())) {
                        conversation = null;
                        conversationId = null;
                    }
                } catch (NumberFormatException ignored) { }
            }
            if (conversation == null) {
                String title = message.length() > 60 ? message.substring(0, 60) + "..." : message;
                conversation = chatService.createConversation(userDetails.getUser().getId(), userDetails.getUser(), title);
                conversationId = conversation.getId();
            }
            response.put("conversationId", conversationId);

            // ── Build history so the AI remembers earlier turns ──
            List<AiMessage> history = chatService.getConversationMessages(conversationId);
            List<Map<String, String>> historyMsgs = new ArrayList<>();
            int from = Math.max(0, history.size() - 10); // last 10 turns
            for (int i = from; i < history.size(); i++) {
                AiMessage m = history.get(i);
                historyMsgs.add(Map.of("role", "user".equals(m.getRole()) ? "user" : "assistant",
                        "content", m.getContent()));
            }

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
            factory.setReadTimeout((int) Duration.ofSeconds(60).toMillis());
            RestTemplate restTemplate = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            String systemPrompt = "You are ResumeCraft AI, a professional resume building assistant. " +
                "You help users write better resumes, improve their professional summaries, " +
                "suggest skills, review experience descriptions, and provide career advice. " +
                "Be concise, professional, and actionable. Format responses with markdown.";

            if (!resumeContext.isBlank()) {
                systemPrompt += "\n\nUser's current resume data:\n" + resumeContext;
            }

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.addAll(historyMsgs);
            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> apiResponse = restTemplate.exchange(
                "https://openrouter.ai/api/v1/chat/completions",
                HttpMethod.POST,
                request,
                Map.class
            );

            String reply = null;
            if (apiResponse.getBody() != null && apiResponse.getBody().containsKey("choices")) {
                List<Map> choices = (List<Map>) apiResponse.getBody().get("choices");
                if (!choices.isEmpty()) {
                    Map choice = choices.get(0);
                    Map messageObj = (Map) choice.get("message");
                    reply = (String) messageObj.get("content");
                    if ((reply == null || reply.isBlank()) && messageObj.get("reasoning") != null) {
                        reply = String.valueOf(messageObj.get("reasoning"));
                    }
                }
            }

            if (reply != null && !reply.isBlank()) {
                // ── Persist both messages ──
                chatService.addMessage(conversationId, "user", message, "chat");
                chatService.addMessage(conversationId, "assistant", reply, "chat");
                response.put("success", true);
                response.put("reply", reply);
                return response;
            }

            response.put("success", false);
            response.put("error", "No response from AI.");

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "AI error: " + e.getMessage());
        }

        return response;
    }

    /** Conversation history for the current user (sidebar lists / reload). */
    @GetMapping("/conversations")
    public Map<String, Object> myConversations(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("conversations", chatService.getUserConversations(userDetails.getUser().getId()));
        return result;
    }
}
