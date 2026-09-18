package com.resumeforge.service;

import com.resumeforge.entity.AiConversation;
import com.resumeforge.entity.AiMessage;
import com.resumeforge.entity.User;
import com.resumeforge.repository.AiConversationRepository;
import com.resumeforge.repository.AiMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ChatService {

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public ChatService(AiConversationRepository conversationRepository,
                       AiMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional(readOnly = true)
    public List<AiConversation> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public AiConversation getConversation(Long id) {
        return conversationRepository.findById(id).orElse(null);
    }

    public AiConversation createConversation(Long userId, User user, String title) {
        AiConversation conv = new AiConversation(user, title);
        conv.setUser(user);
        return conversationRepository.save(conv);
    }

    public void deleteConversation(Long id) {
        conversationRepository.deleteById(id);
    }

    public AiMessage addMessage(Long conversationId, String role, String content, String action) {
        AiConversation conv = conversationRepository.findById(conversationId).orElse(null);
        if (conv == null) return null;

        AiMessage msg = new AiMessage(role, content, action);
        conv.addMessage(msg);
        messageRepository.save(msg);
        conversationRepository.save(conv);
        return msg;
    }

    @Transactional(readOnly = true)
    public List<AiMessage> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
