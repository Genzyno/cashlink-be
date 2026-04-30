package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.RoleSaveRequest;
import com.john.ledger.entry.dto.request.RoleUpdateRequest;
import com.john.ledger.entry.dto.response.RoleResponse;

import java.util.UUID;

public interface IRoleService {
    ServiceResponse<PaginatedResponse<RoleResponse>> getAllRoles(int page, int size);
    ServiceResponse<RoleResponse> saveRole(RoleSaveRequest request);
    ServiceResponse<RoleResponse> updateRole(UUID id, RoleUpdateRequest request);
    ServiceResponse<RoleResponse> deleteRole(UUID id);
}
