package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.RoleSaveRequest;
import com.john.ledger.entry.dto.request.RoleUpdateRequest;
import com.john.ledger.entry.dto.response.RoleResponse;
import com.john.ledger.entry.entity.RoleEntity;

import java.util.Collections;

public class RoleMapper {

    public static RoleEntity toSaveEntity(RoleSaveRequest request) {
        RoleEntity entity = new RoleEntity();
        entity.setRoleName(request.getRoleName());
        entity.setPermissions(request.getPermissions());
        entity.setPermissionScopes(request.getPermissionScopes());
        return entity;
    }

    public static void toUpdateEntity(RoleUpdateRequest request, RoleEntity entity) {
        if (request.getRoleName() != null) {
            entity.setRoleName(request.getRoleName());
        }
        if (request.getPermissions() != null) {
            entity.setPermissions(request.getPermissions());
        }
        if (request.getPermissionScopes() != null) {
            entity.setPermissionScopes(request.getPermissionScopes());
        }
    }

    public static RoleResponse toResponse(RoleEntity entity) {
        if (entity == null) {
            return null;
        }
        RoleResponse dto = new RoleResponse();
        dto.setId(entity.getId());
        dto.setRoleName(entity.getRoleName());
        dto.setPermissions(entity.getPermissions());
        dto.setPermissionScopes(entity.getPermissionScopes() != null ? entity.getPermissionScopes() : Collections.emptyMap());
        dto.setAdminId(entity.getAdminId());
        return dto;
    }
}
