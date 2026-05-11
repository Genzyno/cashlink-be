package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.response.UserResponseDTO;
import com.john.ledger.entry.dto.request.UserSaveRequestDTO;
import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.entity.UserEntity;

public class UserMapper {

    public static UserEntity toSaveEntity(UserSaveRequestDTO dto, RoleEntity roleEntity) {
        UserEntity entity = new UserEntity();
        entity.setUserName(dto.getUserName());
        entity.setUserEmail(dto.getUserEmail());
        entity.setUserMobile(dto.getUserMobile());
        entity.setRoleEntity(roleEntity);
        entity.setStatus(true);
        return entity;
    }

    public static UserResponseDTO toResponse(UserEntity entity) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUserName(entity.getUserName());
        dto.setUserEmail(entity.getUserEmail());
        dto.setUserMobile(entity.getUserMobile());
        dto.setStatus(entity.getStatus());
        if (entity.getRoleEntity() != null) {
            dto.setRoleId(entity.getRoleEntity().getId());
            dto.setRoleName(entity.getRoleEntity().getRoleName());
        }
        dto.setAdminId(entity.getAdminId());
        return dto;
    }
}
