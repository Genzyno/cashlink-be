package com.john.ledger.support.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketRequest {
    private String name;
    private String email;
    private String type;
    private String subject;
    private String description;
    private UUID businessId;
    private UUID userId;
}
