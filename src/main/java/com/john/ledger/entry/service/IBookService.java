package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.*;
import com.john.ledger.entry.dto.response.BookCategoryResponse;
import com.john.ledger.entry.dto.response.BookResponse;
import com.john.ledger.entry.dto.response.PaymentModeResponse;

import java.util.*;

public interface IBookService {

    // ===================== Book Category =====================
    ServiceResponse<BookCategoryResponse> saveBookCategory(java.util.UUID adminId, BookCategorySaveRequest request);

    ServiceResponse<List<BookCategoryResponse>> getBookCategoryList(java.util.UUID adminId, java.util.UUID businessId);

    ServiceResponse<BookCategoryResponse> updateBookCategory(java.util.UUID adminId, java.util.UUID id,
            BookCategoryUpdateRequest request);

    ServiceResponse<BookCategoryResponse> deleteBookCategory(java.util.UUID adminId, java.util.UUID id);

    ServiceResponse<BookResponse> saveBook(BookSaveRequest request);

    ServiceResponse<PaginatedResponse<BookResponse>> getPaginatedBook(UUID businessId, int page, int size);

    ServiceResponse<BookResponse> updateBook(UUID id, BookUpdateRequest request);

    ServiceResponse<BookResponse> deleteBook(UUID id);

    ServiceResponse<PaginatedResponse<BookResponse>> searchBook(UUID businessId, String searchTerm, int page, int size);

    // Payment Mode
    ServiceResponse<PaymentModeResponse> savePaymentMode(PaymentModeSaveRequest request);

    ServiceResponse<List<PaymentModeResponse>> getPaymentModeList(UUID businessId);

    ServiceResponse<PaymentModeResponse> updatePaymentMode(UUID id, PaymentModeUpdateRequest request);

    ServiceResponse<PaymentModeResponse> deletePaymentMode(UUID id);
}
