package com.resumeforge;

import com.resumeforge.dto.RegistrationRequest;
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

    private RegistrationRequest request(String fullName, String username, String email, String password) {
        RegistrationRequest r = new RegistrationRequest();
        r.setFullName(fullName);
        r.setUsername(username);
        r.setEmail(email);
        r.setPassword(password);
        r.setConfirmPassword(password);
        return r;
    }

    @BeforeEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() {
        User user = authService.register(request("John Doe", "johndoe", "john@example.com", "Password123"));
        assertNotNull(user);
        assertEquals("John Doe", user.getFullName());
        assertEquals("johndoe", user.getUsername());
        assertEquals("john@example.com", user.getEmail());
        assertNotNull(user.getPassword()); // Password should be hashed
        assertNotEquals("Password123", user.getPassword()); // Should not be plain text
    }

    @Test
    void testRegisterDuplicateUsername() {
        authService.register(request("John Doe", "johndoe", "john@example.com", "Password123"));
        assertThrows(UserAlreadyExistsException.class, () ->
            authService.register(request("Jane Doe", "johndoe", "jane@example.com", "Password456"))
        );
    }

    @Test
    void testRegisterDuplicateEmail() {
        authService.register(request("John Doe", "johndoe", "john@example.com", "Password123"));
        assertThrows(UserAlreadyExistsException.class, () ->
            authService.register(request("Jane Doe", "janedoe", "john@example.com", "Password456"))
        );
    }

    @Test
    void testRegisteredPasswordIsHashed() {
        authService.register(request("John Doe", "johndoe", "john@example.com", "Password123"));
        User user = userRepository.findByUsername("johndoe").orElse(null);
        assertNotNull(user);
        assertTrue(user.getPassword().startsWith("$2")); // BCrypt hash format
        assertNotEquals("Password123", user.getPassword());
    }

    @Test
    void testRegisterRejectsWeakPassword() {
        assertThrows(Exception.class, () ->
            authService.register(request("John Doe", "weakpw", "weak@example.com", "abc"))
        );
    }
}
