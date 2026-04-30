package com.john.ledger.entry.dto.request;

import com.john.ledger.common.enums.Status;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModeUpdateRequest {

    private java.util.UUID id;
    private String paymentModeName;
    private Status status;
}
