package com.john.ledger.entry.dto.response;

import com.john.ledger.common.enums.Status;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModeResponse {

    private java.util.UUID id;
    private java.util.UUID businessId;
    private String paymentModeName;
    private Status status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
