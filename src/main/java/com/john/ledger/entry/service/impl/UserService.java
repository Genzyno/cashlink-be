package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.*;
import com.john.ledger.entry.util.PermissionScopeHelper;
import com.john.ledger.entry.dto.request.AcceptInviteRequest;
import com.john.ledger.entry.dto.request.RejectInviteRequest;
import com.john.ledger.config.AppProperties;
import com.john.ledger.entry.dto.request.UserInviteRequest;
import com.john.ledger.entry.dto.request.UserSaveRequestDTO;
import com.john.ledger.entry.dto.request.UserUpdateRequestDTO;
import com.john.ledger.entry.dto.response.AcceptedInviteNotificationResponse;
import com.john.ledger.entry.dto.response.InviteDetailsResponse;
import com.john.ledger.entry.dto.response.UserResponseDTO;
import com.john.ledger.entry.entity.BookEntity;
import com.john.ledger.entry.entity.UserInviteEntity;
import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.entity.UserEntity;
import com.john.ledger.entry.mapper.UserMapper;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.BusinessRepository;
import com.john.ledger.entry.repository.RoleRepository;
import com.john.ledger.entry.repository.UserInviteBookRepository;
import com.john.ledger.entry.repository.UserInviteRepository;
import com.john.ledger.entry.repository.UserRepository;
import com.john.ledger.entry.service.InviteEmailService;
import com.john.ledger.entry.service.IUserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.john.ledger.entry.entity.UserInviteBookEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$");

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    PermissionScopeHelper permissionScopeHelper;

    @Autowired
    UserInviteRepository userInviteRepository;

    @Autowired
    UserInviteBookRepository userInviteBookRepository;

    @Autowired
    BusinessRepository businessRepository;

    @Autowired
    InviteEmailService inviteEmailService;

    @Autowired
    private AppProperties appProperties;

    @Override
    public ServiceResponse<UserResponseDTO> saveUser(UserSaveRequestDTO request) {

        try {

            // validation
            Optional<UserEntity> existingUserName = userRepository.findByUserName(request.getUserName());
            if (existingUserName.isPresent()) {
                return ServiceResponse.failureResponse(409, "User Name Already Exists");
            }
            Optional<UserEntity> existingUserEmail = userRepository.findByUserEmail(request.getUserEmail());
            if (existingUserEmail.isPresent()) {
                return ServiceResponse.failureResponse(409, "User Email Already Exists");
            }
            Optional<UserEntity> existingUserMobile = userRepository.findByUserMobile(request.getUserMobile());
            if (existingUserMobile.isPresent()) {
                return ServiceResponse.failureResponse(409, "User Mobile Already Exists");
            }
            if (request.getRoleId() == null) {
                return ServiceResponse.failureResponse(400, "Role is required");
            }
            Optional<RoleEntity> roleOpt = roleRepository.findById(request.getRoleId());
            if (roleOpt.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Role not found");
            }

            // Convert DTO → Entity
            UserEntity userEntity = UserMapper.toSaveEntity(request, roleOpt.get());

            // generate random password
            String userTempPassword = PasswordGenerator.generate6CharCode();
            userEntity.setPassword(userTempPassword);

            // Persist entity
            UserEntity savedEntity = userRepository.save(userEntity);
            // Convert Entity → Response DTO
            UserResponseDTO responseDto = UserMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<UserResponseDTO>> getPaginatedUser(int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            Page<UserEntity> userEntityPage;

            Optional<UUID> currentUserId = permissionScopeHelper.getCurrentUserId();
            if (currentUserId.isEmpty()) {
                return ServiceResponse.failureResponse(401, "Unauthorized");
            }

            UserEntity currentUser = userRepository.findById(currentUserId.get()).orElse(null);
            if (currentUser == null) {
                return ServiceResponse.failureResponse(404, "User not found");
            }

            UUID effectiveAdminId = (currentUser.getAdminId() != null) ? currentUser.getAdminId() : currentUser.getId();

            if (permissionScopeHelper.isAssignedScope("team", "view")) {
                List<java.util.UUID> allowedIds = bookRepository.findUserIdsSharingBookWith(currentUserId.get());
                if (allowedIds.isEmpty()) {
                    userEntityPage = Page.empty(pageRequest);
                } else {
                    userEntityPage = userRepository.findByIdIn(allowedIds, pageRequest);
                }
            } else {
                userEntityPage = userRepository.findByAdminIdOrId(effectiveAdminId, pageRequest);
            }
            Page<UserResponseDTO> userPage = userEntityPage.map(UserMapper::toResponse);
            if (userPage.isEmpty()) {
                return ServiceResponse.failureResponse(204, ResponseMessages.NO_RECORD);
            }
            PaginatedResponse<UserResponseDTO> paginatedResponse = PaginationUtil.createPaginatedResponse(userPage);
            return ServiceResponse.successResponse(200, "User list fetched", paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<UserResponseDTO> updateUser(java.util.UUID id, UserUpdateRequestDTO request) {

        try {

            // validation
            Optional<UserEntity> existingUser = userRepository.findById(id);
            if (existingUser.isEmpty()) {
                return ServiceResponse.failureResponse(404, "User Not Found");
            }

            UserEntity updateUser = existingUser.get();
            updateUser.setUserEmail(request.getUserEmail());
            updateUser.setUserName(request.getUserName());
            updateUser.setUserMobile(request.getUserMobile());
            if (request.getRoleId() != null) {
                Optional<RoleEntity> roleOpt = roleRepository.findById(request.getRoleId());
                if (roleOpt.isEmpty()) {
                    return ServiceResponse.failureResponse(404, "Role not found");
                }
                updateUser.setRoleEntity(roleOpt.get());
            }

            // Persist entity
            UserEntity savedEntity = userRepository.save(updateUser);
            // Convert Entity → Response DTO
            UserResponseDTO responseDto = UserMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<UserResponseDTO> deleteUser(java.util.UUID id) {
        try {
            Optional<UserEntity> existingUser = userRepository.findById(id);
            if (existingUser.isEmpty()) {
                return ServiceResponse.failureResponse(404, "User Not Found!");
            }
            UserEntity user = existingUser.get();
            // Remove user from all books' assignedUsers before delete (avoids FK violation)
            java.util.List<BookEntity> booksWithUser = bookRepository.findByAssignedUserId(id);
            for (BookEntity book : booksWithUser) {
                book.getAssignedUsers().remove(user);
                bookRepository.save(book);
            }
            userRepository.deleteById(id);
            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<UserResponseDTO>> searchUser(String searchTerm, int page, int size) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
            Page<UserEntity> userEntityPage;

            Optional<UUID> currentUserId = permissionScopeHelper.getCurrentUserId();
            if (currentUserId.isEmpty()) {
                return ServiceResponse.failureResponse(401, "Unauthorized");
            }

            UserEntity currentUser = userRepository.findById(currentUserId.get()).orElse(null);
            if (currentUser == null) {
                return ServiceResponse.failureResponse(404, "User not found");
            }

            UUID effectiveAdminId = (currentUser.getAdminId() != null) ? currentUser.getAdminId() : currentUser.getId();

            boolean assignedScope = permissionScopeHelper.isAssignedScope("team", "view");
            if (assignedScope) {
                List<java.util.UUID> allowedIds = bookRepository.findUserIdsSharingBookWith(currentUserId.get());
                if (allowedIds.isEmpty()) {
                    userEntityPage = Page.empty(pageRequest);
                } else if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    userEntityPage = userRepository.findByIdIn(allowedIds, pageRequest);
                } else {
                    userEntityPage = userRepository.findByIdInAndSearch(allowedIds, searchTerm.trim(), pageRequest);
                }
            } else {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    userEntityPage = userRepository.findByAdminIdOrId(effectiveAdminId, pageRequest);
                } else {
                    userEntityPage = userRepository.findByAdminIdOrIdAndSearch(effectiveAdminId, searchTerm.trim(),
                            pageRequest);
                }
            }
            Page<UserResponseDTO> dtoPage = userEntityPage.map(UserMapper::toResponse);
            if (dtoPage.isEmpty()) {
                return ServiceResponse.failureResponse(204, ResponseMessages.NO_RECORD);
            }
            PaginatedResponse<UserResponseDTO> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            return ServiceResponse.successResponse(200, "User list fetched", paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<Void> sendInvite(UserInviteRequest request) {
        try {
            if (request == null || request.getInvites() == null || request.getInvites().isEmpty()) {
                return ServiceResponse.failureResponse(400, "invites array is required and cannot be empty");
            }
            if (Boolean.TRUE.equals(request.getAllBooks()) && request.getBusinessId() == null) {
                return ServiceResponse.failureResponse(400, "businessId is required when allBooks is true");
            }
            UUID currentUserId = permissionScopeHelper.getCurrentUserId().orElse(null);

            // Resolve book IDs: all books for business, or specific bookIds
            List<UUID> resolvedBookIds = new ArrayList<>();
            if (Boolean.TRUE.equals(request.getAllBooks()) && request.getBusinessId() != null) {
                List<BookEntity> businessBooks = bookRepository
                        .findByBusinessId(request.getBusinessId(), Pageable.unpaged()).getContent();
                resolvedBookIds = businessBooks.stream().map(BookEntity::getId).collect(Collectors.toList());
            } else if (request.getBookIds() != null && !request.getBookIds().isEmpty()) {
                List<BookEntity> found = bookRepository.findAllById(request.getBookIds());
                if (found.size() != request.getBookIds().size()) {
                    return ServiceResponse.failureResponse(404, "One or more book IDs not found.");
                }
                resolvedBookIds = new ArrayList<>(request.getBookIds());
            }

            if (resolvedBookIds.isEmpty()) {
                return ServiceResponse.failureResponse(400, "Please choose at least one book to invite users.");
            }

            // Build "BookName (BusinessName)" for email
            List<String> bookAndBusinessLines = new ArrayList<>();
            if (!resolvedBookIds.isEmpty()) {
                List<BookEntity> books = bookRepository.findAllById(resolvedBookIds);
                for (BookEntity b : books) {
                    String businessName = businessRepository.findById(b.getBusinessId())
                            .map(be -> be.getBusinessName())
                            .orElse("Unknown");
                    bookAndBusinessLines.add(b.getBookName() + " (" + businessName + ")");
                }
            }

            int inviteExpirationDays = appProperties.getInvite().getExpirationDays() > 0
                    ? appProperties.getInvite().getExpirationDays()
                    : 7;
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(inviteExpirationDays);
            String fUrl = appProperties.getFrontendUrl();
            if (fUrl == null || fUrl.isBlank())
                fUrl = "http://localhost:4200";
            String baseUrl = fUrl.endsWith("/") ? fUrl : fUrl + "/";
            String acceptPath = "accept-invite";
            String rejectPath = "reject-invite";

            for (UserInviteRequest.InviteItem item : request.getInvites()) {
                String email = item.getEmail() == null ? "" : item.getEmail().trim().toLowerCase();
                if (email.isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                    return ServiceResponse.failureResponse(400, "Invalid email format: " + email);
                }

                if (userRepository.findByUserEmail(email).isPresent()) {
                    return ServiceResponse.failureResponse(409, "User already exists with email: " + email);
                }
                if (userInviteRepository.existsByEmailAndStatus(email, UserInviteEntity.InviteStatus.PENDING)) {
                    return ServiceResponse.failureResponse(409, "Pending invite already exists for: " + email);
                }
                if (item.getRoleId() == null) {
                    return ServiceResponse.failureResponse(400, "Role is required for email: " + email);
                }
                if (roleRepository.findById(item.getRoleId()).isEmpty()) {
                    return ServiceResponse.failureResponse(404, "Role not found for email: " + email);
                }

                String token = UUID.randomUUID().toString().replace("-", "");
                RoleEntity role = item.getRoleId() != null ? roleRepository.findById(item.getRoleId()).orElse(null)
                        : null;
                String roleName = role != null ? role.getRoleName() : null;

                UserEntity currentUser = userRepository.findById(currentUserId).orElse(null);
                UUID adminIdToStore = (currentUser != null)
                        ? (currentUser.getAdminId() != null ? currentUser.getAdminId() : currentUser.getId())
                        : null;

                UserInviteEntity invite = UserInviteEntity.builder()
                        .email(email)
                        .roleId(item.getRoleId())
                        .token(token)
                        .expiresAt(expiresAt)
                        .status(UserInviteEntity.InviteStatus.PENDING)
                        .invitedByUserId(currentUserId)
                        .adminId(adminIdToStore)
                        .build();
                userInviteRepository.save(invite);

                for (UUID bookId : resolvedBookIds) {
                    userInviteBookRepository.save(UserInviteBookEntity.builder()
                            .inviteId(invite.getId())
                            .bookId(bookId)
                            .build());
                }

                String acceptLink = baseUrl + acceptPath + "?token=" + token;
                String rejectLink = baseUrl + rejectPath + "?token=" + token;
                String invitedByName = userRepository.findById(currentUserId).map(UserEntity::getUserName).orElse(null);
                inviteEmailService.sendInviteEmail(email, roleName, bookAndBusinessLines, acceptLink, rejectLink,
                        invitedByName);
            }
            return ServiceResponse.successResponse(200, "Invitation emails sent successfully.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<InviteDetailsResponse> getInviteByToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return ServiceResponse.successResponse(200, "Invalid or expired invite.",
                        InviteDetailsResponse.builder().valid(false).message("Token is required.").build());
            }
            Optional<UserInviteEntity> opt = userInviteRepository.findByToken(token.trim());
            if (opt.isEmpty()) {
                return ServiceResponse.successResponse(200, "Invalid or expired invite.",
                        InviteDetailsResponse.builder().valid(false).message("Invite not found.").build());
            }
            UserInviteEntity invite = opt.get();
            if (invite.getStatus() != UserInviteEntity.InviteStatus.PENDING) {
                return ServiceResponse.successResponse(200, "Invite already used or expired.",
                        InviteDetailsResponse.builder().valid(false)
                                .message("This invitation has already been used or has expired.").build());
            }
            if (LocalDateTime.now().isAfter(invite.getExpiresAt())) {
                invite.setStatus(UserInviteEntity.InviteStatus.EXPIRED);
                userInviteRepository.save(invite);
                return ServiceResponse.successResponse(200, "Invite expired.",
                        InviteDetailsResponse.builder().valid(false).message("This invitation has expired.").build());
            }
            String roleName = invite.getRoleId() != null
                    ? roleRepository.findById(invite.getRoleId()).map(RoleEntity::getRoleName).orElse(null)
                    : null;
            InviteDetailsResponse details = InviteDetailsResponse.builder()
                    .email(invite.getEmail())
                    .roleId(invite.getRoleId())
                    .roleName(roleName)
                    .valid(true)
                    .message("Invite is valid. Complete the form to accept.")
                    .build();
            return ServiceResponse.successResponse(200, "Invite details fetched.", details);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<UserResponseDTO> acceptInvite(AcceptInviteRequest request) {
        try {
            if (request == null || request.getToken() == null || request.getToken().isBlank()) {
                return ServiceResponse.failureResponse(400, "Token is required.");
            }
            if (request.getUserName() == null || request.getUserName().trim().isEmpty()) {
                return ServiceResponse.failureResponse(400, "User name is required.");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ServiceResponse.failureResponse(400, "Password is required.");
            }
            Optional<UserInviteEntity> opt = userInviteRepository.findByToken(request.getToken().trim());
            if (opt.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Invite not found or invalid.");
            }
            UserInviteEntity invite = opt.get();
            if (invite.getStatus() != UserInviteEntity.InviteStatus.PENDING) {
                return ServiceResponse.failureResponse(409, "This invitation has already been used or has expired.");
            }
            if (LocalDateTime.now().isAfter(invite.getExpiresAt())) {
                invite.setStatus(UserInviteEntity.InviteStatus.EXPIRED);
                userInviteRepository.save(invite);
                return ServiceResponse.failureResponse(410, "This invitation has expired.");
            }
            if (userRepository.findByUserEmail(invite.getEmail()).isPresent()) {
                return ServiceResponse.failureResponse(409, "A user with this email already exists.");
            }

            RoleEntity role = invite.getRoleId() != null
                    ? roleRepository.findById(invite.getRoleId()).orElse(null)
                    : null;
            if (invite.getRoleId() != null && role == null) {
                return ServiceResponse.failureResponse(404, "Role no longer exists.");
            }

            // user_mobile column is varchar(15); use unique 15-char placeholder when not
            // provided
            String mobile = (request.getUserMobile() != null && !request.getUserMobile().trim().isEmpty())
                    ? request.getUserMobile().trim()
                    : ("P" + invite.getToken().substring(0, Math.min(14, invite.getToken().length())));
            if (mobile.length() > 15) {
                mobile = mobile.substring(0, 15);
            }
            if (userRepository.findByUserMobile(mobile).isPresent()) {
                return ServiceResponse.failureResponse(409, "User mobile already in use.");
            }

            UserEntity user = UserEntity.builder()
                    .userName(request.getUserName().trim())
                    .userEmail(invite.getEmail())
                    .userMobile(mobile)
                    .password(request.getPassword())
                    .roleEntity(role)
                    .adminId(invite.getAdminId())
                    .status(true)
                    .build();
            UserEntity saved = userRepository.save(user);

            invite.setStatus(UserInviteEntity.InviteStatus.ACCEPTED);
            invite.setAcceptedAt(LocalDateTime.now());
            userInviteRepository.save(invite);

            // Assign user to books linked to this invite
            List<UserInviteBookEntity> inviteBooks = userInviteBookRepository
                    .findByInviteIdOrderByBookId(invite.getId());
            for (UserInviteBookEntity ib : inviteBooks) {
                bookRepository.findById(ib.getBookId()).ifPresent(book -> {
                    Set<UserEntity> assigned = book.getAssignedUsers();
                    if (assigned == null)
                        assigned = new java.util.HashSet<>();
                    assigned.add(saved);
                    book.setAssignedUsers(assigned);
                    bookRepository.save(book);
                });
            }

            UserResponseDTO responseDto = UserMapper.toResponse(saved);
            return ServiceResponse.successResponse(201, "Invitation accepted. Account created successfully.",
                    responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<Void> rejectInvite(RejectInviteRequest request) {
        try {
            if (request == null || request.getToken() == null || request.getToken().isBlank()) {
                return ServiceResponse.failureResponse(400, "Token is required.");
            }
            Optional<UserInviteEntity> opt = userInviteRepository.findByToken(request.getToken().trim());
            if (opt.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Invite not found or invalid.");
            }
            UserInviteEntity invite = opt.get();
            if (invite.getStatus() != UserInviteEntity.InviteStatus.PENDING) {
                return ServiceResponse.successResponse(200, "Invitation was already declined or used.", null);
            }
            invite.setStatus(UserInviteEntity.InviteStatus.REJECTED);
            invite.setRejectedAt(LocalDateTime.now());
            userInviteRepository.save(invite);
            return ServiceResponse.successResponse(200,
                    "Invitation declined. The admin can send a new invite to this email if needed.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getAcceptedInviteNotifications(
            int page, int size, UUID businessId) {
        return getInviteNotificationsByStatus(page, size, businessId, List.of(UserInviteEntity.InviteStatus.ACCEPTED));
    }

    @Override
    public ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getRejectedInviteNotifications(
            int page, int size, UUID businessId) {
        return getInviteNotificationsByStatus(page, size, businessId, List.of(UserInviteEntity.InviteStatus.REJECTED));
    }

    @Override
    public ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getPendingInviteNotifications(
            int page, int size, UUID businessId) {
        return getInviteNotificationsByStatus(page, size, businessId, List.of(UserInviteEntity.InviteStatus.PENDING));
    }

    private ServiceResponse<PaginatedResponse<AcceptedInviteNotificationResponse>> getInviteNotificationsByStatus(
            int page, int size, UUID businessId, List<UserInviteEntity.InviteStatus> statuses) {
        try {
            Optional<UUID> currentUserIdOpt = permissionScopeHelper.getCurrentUserId();
            if (currentUserIdOpt.isEmpty()) {
                return ServiceResponse.failureResponse(401, "Authentication required.");
            }
            UUID currentUserId = currentUserIdOpt.get();
            UserEntity currentUser = userRepository.findById(currentUserId).orElse(null);
            UUID adminId = (currentUser != null) ? currentUser.getAdminId() : null;

            if (adminId == null) {
                return ServiceResponse.successResponse(200, "Success", PaginatedResponse.empty());
            }

            Sort sort;
            if (statuses.contains(UserInviteEntity.InviteStatus.ACCEPTED)) {
                sort = Sort.by(Sort.Order.desc("acceptedAt").nullsLast());
            } else if (statuses.contains(UserInviteEntity.InviteStatus.REJECTED)) {
                sort = Sort.by(Sort.Order.desc("rejectedAt").nullsLast());
            } else {
                sort = Sort.by(Sort.Order.desc("createdTime"));
            }

            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<UserInviteEntity> invitePage = userInviteRepository
                    .findByAdminIdAndStatusInAndOptionalBusiness(adminId, statuses, businessId, pageRequest);

            List<AcceptedInviteNotificationResponse> content = invitePage.getContent().stream()
                    .map(this::mapInviteToNotification)
                    .collect(Collectors.toList());
            PaginatedResponse<AcceptedInviteNotificationResponse> paginated = PaginationUtil.createPaginatedResponse(
                    new PageImpl<>(content, pageRequest, invitePage.getTotalElements()));

            return ServiceResponse.successResponse(200, "Success", paginated);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    private AcceptedInviteNotificationResponse mapInviteToNotification(UserInviteEntity invite) {
        String userId = null;
        String userName = invite.getEmail();
        if (invite.getStatus() == UserInviteEntity.InviteStatus.ACCEPTED) {
            Optional<UserEntity> userOpt = userRepository.findByUserEmail(invite.getEmail());
            if (userOpt.isPresent()) {
                userId = userOpt.get().getId().toString();
                userName = userOpt.get().getUserName();
            }
        }
        String acceptedAtIso = invite.getAcceptedAt() != null
                ? invite.getAcceptedAt().atZone(ZoneOffset.UTC).toInstant().toString()
                : null;
        String rejectedAtIso = invite.getRejectedAt() != null
                ? invite.getRejectedAt().atZone(ZoneOffset.UTC).toInstant().toString()
                : null;
        String roleName = invite.getRoleId() != null
                ? roleRepository.findById(invite.getRoleId()).map(RoleEntity::getRoleName).orElse(null)
                : null;
        UUID businessId = null;
        String businessName = null;
        List<UserInviteBookEntity> inviteBooks = userInviteBookRepository.findByInviteIdOrderByBookId(invite.getId());
        if (!inviteBooks.isEmpty()) {
            Optional<BookEntity> firstBook = bookRepository.findById(inviteBooks.get(0).getBookId());
            if (firstBook.isPresent()) {
                businessId = firstBook.get().getBusinessId();
                businessName = businessRepository.findById(businessId)
                        .map(com.john.ledger.entry.entity.BusinessEntity::getBusinessName).orElse(null);
            }
        }
        return AcceptedInviteNotificationResponse.builder()
                .id(invite.getId().toString())
                .userId(userId)
                .userName(userName)
                .userEmail(invite.getEmail())
                .acceptedAt(acceptedAtIso)
                .rejectedAt(rejectedAtIso)
                .status(invite.getStatus().name())
                .invitedByUserId(invite.getInvitedByUserId() != null ? invite.getInvitedByUserId().toString() : null)
                .businessId(businessId != null ? businessId.toString() : null)
                .businessName(businessName)
                .roleName(roleName)
                .build();
    }

}
