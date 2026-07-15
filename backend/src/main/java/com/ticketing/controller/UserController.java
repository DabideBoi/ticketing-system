package com.ticketing.controller;

import com.ticketing.dto.UserDto;
import com.ticketing.entity.Role;
import com.ticketing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ASSIGNER','APPROVER','ADMIN')")
    public ResponseEntity<List<UserDto>> listByRole(@RequestParam Role role) {
        List<UserDto> users = userRepository.findByRole(role).stream()
                .map(UserDto::fromEntity)
                .toList();
        return ResponseEntity.ok(users);
    }
}
