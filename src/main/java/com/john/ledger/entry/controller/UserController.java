package com.john.ledger.entry.controller;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.AcceptInviteRequest;
import com.john.ledger.entry.dto.request.RejectInviteRequest;
import com.john.ledger.entry.dto.request.UserInviteRequest;
import com.john.ledger.entry.dto.request.UserSaveRequestDTO;
import com.john.ledger.entry.dto.request.UserUpdateRequestDTO;
import com.john.ledger.entry.dto.response.AcceptedInviteNotificationResponse;
import com.john.ledger.entry.dto.response.InviteDetailsResponse;
import com.john.ledger.entry.dto.response.UserResponseDTO;
import com.john.ledger.entry.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Operation(summary = "Save User Object")
    @PostMapping("/save-user")
    public ResponseEntity<ServiceResponse<UserResponseDTO>> saveUser(@RequestBody UserSaveRequestDTO request) {

        ServiceResponse<UserResponseDTO> response = null;
        try {
            response = userService.saveUser(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Get paginated user list")
    @GetMapping("/get-all-user")
    public ResponseEntity<ServiceResponse<PaginatedResponse<UserResponseDTO>>> getUser(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<UserResponseDTO>> response = null;
        try {
            response = userService.getPaginatedUser(page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Update User Object")
    @PutMapping("/update-user/{id}")
    public ResponseEntity<ServiceResponse<UserResponseDTO>> updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequestDTO request) {

        ServiceResponse<UserResponseDTO> response = null;
        try {
            response = userService.updateUser(id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete User Object")
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<ServiceResponse<UserResponseDTO>> deleteUser(@PathVariable UUID id) {

        ServiceResponse<UserResponseDTO> response = null;
        try {
            response = userService.deleteUser(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Search user list")
    @GetMapping("/search-user")
    public ResponseEntity<ServiceResponse<PaginatedResponse<UserResponseDTO>>> searchUser(@RequestParam(defaultValue = "") String searchTerm, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<UserResponseDTO>> response = null;
        try {
            response = userService.searchUser(searchTerm, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Send invite to multiple emails; each can have a different role")
    @PostMapping("/send-invite")
    public ResponseEntity<ServiceResponse<Void>> sendInvite(@RequestBody UserInviteRequest request) {
        try {
            ServiceResponse<Void> response = userService.sendInvite(request);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ServiceResponse.failureResponse(500, "Internal server error"));
        }
    }

    @Operation(summary = "Get invite details by token (for accept-invite page)")
    @GetMapping("/invite-by-token")
    public ResponseEntity<ServiceResponse<InviteDetailsResponse>> getInviteByToken(@RequestParam String token) {
        try {
            ServiceResponse<InviteDetailsResponse> response = userService.getInviteByToken(token);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Accept invitation: create account with token from email")
    @PostMapping("/accept-invite")
    public ResponseEntity<ServiceResponse<UserResponseDTO>> acceptInvite(@RequestBody AcceptInviteRequest request) {
        try {
            ServiceResponse<UserResponseDTO> response = userService.acceptInvite(request);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Reject invitation: decline invite so admin can send a new one to this email")
    @PostMapping("/reject-invite")
    public ResponseEntity<ServiceResponse<Void>> rejectInvite(@RequestBody RejectInviteRequest request) {
        try {
            ServiceResponse<Void> response = userService.rejectInvite(request);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Get paginated list of accepted invite notifications for the current organization")
    @GetMapping("/accepted-invite-notifications")
    public ResponseEntity<ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>>> getAcceptedInviteNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID businessId) {
        try {
            ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> response =
                    userService.getAcceptedInviteNotifications(page, size, businessId);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Get paginated list of rejected invite notifications for the current organization")
    @GetMapping("/rejected-invite-notifications")
    public ResponseEntity<ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>>> getRejectedInviteNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID businessId) {
        try {
            ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> response =
                    userService.getRejectedInviteNotifications(page, size, businessId);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Get paginated list of pending invite notifications for the current organization")
    @GetMapping("/pending-invite-notifications")
    public ResponseEntity<ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>>> getPendingInviteNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) UUID businessId) {
        try {
            ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> response =
                    userService.getPendingInviteNotifications(page, size, businessId);
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
