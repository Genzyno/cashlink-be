package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.BookCategorySaveRequest;
import com.john.ledger.entry.dto.response.BookCategoryResponse;
import com.john.ledger.entry.dto.request.BookCategoryUpdateRequest;
import com.john.ledger.entry.entity.BookCategoryEntity;

public class BookCategoryMapper {

    public static BookCategoryEntity toSaveEntity(BookCategorySaveRequest request) {

        BookCategoryEntity entity = new BookCategoryEntity();

        entity.setBusinessId(request.getBusinessId());
        entity.setCategoryName(request.getCategoryName());
        entity.setCategoryType(request.getCategoryType());
        entity.setColorCode(request.getColorCode());
        entity.setStatus(request.getStatus());

        return entity;
    }

    public static void toUpdateEntity(BookCategoryUpdateRequest request, BookCategoryEntity entity) {

        entity.setCategoryName(request.getCategoryName());
        entity.setCategoryType(request.getCategoryType());
        entity.setColorCode(request.getColorCode());
        entity.setStatus(request.getStatus());
    }

    public static BookCategoryResponse toResponse(BookCategoryEntity entity) {

        BookCategoryResponse response = new BookCategoryResponse();

        response.setId(entity.getId());
        response.setBusinessId(entity.getBusinessId());
        response.setCategoryName(entity.getCategoryName());
        response.setCategoryType(entity.getCategoryType());
        response.setColorCode(entity.getColorCode());
        response.setStatus(entity.getStatus());
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());
        response.setAdminId(entity.getAdminId());
        return response;
    }
}
