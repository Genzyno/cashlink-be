package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.PaginationUtil;
import com.john.ledger.common.util.ResponseMessages;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.RoleSaveRequest;
import com.john.ledger.entry.dto.request.RoleUpdateRequest;
import com.john.ledger.entry.dto.response.RoleResponse;
import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.mapper.RoleMapper;
import com.john.ledger.entry.repository.RoleRepository;
import com.john.ledger.entry.service.IRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RoleService implements IRoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public ServiceResponse<PaginatedResponse<RoleResponse>> getAllRoles(int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<RoleResponse> rolePage = roleRepository.findAllOrderedByDisplayOrder(pageRequest).map(RoleMapper::toResponse);
            PaginatedResponse<RoleResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(rolePage);
            String message = rolePage.isEmpty() ? ResponseMessages.NO_RECORD : "Role list fetched successfully";
            return ServiceResponse.successResponse(200, message, paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<RoleResponse> saveRole(RoleSaveRequest request) {
        try {
            if (request.getRoleName() == null || request.getRoleName().isBlank()) {
                return ServiceResponse.failureResponse(400, "Role name is required");
            }
            Optional<RoleEntity> existing = roleRepository.findByRoleName(request.getRoleName().trim());
            if (existing.isPresent()) {
                return ServiceResponse.failureResponse(409, "Role name already exists");
            }
            RoleEntity entity = RoleMapper.toSaveEntity(request);
            RoleEntity saved = roleRepository.save(entity);
            return ServiceResponse.successResponse(201, "Role created successfully", RoleMapper.toResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<RoleResponse> updateRole(UUID id, RoleUpdateRequest request) {
        try {
            if (id == null) {
                return ServiceResponse.failureResponse(400, "Role ID is required");
            }
            Optional<RoleEntity> existingOpt = roleRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Role not found");
            }
            if (request.getRoleName() != null && !request.getRoleName().isBlank()) {
                Optional<RoleEntity> sameName = roleRepository.findByRoleName(request.getRoleName().trim());
                if (sameName.isPresent() && !sameName.get().getId().equals(id)) {
                    return ServiceResponse.failureResponse(409, "Role name already exists");
                }
            }
            RoleEntity entity = existingOpt.get();
            RoleMapper.toUpdateEntity(request, entity);
            RoleEntity saved = roleRepository.save(entity);
            return ServiceResponse.successResponse(200, "Role updated successfully", RoleMapper.toResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<RoleResponse> deleteRole(UUID id) {
        try {
            if (id == null) {
                return ServiceResponse.failureResponse(400, "Role ID is required");
            }
            Optional<RoleEntity> existing = roleRepository.findById(id);
            if (existing.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Role not found");
            }
            roleRepository.deleteById(id);
            return ServiceResponse.successResponse(200, "Role deleted successfully", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }
}
