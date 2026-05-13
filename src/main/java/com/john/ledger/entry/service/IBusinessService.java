package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.BusinessSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeUpdateRequest;
import com.john.ledger.entry.dto.request.BusinessUpdateRequest;
import com.john.ledger.entry.dto.response.BusinessResponse;
import com.john.ledger.entry.dto.response.BusinessTypeResponse;

import java.util.*;

public interface IBusinessService {

    // ---------- Business Type ----------
    ServiceResponse<BusinessTypeResponse> saveBusinessType(java.util.UUID adminId, BusinessTypeSaveRequest request);

    ServiceResponse<List<BusinessTypeResponse>> getBusinessTypeList(java.util.UUID adminId);

    ServiceResponse<BusinessTypeResponse> updateBusinessType(java.util.UUID adminId, java.util.UUID id,
            BusinessTypeUpdateRequest request);

    ServiceResponse<BusinessTypeResponse> deleteBusinessType(java.util.UUID adminId, java.util.UUID id);

    ServiceResponse<BusinessResponse> saveBusiness(BusinessSaveRequest request);

    ServiceResponse<PaginatedResponse<BusinessResponse>> getPaginatedBusiness(int page, int size);

    ServiceResponse<BusinessResponse> updateBusiness(UUID id, BusinessUpdateRequest request);

    ServiceResponse<BusinessResponse> deleteBusiness(UUID id);

    ServiceResponse<PaginatedResponse<BusinessResponse>> searchBusiness(String searchTerm, int page, int size);
}
