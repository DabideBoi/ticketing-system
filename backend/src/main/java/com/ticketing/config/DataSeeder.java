package com.ticketing.config;

import com.ticketing.entity.Role;
import com.ticketing.entity.User;
import com.ticketing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEV_PASSWORD = "password123";

    @Override
    public void run(String... args) {
        seed("requestor@ticketing.local", Role.REQUESTOR, "Riley Requestor");
        seed("approver@ticketing.local", Role.APPROVER, "Avery Approver");
        seed("assigner@ticketing.local", Role.ASSIGNER, "Sam Assigner");
        seed("assignee@ticketing.local", Role.ASSIGNEE, "Jamie Assignee");
        seed("admin@ticketing.local", Role.ADMIN, "Alex Admin");
    }

    private void seed(String email, Role role, String fullName) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(DEV_PASSWORD))
                .role(role)
                .fullName(fullName)
                .build();
        userRepository.save(user);
    }
}
