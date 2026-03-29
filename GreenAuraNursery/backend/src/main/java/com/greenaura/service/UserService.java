package com.greenaura.service;

import com.greenaura.model.User;
import org.springframework.stereotype.Service;
import com.greenaura.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }

        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return null;
        }

        // Backward compatibility for existing plaintext rows, while preferring BCrypt.
        boolean passwordMatches = storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$")
                ? passwordEncoder.matches(password, storedPassword)
                : storedPassword.equals(password);

        return passwordMatches ? user : null;
    }

    public boolean emailExists(String email) {
        return userRepository.emailExists(email);
    }

    public boolean registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.register(user);
    }
}
