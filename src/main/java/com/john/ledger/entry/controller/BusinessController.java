package com.john.ledger.entry.controller;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.BusinessSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeSaveRequest;
import com.john.ledger.entry.dto.request.BusinessTypeUpdateRequest;
import com.john.ledger.entry.dto.request.BusinessUpdateRequest;
import com.john.ledger.entry.dto.response.BusinessResponse;
import com.john.ledger.entry.dto.response.BusinessTypeResponse;
import com.john.ledger.entry.service.IBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("business")
public class BusinessController {

    @Autowired
    private IBusinessService businessService;


    @Operation(summary = "Save Business Type Object")
    @PostMapping("/save-business-type")
    public ResponseEntity<ServiceResponse<BusinessTypeResponse>> saveBusinessType(@RequestBody BusinessTypeSaveRequest request) {

        ServiceResponse<BusinessTypeResponse> response = null;
        try {
            response = businessService.saveBusinessType(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Get All Business Type")
    @GetMapping("/get-all-business-type")
    public ResponseEntity<ServiceResponse<List<BusinessTypeResponse>>> getBusinessTypeList() {

        ServiceResponse<List<BusinessTypeResponse>> response = null;
        try {
            response = businessService.getBusinessTypeList();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Update Business Type")
    @PutMapping("/update-business-type/{id}")
    public ResponseEntity<ServiceResponse<BusinessTypeResponse>> updateBusinessType(@PathVariable UUID id, @RequestBody BusinessTypeUpdateRequest request) {
        ServiceResponse<BusinessTypeResponse> response = null;
        try {
            response = businessService.updateBusinessType(id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete Business Type")
    @DeleteMapping("/delete-business-type/{id}")
    public ResponseEntity<ServiceResponse<BusinessTypeResponse>> deleteBusinessType(@PathVariable UUID id) {
        ServiceResponse<BusinessTypeResponse> response = null;
        try {
            response = businessService.deleteBusinessType(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Save Business Object")
    @PostMapping("/save-business")
    public ResponseEntity<ServiceResponse<BusinessResponse>> saveBusiness(@RequestBody BusinessSaveRequest request) {

        ServiceResponse<BusinessResponse> response = null;
        try {
            response = businessService.saveBusiness(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Get paginated Business list")
    @GetMapping("/get-all-business")
    public ResponseEntity<ServiceResponse<PaginatedResponse<BusinessResponse>>> getBusiness(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<BusinessResponse>> response = null;
        try {
            response = businessService.getPaginatedBusiness(page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Update Business Object")
    @PutMapping("/update-business/{id}")
    public ResponseEntity<ServiceResponse<BusinessResponse>> updateBusiness(@PathVariable UUID id, @RequestBody BusinessUpdateRequest request) {

        ServiceResponse<BusinessResponse> response = null;
        try {
            response = businessService.updateBusiness(id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete Business Object")
    @DeleteMapping("/delete-business/{id}")
    public ResponseEntity<ServiceResponse<BusinessResponse>> deleteBusiness(@PathVariable UUID id) {

        ServiceResponse<BusinessResponse> response = null;
        try {
            response = businessService.deleteBusiness(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Search Business list")
    @GetMapping("/search-business")
    public ResponseEntity<ServiceResponse<PaginatedResponse<BusinessResponse>>> searchBusiness(@RequestParam(defaultValue = "") String searchTerm, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<BusinessResponse>> response = null;
        try {
            response = businessService.searchBusiness(searchTerm, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }
}
