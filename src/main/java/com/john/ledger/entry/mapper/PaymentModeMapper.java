package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.PaymentModeSaveRequest;
import com.john.ledger.entry.dto.request.PaymentModeUpdateRequest;
import com.john.ledger.entry.dto.response.PaymentModeResponse;
import com.john.ledger.entry.entity.PaymentModeEntity;

public class PaymentModeMapper {

    public static PaymentModeEntity toSaveEntity(PaymentModeSaveRequest request) {

        PaymentModeEntity entity = new PaymentModeEntity();

        entity.setBusinessId(request.getBusinessId());
        entity.setPaymentModeName(request.getPaymentModeName());
        entity.setStatus(request.getStatus());

        return entity;
    }

    public static void toUpdateEntity(PaymentModeUpdateRequest request, PaymentModeEntity entity) {

        entity.setPaymentModeName(request.getPaymentModeName());
        entity.setStatus(request.getStatus());
    }

    public static PaymentModeResponse toResponse(PaymentModeEntity entity) {

        PaymentModeResponse response = new PaymentModeResponse();

        response.setId(entity.getId());
        response.setBusinessId(entity.getBusinessId());
        response.setPaymentModeName(entity.getPaymentModeName());
        response.setStatus(entity.getStatus());
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());

        return response;
    }
}
