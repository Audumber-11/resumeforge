package com.resumeforge;

import com.resumeforge.entity.AiConversation;
import com.resumeforge.entity.AiMessage;
import com.resumeforge.entity.User;
import com.resumeforge.repository.UserRepository;
import com.resumeforge.service.AuthService;
import com.resumeforge.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        testUser = authService.register("Test User", "testuser", "test@example.com", "password123");
    }

    @Test
    void testCreateConversation() {
        AiConversation conv = chatService.createConversation(testUser.getId(), testUser, "Test Chat");
        assertNotNull(conv);
        assertEquals("Test Chat", conv.getTitle());
        assertEquals(testUser.getId(), conv.getUser().getId());
    }

    @Test
    void testGetConversation() {
        AiConversation conv = chatService.createConversation(testUser.getId(), testUser, "Test Chat");
        AiConversation fetched = chatService.getConversation(conv.getId());
        assertNotNull(fetched);
        assertEquals("Test Chat", fetched.getTitle());
    }

    @Test
    void testGetConversationNotFound() {
        AiConversation result = chatService.getConversation(999L);
        assertNull(result);
    }

    @Test
    void testAddMessage() {
        AiConversation conv = chatService.createConversation(testUser.getId(), testUser, "Test Chat");

        AiMessage msg1 = chatService.addMessage(conv.getId(), "user", "Hello, AI!", "chat");
        assertNotNull(msg1);
        assertEquals("user", msg1.getRole());
        assertEquals("Hello, AI!", msg1.getContent());

        AiMessage msg2 = chatService.addMessage(conv.getId(), "assistant", "Hello! How can I help?", "chat");
        assertNotNull(msg2);
        assertEquals("assistant", msg2.getRole());
    }

    @Test
    void testGetMessages() {
        AiConversation conv = chatService.createConversation(testUser.getId(), testUser, "Test Chat");

        chatService.addMessage(conv.getId(), "user", "Message 1", "chat");
        chatService.addMessage(conv.getId(), "assistant", "Response 1", "chat");
        chatService.addMessage(conv.getId(), "user", "Message 2", "chat");

        List<AiMessage> messages = chatService.getConversationMessages(conv.getId());
        assertEquals(3, messages.size());
        assertEquals("Message 1", messages.get(0).getContent());
        assertEquals("Response 1", messages.get(1).getContent());
        assertEquals("Message 2", messages.get(2).getContent());
    }

    @Test
    void testDeleteConversation() {
        AiConversation conv = chatService.createConversation(testUser.getId(), testUser, "To Delete");
        Long id = conv.getId();

        chatService.deleteConversation(id);

        AiConversation result = chatService.getConversation(id);
        assertNull(result);
    }

    @Test
    void testGetUserConversations() {
        chatService.createConversation(testUser.getId(), testUser, "Chat 1");
        chatService.createConversation(testUser.getId(), testUser, "Chat 2");
        chatService.createConversation(testUser.getId(), testUser, "Chat 3");

        List<AiConversation> convs = chatService.getUserConversations(testUser.getId());
        assertEquals(3, convs.size());
    }
}
