package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.BookSaveRequest;
import com.john.ledger.entry.dto.request.BookUpdateRequest;
import com.john.ledger.entry.dto.response.BookResponse;
import com.john.ledger.entry.entity.BookEntity;
import com.john.ledger.entry.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class BookMapper {

    public static BookEntity toSaveEntity(BookSaveRequest dto) {
        BookEntity entity = new BookEntity();
        entity.setBusinessId(dto.getBusinessId());
        entity.setBookName(dto.getBookName());
        return entity;
    }

    public static void toUpdateEntity(BookEntity entity, BookUpdateRequest dto, Set<UserEntity> users) {
        if (dto.getBookName() != null) {
            entity.setBookName(dto.getBookName());
        }
        if (dto.getAssignedUserIds() != null && users != null) {
            entity.setAssignedUsers(users);
        }
    }


    public static BookResponse toResponse(BookEntity entity) {
        BookResponse dto = new BookResponse();
        dto.setId(entity.getId());
        dto.setBusinessId(entity.getBusinessId());
        dto.setBookName(entity.getBookName());

        // Extract user IDs from the assignedUsers set
        if (entity.getAssignedUsers() != null) {
            dto.setAssignedUserIds(entity.getAssignedUsers().stream().map(UserEntity::getId).collect(Collectors.toList())
            );
        }

        dto.setIsActive(entity.getIsActive());

        return dto;
    }
}
