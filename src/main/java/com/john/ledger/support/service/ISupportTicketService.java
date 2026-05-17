package com.john.ledger.support.service;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.support.dto.SupportTicketRequest;
import com.john.ledger.support.dto.SupportTicketResponse;

import java.util.List;
import java.util.UUID;

public interface ISupportTicketService {
    ServiceResponse<Void> raiseTicket(SupportTicketRequest request);
    ServiceResponse<List<SupportTicketResponse>> getMyTickets(UUID userId);
}
