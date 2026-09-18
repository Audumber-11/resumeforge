package com.resumeforge.service;

import com.resumeforge.dto.RegistrationRequest;
import com.resumeforge.entity.User;
import com.resumeforge.exception.UserAlreadyExistsException;
import com.resumeforge.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9._-]{3,30}$";
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    public User register(RegistrationRequest request) {
        validateRegistration(request);
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists. Please choose another.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered. Please use a different email.");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new UserAlreadyExistsException("Passwords do not match.");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /** Server-side validation — mirrors the client-side checks so the API cannot be bypassed. */
    private void validateRegistration(RegistrationRequest request) {
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new UserAlreadyExistsException("Full name must be between 2 and 100 characters.");
        }
        if (!username.matches(USERNAME_PATTERN)) {
            throw new UserAlreadyExistsException("Username must be 3-30 characters (letters, numbers, dot, dash, underscore only).");
        }
        if (!email.matches(EMAIL_PATTERN)) {
            throw new UserAlreadyExistsException("Please enter a valid email address.");
        }
        if (password.length() < 8) {
            throw new UserAlreadyExistsException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new UserAlreadyExistsException("Password must contain both letters and numbers.");
        }
    }
}
