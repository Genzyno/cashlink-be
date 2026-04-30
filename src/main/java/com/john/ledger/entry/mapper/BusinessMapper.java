package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.BusinessSaveRequest;
import com.john.ledger.entry.dto.request.BusinessUpdateRequest;
import com.john.ledger.entry.dto.response.BusinessResponse;
import com.john.ledger.entry.entity.BusinessEntity;

public class BusinessMapper {

    public static BusinessEntity toSaveEntity(BusinessSaveRequest dto) {

        BusinessEntity entity = new BusinessEntity();
        entity.setBusinessName(dto.getBusinessName());
        entity.setCurrency(dto.getCurrency());
        entity.setFinancialYear(dto.getFinancialYear());

        return entity;
    }

    public static void toUpdateEntity(BusinessEntity entity, BusinessUpdateRequest dto) {

        entity.setBusinessName(dto.getBusinessName());
        entity.setCurrency(dto.getCurrency());
        entity.setFinancialYear(dto.getFinancialYear());
    }


    public static BusinessResponse toResponse(BusinessEntity entity) {

        BusinessResponse dto = new BusinessResponse();
        dto.setId(entity.getId());
        dto.setBusinessName(entity.getBusinessName());
        dto.setCurrency(entity.getCurrency());
        dto.setFinancialYear(entity.getFinancialYear());
        dto.setBusinessType(entity.getBusinessTypeEntity().getBusinessType());
        return dto;
    }

}
