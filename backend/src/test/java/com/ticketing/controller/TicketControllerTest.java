package com.ticketing.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String requestorToken;
    private String approverToken;
    private String assignerToken;
    private String assigneeToken;

    @BeforeEach
    void loginAllRoles() throws Exception {
        requestorToken = login("requestor@ticketing.local");
        approverToken = login("approver@ticketing.local");
        assignerToken = login("assigner@ticketing.local");
        assigneeToken = login("assignee@ticketing.local");
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"password\":\"password123\"}".formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void requestor_cannotApproveOrAssign() throws Exception {
        String ticketId = createDbFixTicket();

        mockMvc.perform(post("/api/tickets/" + ticketId + "/approve")
                        .header("Authorization", "Bearer " + requestorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void dbFixTicket_rejection_terminatesFlowAsClose() throws Exception {
        String ticketId = createDbFixTicket();

        mockMvc.perform(post("/api/tickets/" + ticketId + "/approve")
                        .header("Authorization", "Bearer " + approverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":false,\"remarks\":\"not needed\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSE"));
    }

    @Test
    void fullDbFixLifecycle_reachesClose() throws Exception {
        String ticketId = createDbFixTicket();

        mockMvc.perform(post("/api/tickets/" + ticketId + "/approve")
                        .header("Authorization", "Bearer " + approverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approved\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOR_ASSIGNMENT"));

        MvcResult usersResult = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/users")
                                .param("role", "ASSIGNEE")
                                .header("Authorization", "Bearer " + assignerToken))
                .andExpect(status().isOk())
                .andReturn();
        String assigneeId = objectMapper.readTree(usersResult.getResponse().getContentAsString()).get(0).get("id").asText();

        mockMvc.perform(post("/api/tickets/" + ticketId + "/assign")
                        .header("Authorization", "Bearer " + assignerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assigneeId\":\"%s\"}".formatted(assigneeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ASSIGNED"));

        mockMvc.perform(put("/api/tickets/" + ticketId + "/status")
                        .header("Authorization", "Bearer " + assigneeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ONGOING\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONGOING"));

        mockMvc.perform(put("/api/tickets/" + ticketId + "/status")
                        .header("Authorization", "Bearer " + assigneeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"FOR_CLOSE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOR_CLOSE"));

        mockMvc.perform(put("/api/tickets/" + ticketId + "/status")
                        .header("Authorization", "Bearer " + requestorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CLOSE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSE"));
    }

    private String createDbFixTicket() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + requestorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Fix prod DB","description":"desc","type":"DB_FIX"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FOR_APPROVAL"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
    }
}
