package com.john.ledger.support.controller;

import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.support.dto.SupportTicketRequest;
import com.john.ledger.support.dto.SupportTicketResponse;
import com.john.ledger.support.service.ISupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportTicketController {

    private final ISupportTicketService supportTicketService;

    @PostMapping("/raise-ticket")
    public ResponseEntity<ServiceResponse<Void>> raiseTicket(@RequestBody SupportTicketRequest request) {
        ServiceResponse<Void> response = supportTicketService.raiseTicket(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<ServiceResponse<List<SupportTicketResponse>>> getMyTickets(
            @RequestParam UUID userId) {
        ServiceResponse<List<SupportTicketResponse>> response = supportTicketService.getMyTickets(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
