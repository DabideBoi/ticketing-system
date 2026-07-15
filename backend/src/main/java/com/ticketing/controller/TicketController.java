package com.ticketing.controller;

import com.ticketing.dto.ApproveRequest;
import com.ticketing.dto.AssignRequest;
import com.ticketing.dto.CreateTicketRequest;
import com.ticketing.dto.StatusUpdateRequest;
import com.ticketing.dto.TicketResponse;
import com.ticketing.security.UserPrincipal;
import com.ticketing.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('REQUESTOR','ADMIN')")
    public ResponseEntity<TicketResponse> create(@Valid @RequestBody CreateTicketRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ticketService.createTicket(request, principal.getUser()));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> list(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ticketService.getTicketsForUser(principal.getUser()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('APPROVER','ADMIN')")
    public ResponseEntity<TicketResponse> approve(@PathVariable UUID id,
                                                   @Valid @RequestBody ApproveRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ticketService.approve(id, request, principal.getUser()));
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ASSIGNER','APPROVER','ADMIN')")
    public ResponseEntity<TicketResponse> assign(@PathVariable UUID id,
                                                  @Valid @RequestBody AssignRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ticketService.assign(id, request, principal.getUser()));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable UUID id,
                                                        @Valid @RequestBody StatusUpdateRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ticketService.updateStatus(id, request, principal.getUser()));
    }
}
