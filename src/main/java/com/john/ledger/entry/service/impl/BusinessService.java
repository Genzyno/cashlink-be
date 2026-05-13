package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.*;
import com.john.ledger.entry.util.PermissionScopeHelper;
import com.john.ledger.entry.dto.request.BusinessSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeUpdateRequest;
import com.john.ledger.entry.dto.request.BusinessUpdateRequest;
import com.john.ledger.entry.dto.response.BusinessResponse;
import com.john.ledger.entry.dto.response.BusinessTypeResponse;
import com.john.ledger.entry.entity.BusinessEntity;
import com.john.ledger.entry.entity.BusinessTypeEntity;
import com.john.ledger.entry.mapper.BusinessMapper;
import com.john.ledger.entry.mapper.BusinessTypeMapper;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.BusinessRepository;
import com.john.ledger.entry.repository.BusinessTypeRepository;
import com.john.ledger.entry.service.IBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class BusinessService implements IBusinessService {

    @Autowired
    BusinessTypeRepository businessTypeRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    PermissionScopeHelper permissionScopeHelper;

    @Override
    public ServiceResponse<BusinessTypeResponse> saveBusinessType(java.util.UUID adminId, BusinessTypeSaveRequest request) {
        try {
            if (adminId == null) return ServiceResponse.failureResponse(401, "Unauthorized");
            // Check for duplicates in my scope (system or mine)
            Optional<BusinessTypeEntity> duplicate = businessTypeRepository.findByBusinessTypeAndAdminId(request.getBusinessType().trim(), adminId);
            if (duplicate.isPresent()) {
                return ServiceResponse.failureResponse(409, "Business type already exists in your scope");
            }

            BusinessTypeEntity entity = BusinessTypeMapper.toSaveEntity(request);
            entity.setAdminId(adminId);
            BusinessTypeEntity saved = businessTypeRepository.save(entity);
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, BusinessTypeMapper.toResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<List<BusinessTypeResponse>> getBusinessTypeList(java.util.UUID adminId) {
        try {
            if (adminId == null) return ServiceResponse.failureResponse(401, "Unauthorized");
            List<BusinessTypeEntity> entities = businessTypeRepository.findAllByAdminId(adminId);
            List<BusinessTypeResponse> responses = entities.stream()
                    .map(BusinessTypeMapper::toResponse)
                    .collect(Collectors.toList());
            return ServiceResponse.successResponse(200, entities.isEmpty() ? ResponseMessages.NO_RECORD : "Fetched successfully", responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BusinessTypeResponse> updateBusinessType(java.util.UUID adminId, java.util.UUID id, BusinessTypeUpdateRequest request) {
        try {
            if (adminId == null) return ServiceResponse.failureResponse(401, "Unauthorized");
            Optional<BusinessTypeEntity> existing = businessTypeRepository.findById(id);
            if (existing.isEmpty()) return ServiceResponse.failureResponse(404, "Not found");
            
            // Check ownership
            if (existing.get().getAdminId() == null) return ServiceResponse.failureResponse(403, "System types cannot be modified");
            if (!existing.get().getAdminId().equals(adminId)) return ServiceResponse.failureResponse(403, "Access denied");

            BusinessTypeEntity entity = existing.get();
            entity.setBusinessType(request.getBusinessType().trim());
            return ServiceResponse.successResponse(200, "Updated", BusinessTypeMapper.toResponse(businessTypeRepository.save(entity)));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BusinessTypeResponse> deleteBusinessType(java.util.UUID adminId, java.util.UUID id) {
        try {
            if (adminId == null) return ServiceResponse.failureResponse(401, "Unauthorized");
            Optional<BusinessTypeEntity> existing = businessTypeRepository.findById(id);
            if (existing.isEmpty()) return ServiceResponse.failureResponse(404, "Not found");

            // Check ownership
            if (existing.get().getAdminId() == null) return ServiceResponse.failureResponse(403, "System types cannot be deleted");
            if (!existing.get().getAdminId().equals(adminId)) return ServiceResponse.failureResponse(403, "Access denied");

            if (businessRepository.countByBusinessTypeEntity_Id(id) > 0) {
                return ServiceResponse.failureResponse(409, "In use by one or more businesses");
            }
            businessTypeRepository.deleteById(id);
            return ServiceResponse.successResponse(200, "Deleted", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BusinessResponse> saveBusiness(BusinessSaveRequest request) {
        try {

            //validation
            Optional<BusinessEntity> existingBusinessName = businessRepository.findByBusinessName(request.getBusinessName());
            if (existingBusinessName.isPresent()) {
                return ServiceResponse.failureResponse(409, "Business Name Already Exists");
            }

            Optional<BusinessTypeEntity> businessTypeEntity = businessTypeRepository.findById(request.getBusinessTypeId());
            if (businessTypeEntity.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business Type Not Found");
            }

            // Convert DTO → Entity
            BusinessEntity businessEntity = BusinessMapper.toSaveEntity(request);
            businessEntity.setBusinessTypeEntity(businessTypeEntity.get());
            permissionScopeHelper.getCurrentUserId().ifPresent(businessEntity::setCreatedByUserId);

            // Persist entity
            BusinessEntity savedEntity = businessRepository.save(businessEntity);
            // Convert Entity → Response DTO
            BusinessResponse responseDto = BusinessMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<BusinessResponse>> getPaginatedBusiness(int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            Page<BusinessEntity> businessPage;
            java.util.Optional<java.util.UUID> userId = permissionScopeHelper.getCurrentUserId();
            if (userId.isPresent()) {
                businessPage = businessRepository.findByCreatedByUserIdOrAssignedUserId(userId.get(), pageRequest);
            } else {
                businessPage = businessRepository.findAll(pageRequest);
            }
            if (businessPage.isEmpty()) {
                return ServiceResponse.failureResponse(204, ResponseMessages.NO_RECORD);
            }
            Page<BusinessResponse> businessResponsePage = businessPage.map(BusinessMapper::toResponse);
            PaginatedResponse<BusinessResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(businessResponsePage);
            return ServiceResponse.successResponse(200, "Business list fetched", paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BusinessResponse> updateBusiness(java.util.UUID id, BusinessUpdateRequest request) {
        try {

            //validation
            Optional<BusinessEntity> existingBusiness = businessRepository.findById(id);
            if (existingBusiness.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business Not Found");
            }
            BusinessEntity businessEntity = existingBusiness.get();
            if (!canAccessBusiness(businessEntity)) {
                return ServiceResponse.failureResponse(403, "Access denied: you can only update businesses you created or are assigned to.");
            }

            Optional<BusinessTypeEntity> businessTypeEntity = businessTypeRepository.findById(request.getBusinessTypeId());
            if (businessTypeEntity.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business Type Not Found");
            }

            businessEntity.setBusinessName(request.getBusinessName());
            businessEntity.setCurrency(request.getCurrency());
            businessEntity.setFinancialYear(request.getFinancialYear());
            businessEntity.setBusinessTypeEntity(businessTypeEntity.get());

            // Persist entity
            BusinessEntity savedEntity = businessRepository.save(businessEntity);
            // Convert Entity → Response DTO
            BusinessResponse responseDto = BusinessMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BusinessResponse> deleteBusiness(java.util.UUID id) {
        try {

            //validation
            Optional<BusinessEntity> existingBusiness = businessRepository.findById(id);
            if (existingBusiness.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Business Not Found!");
            }
            if (!canAccessBusiness(existingBusiness.get())) {
                return ServiceResponse.failureResponse(403, "Access denied: you can only delete businesses you created or are assigned to.");
            }
            // Delete entity
            businessRepository.deleteById(id);
            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    /** True if current user created the business or is assigned to at least one book in it. */
    private boolean canAccessBusiness(BusinessEntity business) {
        java.util.Optional<java.util.UUID> userId = permissionScopeHelper.getCurrentUserId();
        if (userId.isEmpty()) return true;
        if (business.getCreatedByUserId() != null && business.getCreatedByUserId().equals(userId.get())) return true;
        return bookRepository.findBusinessIdsByAssignedUserId(userId.get()).contains(business.getId());
    }

    @Override
    public ServiceResponse<PaginatedResponse<BusinessResponse>> searchBusiness(String searchTerm, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            Page<BusinessEntity> businessEntityPage;
            java.util.Optional<java.util.UUID> userId = permissionScopeHelper.getCurrentUserId();
            if (userId.isPresent()) {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    businessEntityPage = businessRepository.findByCreatedByUserIdOrAssignedUserId(userId.get(), pageRequest);
                } else {
                    businessEntityPage = businessRepository.findByCreatedByUserIdOrAssignedUserIdAndSearch(userId.get(), searchTerm.trim(), pageRequest);
                }
            } else {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    businessEntityPage = businessRepository.findAll(pageRequest);
                } else {
                    businessEntityPage = businessRepository.searchBusiness(searchTerm.trim(), pageRequest);
                }
            }
            if (businessEntityPage.isEmpty()) {
                return ServiceResponse.failureResponse(204, ResponseMessages.NO_RECORD);
            }
            Page<BusinessResponse> dtoPage = businessEntityPage.map(BusinessMapper::toResponse);
            PaginatedResponse<BusinessResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            return ServiceResponse.successResponse(200, "Business list fetched", paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

}
