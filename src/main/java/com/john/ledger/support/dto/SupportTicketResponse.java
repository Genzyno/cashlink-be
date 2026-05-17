package com.john.ledger.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {
    private UUID id;
    private String ticketNumber;
    private String subject;
    private String description;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
