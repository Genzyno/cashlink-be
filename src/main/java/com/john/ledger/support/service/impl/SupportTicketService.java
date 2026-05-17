package com.john.ledger.support.service.impl;

import com.john.ledger.common.util.ResponseMessages;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.support.dto.SupportTicketRequest;
import com.john.ledger.support.dto.SupportTicketResponse;
import com.john.ledger.support.entity.SupportTicketEntity;
import com.john.ledger.support.repository.SupportTicketRepository;
import com.john.ledger.support.service.ISupportTicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportTicketService implements ISupportTicketService {

    private final SupportTicketRepository supportTicketRepository;

    @Override
    public ServiceResponse<Void> raiseTicket(SupportTicketRequest request) {
        try {
            long count = supportTicketRepository.count();
            String ticketNumber = String.format("TKT-%03d", count + 1);

            SupportTicketEntity entity = SupportTicketEntity.builder()
                    .ticketNumber(ticketNumber)
                    .name(request.getName())
                    .email(request.getEmail())
                    .type(request.getType())
                    .subject(request.getSubject())
                    .description(request.getDescription())
                    .businessId(request.getBusinessId())
                    .userId(request.getUserId())
                    .status("OPEN")
                    .build();

            supportTicketRepository.save(entity);
            return ServiceResponse.successResponse(201, "Ticket raised successfully", null);
        } catch (Exception e) {
            log.error("Error raising support ticket: ", e);
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<List<SupportTicketResponse>> getMyTickets(UUID userId) {
        try {
            List<SupportTicketEntity> entities = supportTicketRepository.findByUserIdOrderByCreatedTimeDesc(userId);
            
            List<SupportTicketResponse> responses = entities.stream()
                    .map(entity -> SupportTicketResponse.builder()
                            .id(entity.getId())
                            .ticketNumber(entity.getTicketNumber())
                            .subject(entity.getSubject())
                            .description(entity.getDescription())
                            .type(entity.getType())
                            .status(entity.getStatus())
                            .createdAt(entity.getCreatedTime())
                            .build())
                    .collect(Collectors.toList());

            return ServiceResponse.successResponse(200, "Tickets fetched successfully", responses);
        } catch (Exception e) {
            log.error("Error fetching support tickets: ", e);
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }
}
