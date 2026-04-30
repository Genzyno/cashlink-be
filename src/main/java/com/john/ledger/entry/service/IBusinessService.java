package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.BusinessSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeUpdateRequest;
import com.john.ledger.entry.dto.request.BusinessUpdateRequest;
import com.john.ledger.entry.dto.response.BusinessResponse;
import com.john.ledger.entry.dto.response.BusinessTypeResponse;

import java.util.List;
import java.util.UUID;

public interface IBusinessService {

    ServiceResponse<BusinessTypeResponse> saveBusinessType(BusinessTypeSaveRequest request);

    ServiceResponse<List<BusinessTypeResponse>> getBusinessTypeList();

    ServiceResponse<BusinessTypeResponse> updateBusinessType(UUID id, BusinessTypeUpdateRequest request);

    ServiceResponse<BusinessTypeResponse> deleteBusinessType(UUID id);

    ServiceResponse<BusinessResponse> saveBusiness(BusinessSaveRequest request);

    ServiceResponse<PaginatedResponse<BusinessResponse>> getPaginatedBusiness(int page, int size);

    ServiceResponse<BusinessResponse> updateBusiness(UUID id, BusinessUpdateRequest request);

    ServiceResponse<BusinessResponse> deleteBusiness(UUID id);

    ServiceResponse<PaginatedResponse<BusinessResponse>> searchBusiness(String searchTerm, int page, int size);
}
