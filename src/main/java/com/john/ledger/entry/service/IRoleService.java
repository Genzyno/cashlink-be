package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.RoleSaveRequest;
import com.john.ledger.entry.dto.request.RoleUpdateRequest;
import com.john.ledger.entry.dto.response.RoleResponse;

public interface IRoleService {
    ServiceResponse<PaginatedResponse<RoleResponse>> getAllRoles(java.util.UUID adminId, int page, int size);
    ServiceResponse<RoleResponse> saveRole(java.util.UUID adminId, RoleSaveRequest request);
    ServiceResponse<RoleResponse> updateRole(java.util.UUID adminId, java.util.UUID id, RoleUpdateRequest request);
    ServiceResponse<RoleResponse> deleteRole(java.util.UUID adminId, java.util.UUID id);
}
