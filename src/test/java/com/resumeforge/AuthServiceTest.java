package com.resumeforge;

import com.resumeforge.entity.User;
import com.resumeforge.exception.UserAlreadyExistsException;
import com.resumeforge.repository.UserRepository;
import com.resumeforge.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() {
        User user = authService.register("John Doe", "johndoe", "john@example.com", "password123");
        assertNotNull(user);
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getPassword()); // Password should be hashed
        assertNotEquals("password123", user.getPassword()); // Should not be plain text
    }

    @Test
    void testRegisterDuplicateUsername() {
        authService.register("John Doe", "johndoe", "john@example.com", "password123");
        assertThrows(UserAlreadyExistsException.class, () ->
            authService.register("Jane Doe", "johndoe", "jane@example.com", "password456")
        );
    }

    @Test
    void testRegisterDuplicateEmail() {
        authService.register("John Doe", "johndoe", "john@example.com", "password123");
        assertThrows(UserAlreadyExistsException.class, () ->
            authService.register("Jane Doe", "janedoe", "john@example.com", "password456")
        );
    }

    @Test
    void testLoginSuccess() {
        authService.register("John Doe", "johndoe", "john@example.com", "password123");
        User user = authService.authenticate("johndoe", "password123");
        assertNotNull(user);
        assertEquals("johndoe", user.getUsername());
    }

    @Test
    void testLoginWrongPassword() {
        authService.register("John Doe", "johndoe", "john@example.com", "password123");
        assertThrows(Exception.class, () ->
            authService.authenticate("johndoe", "wrongpassword")
        );
    }

    @Test
    void testLoginNonExistentUser() {
        assertThrows(Exception.class, () ->
            authService.authenticate("nonexistent", "password123")
        );
    }
}
