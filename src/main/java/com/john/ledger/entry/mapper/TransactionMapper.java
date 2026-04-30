package com.john.ledger.entry.mapper;

import com.john.ledger.entry.dto.request.TransactionSaveRequest;
import com.john.ledger.entry.dto.request.TransactionUpdateRequest;
import com.john.ledger.entry.dto.response.TransactionFileResponse;
import com.john.ledger.entry.dto.response.TransactionResponse;
import com.john.ledger.entry.entity.TransactionEntity;
import com.john.ledger.entry.entity.TransactionFileEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionMapper {

    public static TransactionEntity toSaveEntity(TransactionSaveRequest request) {

        TransactionEntity entity = new TransactionEntity();

        entity.setBusinessId(request.getBusinessId());
        entity.setBookId(request.getBookId());
        entity.setTransactionType(request.getTransactionType());
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        entity.setAmount(request.getAmount());
        entity.setRemarks(request.getRemarks());
        entity.setCategoryId(request.getCategoryId());
        entity.setPaymentModeId(request.getPaymentModeId());

        return entity;
    }

    public static void toUpdateEntity(TransactionUpdateRequest request, TransactionEntity entity) {

        entity.setBusinessId(request.getBusinessId());
        entity.setBookId(request.getBookId());
        entity.setTransactionType(request.getTransactionType());
        entity.setDate(request.getDate());
        entity.setTime(request.getTime());
        entity.setAmount(request.getAmount());
        entity.setRemarks(request.getRemarks());
        entity.setCategoryId(request.getCategoryId());
        entity.setPaymentModeId(request.getPaymentModeId());
    }

    public static TransactionResponse toResponse(TransactionEntity entity) {

        TransactionResponse response = new TransactionResponse();

        response.setId(entity.getId());
        response.setBusinessId(entity.getBusinessId());
        response.setBookId(entity.getBookId());
        response.setTransactionType(entity.getTransactionType());
        response.setDate(entity.getDate());
        response.setTime(entity.getTime());
        response.setTransactionAt(entity.getDate() != null && entity.getTime() != null
                ? LocalDateTime.of(entity.getDate(), entity.getTime())
                : null);
        response.setAmount(entity.getAmount());
        response.setRemarks(entity.getRemarks());
        response.setCategoryId(entity.getCategoryId());
        response.setPaymentModeId(entity.getPaymentModeId());
        response.setCreatedTime(entity.getCreatedTime());
        response.setUpdatedTime(entity.getUpdatedTime());
        response.setCreatedByUserId(entity.getCreatedByUserId());
        response.setUpdatedByUserId(entity.getUpdatedByUserId());

        if (entity.getBillFiles() != null && !entity.getBillFiles().isEmpty()) {
            response.setBillFiles(entity.getBillFiles().stream()
                    .map(TransactionMapper::toFileResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setBillFiles(Collections.emptyList());
        }

        return response;
    }

    public static TransactionFileResponse toFileResponse(TransactionFileEntity fileEntity) {

        TransactionFileResponse response = new TransactionFileResponse();

        response.setId(fileEntity.getId());
        response.setFileName(fileEntity.getFileName());
        response.setFilePath(fileEntity.getFilePath());
        response.setFileType(fileEntity.getFileType());
        response.setFileSize(fileEntity.getFileSize());
        response.setDownloadUrl("/myledger-api/transaction/file/download/" + fileEntity.getId());
        response.setViewUrl("/myledger-api/transaction/file/view/" + fileEntity.getId());
        response.setCreatedTime(fileEntity.getCreatedTime());

        return response;
    }
}
