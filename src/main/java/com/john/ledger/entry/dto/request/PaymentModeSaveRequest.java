package com.john.ledger.entry.dto.request;

import com.john.ledger.common.enums.Status;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentModeSaveRequest {

    private java.util.UUID businessId;
    private String paymentModeName;
    private Status status;
}
