package com.john.ledger.entry.service;

import com.john.ledger.common.util.PaginatedResponse;
import com.john.ledger.common.util.ServiceResponse;
import com.john.ledger.entry.dto.request.*;
import com.john.ledger.entry.dto.response.BookCategoryResponse;
import com.john.ledger.entry.dto.response.BookResponse;
import com.john.ledger.entry.dto.response.PaymentModeResponse;


import java.util.List;
import java.util.UUID;

public interface IBookService {

    ServiceResponse<BookCategoryResponse> saveBookCategory(BookCategorySaveRequest request);

    ServiceResponse<List<BookCategoryResponse>> getBookCategoryList(UUID businessId);

    ServiceResponse<BookCategoryResponse> updateBookCategory(UUID id, BookCategoryUpdateRequest request);

    ServiceResponse<BookCategoryResponse> deleteBookCategory(UUID id);

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
