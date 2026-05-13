package com.john.ledger.entry.controller;

import com.john.ledger.common.util.CurrentUserHolder;
import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.*;
import com.john.ledger.entry.dto.response.BookCategoryResponse;
import com.john.ledger.entry.dto.response.BookResponse;
import com.john.ledger.entry.dto.response.PaymentModeResponse;
import com.john.ledger.entry.service.IBookService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("book")
public class BookController {

    @Autowired
    private IBookService bookService;


    @Operation(summary = "Save Book Category (body includes businessId)")
    @PostMapping("/save-book-category")
    public ResponseEntity<ServiceResponse<BookCategoryResponse>> saveBookCategory(@RequestBody BookCategorySaveRequest request) {
        Optional<UUID> adminIdOpt = CurrentUserHolder.getUserId();
        if (adminIdOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ServiceResponse<BookCategoryResponse> response = null;
        try {
            response = bookService.saveBookCategory(adminIdOpt.get(), request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Save Book Category with businessId in path (POST with JSON body: categoryName, categoryType, colorCode, status)")
    @PostMapping("/save-book-category/{businessId}")
    public ResponseEntity<ServiceResponse<BookCategoryResponse>> saveBookCategoryWithPath(
            @PathVariable Optional<UUID> businessId,
            @RequestBody BookCategorySaveRequest request) {
        Optional<UUID> adminIdOpt = CurrentUserHolder.getUserId();
        if (adminIdOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (businessId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "businessId must be a valid UUID. Use the UUID from the get-all-business API."));
        }
        request.setBusinessId(businessId.get());
        ServiceResponse<BookCategoryResponse> response = null;
        try {
            response = bookService.saveBookCategory(adminIdOpt.get(), request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Get All Book Category")
    @GetMapping("/get-all-book-category/{businessId}")
    public ResponseEntity<ServiceResponse<List<BookCategoryResponse>>> getBookCategoryList(@PathVariable Optional<UUID> businessId) {
        Optional<UUID> adminIdOpt = CurrentUserHolder.getUserId();
        if (adminIdOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        if (businessId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "businessId must be a valid UUID. Use the UUID from the get-all-business API."));
        }
        try {
            ServiceResponse<List<BookCategoryResponse>> res = bookService.getBookCategoryList(adminIdOpt.get(), businessId.get());
            return ResponseEntity.status(HttpStatus.valueOf(res.getStatusCode())).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Update Book Category Object")
    @PutMapping("/update-book-category/{id}")
    public ResponseEntity<ServiceResponse<BookCategoryResponse>> updateBookCategory(@PathVariable UUID id, @RequestBody BookCategoryUpdateRequest request) {
        Optional<UUID> adminIdOpt = CurrentUserHolder.getUserId();
        if (adminIdOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ServiceResponse<BookCategoryResponse> response = null;
        try {
            response = bookService.updateBookCategory(adminIdOpt.get(), id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete Book Category Object")
    @DeleteMapping("/delete-book-category/{id}")
    public ResponseEntity<ServiceResponse<BookCategoryResponse>> deleteBookCategory(@PathVariable UUID id) {
        Optional<UUID> adminIdOpt = CurrentUserHolder.getUserId();
        if (adminIdOpt.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        ServiceResponse<BookCategoryResponse> response = null;
        try {
            response = bookService.deleteBookCategory(adminIdOpt.get(), id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Save Book Object")
    @PostMapping("/save-book")
    public ResponseEntity<ServiceResponse<BookResponse>> saveBook(@RequestBody BookSaveRequest request) {

        ServiceResponse<BookResponse> response = null;
        try {
            response = bookService.saveBook(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Get paginated book list by business")
    @GetMapping("/get-all-book")
    public ResponseEntity<ServiceResponse<PaginatedResponse<BookResponse>>> getBook(
            @RequestParam UUID businessId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<BookResponse>> response = null;
        try {
            response = bookService.getPaginatedBook(businessId, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Update Book Object")
    @PutMapping("/update-book/{id}")
    public ResponseEntity<ServiceResponse<BookResponse>> updateBook(@PathVariable UUID id, @RequestBody BookUpdateRequest request) {

        ServiceResponse<BookResponse> response = null;
        try {
            response = bookService.updateBook(id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete Book Object")
    @DeleteMapping("/delete-book/{id}")
    public ResponseEntity<ServiceResponse<BookResponse>> deleteBook(@PathVariable UUID id) {

        ServiceResponse<BookResponse> response = null;
        try {
            response = bookService.deleteBook(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }


    @Operation(summary = "Search book list by business")
    @GetMapping("/search-book")
    public ResponseEntity<ServiceResponse<PaginatedResponse<BookResponse>>> searchBook(
            @RequestParam UUID businessId,
            @RequestParam(required = false, defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        ServiceResponse<PaginatedResponse<BookResponse>> response = null;
        try {
            response = bookService.searchBook(businessId, searchTerm == null ? "" : searchTerm, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    // ===================== Payment Mode =====================

    @Operation(summary = "Save Payment Mode Object")
    @PostMapping("/save-payment-mode")
    public ResponseEntity<ServiceResponse<PaymentModeResponse>> savePaymentMode(@RequestBody PaymentModeSaveRequest request) {

        ServiceResponse<PaymentModeResponse> response = null;
        try {
            response = bookService.savePaymentMode(request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Get All Payment Modes by Business")
    @GetMapping("/get-all-payment-mode/{businessId}")
    public ResponseEntity<ServiceResponse<List<PaymentModeResponse>>> getPaymentModeList(@PathVariable Optional<UUID> businessId) {
        if (businessId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServiceResponse.failureResponse(400, "businessId must be a valid UUID. Use the UUID from the get-all-business API."));
        }
        try {
            ServiceResponse<List<PaymentModeResponse>> res = bookService.getPaymentModeList(businessId.get());
            return ResponseEntity.status(HttpStatus.valueOf(res.getStatusCode())).body(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Operation(summary = "Update Payment Mode Object")
    @PutMapping("/update-payment-mode/{id}")
    public ResponseEntity<ServiceResponse<PaymentModeResponse>> updatePaymentMode(@PathVariable UUID id, @RequestBody PaymentModeUpdateRequest request) {

        ServiceResponse<PaymentModeResponse> response = null;
        try {
            response = bookService.updatePaymentMode(id, request);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

    @Operation(summary = "Delete Payment Mode Object")
    @DeleteMapping("/delete-payment-mode/{id}")
    public ResponseEntity<ServiceResponse<PaymentModeResponse>> deletePaymentMode(@PathVariable UUID id) {

        ServiceResponse<PaymentModeResponse> response = null;
        try {
            response = bookService.deletePaymentMode(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        return ResponseEntity.status(HttpStatus.valueOf(response.getStatusCode())).body(response);
    }

}
