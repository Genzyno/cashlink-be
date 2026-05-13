package com.john.ledger.entry.service.impl;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.PaginationUtil;
import com.john.ledger.entry.util.PermissionScopeHelper;
import com.john.ledger.common.util.ResponseMessages;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.*;
import com.john.ledger.entry.dto.response.BookCategoryResponse;
import com.john.ledger.entry.dto.response.BookResponse;
import com.john.ledger.entry.dto.response.PaymentModeResponse;
import com.john.ledger.entry.entity.*;
import com.john.ledger.entry.mapper.BookCategoryMapper;
import com.john.ledger.entry.mapper.BookMapper;
import com.john.ledger.entry.mapper.PaymentModeMapper;
import com.john.ledger.entry.repository.BookCategoryRepository;
import com.john.ledger.entry.repository.BookRepository;
import com.john.ledger.entry.repository.PaymentModeRepository;
import com.john.ledger.entry.repository.UserRepository;
import com.john.ledger.entry.service.IBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import java.util.stream.Collectors;

@Service
public class BookService implements IBookService {

    @Autowired
    BookCategoryRepository bookCategoryRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PaymentModeRepository paymentModeRepository;
    @Autowired
    PermissionScopeHelper permissionScopeHelper;

    @Override
    public ServiceResponse<BookCategoryResponse> saveBookCategory(java.util.UUID adminId,
            BookCategorySaveRequest request) {
        try {
            if (adminId == null)
                return ServiceResponse.failureResponse(401, "Unauthorized");
            // validation: businessId is optional now for system/global categories
            // validation: duplicate check within admin scope
            Optional<BookCategoryEntity> existing = bookCategoryRepository
                    .findByCategoryNameAndAdminId(request.getCategoryName(), adminId);
            if (existing.isPresent()) {
                return ServiceResponse.failureResponse(409, "Category already exists in your scope");
            }

            BookCategoryEntity entity = BookCategoryMapper.toSaveEntity(request);
            entity.setAdminId(adminId);
            BookCategoryEntity saved = bookCategoryRepository.save(entity);
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, BookCategoryMapper.toResponse(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<List<BookCategoryResponse>> getBookCategoryList(java.util.UUID adminId,
            java.util.UUID businessId) {
        try {
            if (adminId == null)
                return ServiceResponse.failureResponse(401, "Unauthorized");
            List<BookCategoryEntity> entities = bookCategoryRepository.findAllByAdminId(adminId);
            List<BookCategoryResponse> responses = entities.stream()
                    .map(BookCategoryMapper::toResponse)
                    .collect(Collectors.toList());
            return ServiceResponse.successResponse(200,
                    entities.isEmpty() ? ResponseMessages.NO_RECORD : "Fetched successfully", responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BookCategoryResponse> updateBookCategory(java.util.UUID adminId, java.util.UUID id,
            BookCategoryUpdateRequest request) {
        try {
            if (adminId == null)
                return ServiceResponse.failureResponse(401, "Unauthorized");
            Optional<BookCategoryEntity> existing = bookCategoryRepository.findById(id);
            if (existing.isEmpty())
                return ServiceResponse.failureResponse(404, "Not found");

            // Check ownership
            if (existing.get().getAdminId() == null)
                return ServiceResponse.failureResponse(403, "System categories cannot be modified");
            if (!existing.get().getAdminId().equals(adminId))
                return ServiceResponse.failureResponse(403, "Access denied");

            BookCategoryEntity entity = existing.get();
            BookCategoryMapper.toUpdateEntity(request, entity);
            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED,
                    BookCategoryMapper.toResponse(bookCategoryRepository.save(entity)));
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BookCategoryResponse> deleteBookCategory(java.util.UUID adminId, java.util.UUID id) {
        try {
            if (adminId == null)
                return ServiceResponse.failureResponse(401, "Unauthorized");
            Optional<BookCategoryEntity> existing = bookCategoryRepository.findById(id);
            if (existing.isEmpty())
                return ServiceResponse.failureResponse(404, "Not found");

            // Check ownership
            if (existing.get().getAdminId() == null)
                return ServiceResponse.failureResponse(403, "System categories cannot be deleted");
            if (!existing.get().getAdminId().equals(adminId))
                return ServiceResponse.failureResponse(403, "Access denied");

            bookCategoryRepository.deleteById(id);
            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BookResponse> saveBook(BookSaveRequest request) {

        try {
            if (request.getBusinessId() == null) {
                return ServiceResponse.failureResponse(400,
                        "businessId is required. Use the business UUID from the get-all-business API.");
            }
            // Unique per business
            Optional<BookEntity> existingBookName = bookRepository.findByBookNameAndBusinessId(request.getBookName(),
                    request.getBusinessId());
            if (existingBookName.isPresent()) {
                return ServiceResponse.failureResponse(409, "Book Name Already Exists for this business");
            }
            List<java.util.UUID> ids = request.getAssignedUserIds() == null
                    ? Collections.emptyList()
                    : request.getAssignedUserIds().stream().filter(id -> id != null).toList();
            Set<UserEntity> assignedUsers = ids.isEmpty()
                    ? new HashSet<>()
                    : userRepository.findAllById(ids).stream().collect(Collectors.toSet());

            // Convert DTO → Entity
            BookEntity bookEntity = BookMapper.toSaveEntity(request);
            bookEntity.setAssignedUsers(assignedUsers);

            // Persist entity
            BookEntity savedEntity = bookRepository.save(bookEntity);
            // Convert Entity → Response DTO
            BookResponse responseDto = BookMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<BookResponse>> getPaginatedBook(java.util.UUID businessId, int page,
            int size) {
        try {
            if (businessId == null) {
                return ServiceResponse.failureResponse(400,
                        "businessId is required. Use the business UUID from the get-all-business API.");
            }
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<BookEntity> bookPage;
            if (permissionScopeHelper.isAssignedScope("book", "view")
                    && permissionScopeHelper.getCurrentUserId().isPresent()) {
                bookPage = bookRepository.findByBusinessIdAndAssignedUserId(businessId,
                        permissionScopeHelper.getCurrentUserId().get(), pageRequest);
            } else {
                bookPage = bookRepository.findByBusinessId(businessId, pageRequest);
            }
            Page<BookResponse> bookResponsePage = bookPage.map(BookMapper::toResponse);
            PaginatedResponse<BookResponse> paginatedResponse = PaginationUtil
                    .createPaginatedResponse(bookResponsePage);
            return ServiceResponse.successResponse(200, "Book list fetched", paginatedResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Transactional
    public ServiceResponse<BookResponse> updateBook(java.util.UUID id, BookUpdateRequest request) {
        try {
            // Validation - Check if book exists
            Optional<BookEntity> existingBook = bookRepository.findById(id);
            if (existingBook.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Book Not Found");
            }

            BookEntity bookEntity = existingBook.get();

            // Fetch users if assignedUserIds are provided (filter nulls from legacy numeric
            // IDs)
            Set<UserEntity> usersToAssign = null;
            List<java.util.UUID> ids = request.getAssignedUserIds() == null
                    ? Collections.emptyList()
                    : request.getAssignedUserIds().stream().filter(uid -> uid != null).toList();
            if (!ids.isEmpty()) {
                usersToAssign = new HashSet<>(userRepository.findAllById(ids));
                if (usersToAssign.size() != ids.size()) {
                    return ServiceResponse.failureResponse(400, "One or more users not found");
                }
            }

            // Update entity using mapper
            BookMapper.toUpdateEntity(bookEntity, request, usersToAssign);

            // Persist entity
            BookEntity savedEntity = bookRepository.save(bookEntity);

            // Convert Entity → Response DTO
            BookResponse responseDto = BookMapper.toResponse(savedEntity);

            // Prepare standardized response
            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED, responseDto);

        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<BookResponse> deleteBook(java.util.UUID id) {
        try {

            // validation
            Optional<BookEntity> existingBook = bookRepository.findById(id);
            if (existingBook.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Book Not Found!");
            }
            // Delete entity
            bookRepository.deleteById(id);
            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaginatedResponse<BookResponse>> searchBook(java.util.UUID businessId, String searchTerm,
            int page, int size) {
        try {
            if (businessId == null) {
                return ServiceResponse.failureResponse(400,
                        "businessId is required. Use the business UUID from the get-all-business API.");
            }
            PageRequest pageRequest = PageRequest.of(page, size);
            Page<BookEntity> bookEntityPage;
            boolean assignedScope = permissionScopeHelper.isAssignedScope("book", "view");
            Optional<java.util.UUID> userId = permissionScopeHelper.getCurrentUserId();
            if (assignedScope && userId.isPresent()) {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    bookEntityPage = bookRepository.findByBusinessIdAndAssignedUserId(businessId, userId.get(),
                            pageRequest);
                } else {
                    bookEntityPage = bookRepository.findByBusinessIdAndAssignedUserIdAndBookNameContaining(businessId,
                            userId.get(), searchTerm.trim(), pageRequest);
                }
            } else {
                if (searchTerm == null || searchTerm.trim().isEmpty()) {
                    bookEntityPage = bookRepository.findByBusinessId(businessId, pageRequest);
                } else {
                    bookEntityPage = bookRepository.findByBusinessIdAndBookNameContainingIgnoreCase(businessId,
                            searchTerm.trim(), pageRequest);
                }
            }
            Page<BookResponse> dtoPage = bookEntityPage.map(BookMapper::toResponse);
            PaginatedResponse<BookResponse> paginatedResponse = PaginationUtil.createPaginatedResponse(dtoPage);
            return ServiceResponse.successResponse(200, "Book list fetched", paginatedResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    // ===================== Payment Mode =====================

    @Override
    public ServiceResponse<PaymentModeResponse> savePaymentMode(PaymentModeSaveRequest request) {
        try {
            // Validation
            Optional<PaymentModeEntity> existingPaymentMode = paymentModeRepository
                    .findByPaymentModeNameAndBusinessId(request.getPaymentModeName(), request.getBusinessId());
            if (existingPaymentMode.isPresent()) {
                return ServiceResponse.failureResponse(409, "Payment Mode Already Exists");
            }
            // Convert DTO → Entity
            PaymentModeEntity entity = PaymentModeMapper.toSaveEntity(request);
            // Persist entity
            PaymentModeEntity savedEntity = paymentModeRepository.save(entity);
            // Convert Entity → Response DTO
            PaymentModeResponse responseDto = PaymentModeMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(201, ResponseMessages.CREATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<List<PaymentModeResponse>> getPaymentModeList(java.util.UUID businessId) {
        try {
            if (businessId == null) {
                return ServiceResponse.failureResponse(400,
                        "businessId is required. Use the business UUID from the get-all-business API.");
            }
            if (permissionScopeHelper.isAssignedScope("business", "view")
                    && permissionScopeHelper.getCurrentUserId().isPresent()) {
                java.util.List<java.util.UUID> allowed = bookRepository
                        .findBusinessIdsByAssignedUserId(permissionScopeHelper.getCurrentUserId().get());
                if (!allowed.contains(businessId)) {
                    return ServiceResponse.failureResponse(403,
                            "Access denied: you can only view payment modes for assigned businesses.");
                }
            }
            List<PaymentModeEntity> entities = paymentModeRepository.findAllByBusinessId(businessId);
            if (entities.isEmpty()) {
                return ServiceResponse.failureResponse(204, ResponseMessages.NO_RECORD);
            }
            List<PaymentModeResponse> responses = entities.stream().map(PaymentModeMapper::toResponse)
                    .collect(Collectors.toList());
            return ServiceResponse.successResponse(200, "Payment Mode list fetched successfully", responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaymentModeResponse> updatePaymentMode(java.util.UUID id, PaymentModeUpdateRequest request) {
        try {
            // Validation
            Optional<PaymentModeEntity> existingPaymentMode = paymentModeRepository.findById(id);
            if (existingPaymentMode.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Payment Mode Not Found");
            }

            PaymentModeEntity entity = existingPaymentMode.get();
            PaymentModeMapper.toUpdateEntity(request, entity);
            // Persist entity
            PaymentModeEntity savedEntity = paymentModeRepository.save(entity);
            // Convert Entity → Response DTO
            PaymentModeResponse responseDto = PaymentModeMapper.toResponse(savedEntity);
            // Prepare standardized response
            return ServiceResponse.successResponse(200, ResponseMessages.UPDATED, responseDto);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

    @Override
    public ServiceResponse<PaymentModeResponse> deletePaymentMode(java.util.UUID id) {
        try {
            // Validation
            Optional<PaymentModeEntity> existingPaymentMode = paymentModeRepository.findById(id);
            if (existingPaymentMode.isEmpty()) {
                return ServiceResponse.failureResponse(404, "Payment Mode Not Found!");
            }
            // Delete entity
            paymentModeRepository.deleteById(id);
            return ServiceResponse.successResponse(200, ResponseMessages.DELETED, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ServiceResponse.failureResponse(500, ResponseMessages.INTERNAL_ERROR);
        }
    }

}
