package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.BusinessTypeSaveRequest;
import com.john.ledger.entry.dto.response.BusinessTypeResponse;
import com.john.ledger.entry.entity.BusinessTypeEntity;

public class BusinessTypeMapper {

    public static BusinessTypeEntity toSaveEntity(BusinessTypeSaveRequest dto) {

        BusinessTypeEntity entity = new BusinessTypeEntity();
        entity.setBusinessType(dto.getBusinessType());

        return entity;
    }

    public static BusinessTypeResponse toResponse(BusinessTypeEntity entity) {

        BusinessTypeResponse dto = new BusinessTypeResponse();
        dto.setId(entity.getId());
        dto.setBusinessType(entity.getBusinessType());
        return dto;
    }

}
