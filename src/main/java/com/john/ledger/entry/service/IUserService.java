package com.john.ledger.entry.service;

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

import java.util.UUID;

public interface IUserService {

    ServiceResponse<UserResponseDTO> saveUser(UserSaveRequestDTO request);

    ServiceResponse<PaginatedResponse<UserResponseDTO>> getPaginatedUser(int page, int size);

    ServiceResponse<UserResponseDTO> updateUser(UUID id, UserUpdateRequestDTO request);

    ServiceResponse<UserResponseDTO> deleteUser(UUID id);

    ServiceResponse<PaginatedResponse<UserResponseDTO>> searchUser(String searchTerm, int page, int size);

    /** Send invite emails to multiple addresses; each can have a different role. */
    ServiceResponse<Void> sendInvite(UserInviteRequest request);

    /** Get invite details by token (for accept-invite UI). */
    ServiceResponse<InviteDetailsResponse> getInviteByToken(String token);

    /** Accept invitation: create user and mark invite accepted. Returns created user. */
    ServiceResponse<UserResponseDTO> acceptInvite(AcceptInviteRequest request);

    /** Reject invitation: clear invite for this email so admin can send invite again. */
    ServiceResponse<Void> rejectInvite(RejectInviteRequest request);

    /** Get paginated list of accepted invite notifications for the current organization. */
    ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getAcceptedInviteNotifications(
            int page, int size, UUID businessId);

    /** Get paginated list of rejected invite notifications for the current organization. */
    ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getRejectedInviteNotifications(
            int page, int size, UUID businessId);

    /** Get paginated list of pending invite notifications for the current organization. */
    ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getPendingInviteNotifications(
            int page, int size, UUID businessId);
}
