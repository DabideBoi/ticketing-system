package com.ticketing.service;

import com.ticketing.dto.LoginRequest;
import com.ticketing.dto.LoginResponse;
import com.ticketing.dto.UserDto;
import com.ticketing.security.JwtService;
import com.ticketing.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        UsernamePasswordAuthenticationToken authRequest =
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        UserPrincipal principal = (UserPrincipal) authenticationManager.authenticate(authRequest).getPrincipal();

        String token = jwtService.generateToken(principal);
        UserDto userDto = UserDto.fromEntity(principal.getUser());

        return LoginResponse.builder()
                .token(token)
                .user(userDto)
                .build();
    }
}
