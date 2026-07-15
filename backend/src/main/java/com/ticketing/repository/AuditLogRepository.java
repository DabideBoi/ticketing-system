package com.ticketing.repository;

import com.ticketing.entity.AuditLog;
import com.ticketing.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByTicketOrderByTimestampAsc(Ticket ticket);
    List<AuditLog> findTop20ByOrderByTimestampDesc();
    List<AuditLog> findTop15ByTicketInOrderByTimestampDesc(List<Ticket> tickets);
}
