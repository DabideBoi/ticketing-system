package com.ticketing.repository;

import com.ticketing.entity.Ticket;
import com.ticketing.entity.TicketStatus;
import com.ticketing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByRequestor(User requestor);
    List<Ticket> findByStatus(TicketStatus status);
    List<Ticket> findByStatusIn(List<TicketStatus> statuses);
    List<Ticket> findByAssigneeAndStatusIn(User assignee, List<TicketStatus> statuses);
    long countByStatus(TicketStatus status);
}
