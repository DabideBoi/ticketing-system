package com.ticketing.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_withSeededAdminCredentials_returnsTokenAndUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@ticketing.local","password":"password123"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    void login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@ticketing.local","password":"wrong-password"}"""))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void accessingProtectedEndpoint_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void accessingProtectedEndpoint_withInvalidToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/tickets").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().is4xxClientError());
    }
}
