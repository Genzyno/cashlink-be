package com.john.ledger.entry.controller;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.RoleSaveRequest;
import com.john.ledger.entry.dto.request.RoleUpdateRequest;
import com.john.ledger.entry.dto.response.RoleResponse;
import com.john.ledger.entry.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("role")
public class RoleController {

    @Autowired
    private IRoleService roleService;

    @Operation(summary = "List roles (paginated)")
    @GetMapping("/get-all-role")
    public ResponseEntity<ServiceResponse<PaginatedResponse<RoleResponse>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        try {
            ServiceResponse<PaginatedResponse<RoleResponse>> response = roleService.getAllRoles(page, size);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Create role")
    @PostMapping("/save-role")
    public ResponseEntity<ServiceResponse<RoleResponse>> saveRole(@RequestBody RoleSaveRequest request) {
        try {
            ServiceResponse<RoleResponse> response = roleService.saveRole(request);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Update role")
    @PutMapping("/update-role/{id}")
    public ResponseEntity<ServiceResponse<RoleResponse>> updateRole(
            @PathVariable Optional<UUID> id,
            @RequestBody RoleUpdateRequest request) {
        if (id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Role ID is required"));
        }
        try {
            request.setId(id.get());
            ServiceResponse<RoleResponse> response = roleService.updateRole(id.get(), request);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/delete-role/{id}")
    public ResponseEntity<ServiceResponse<RoleResponse>> deleteRole(@PathVariable Optional<UUID> id) {
        if (id.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "Role ID is required"));
        }
        try {
            ServiceResponse<RoleResponse> response = roleService.deleteRole(id.get());
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
