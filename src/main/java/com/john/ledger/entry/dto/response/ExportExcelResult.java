package com.john.ledger.entry.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * Result of transaction export to Excel.
 * On success: fileData and filename are set; on failure: statusCode and message.
 */
@Getter
@Builder
public class ExportExcelResult {

    private final boolean success;
    private final int statusCode;
    private final String message;
    private final byte[] fileData;
    private final String filename;

    public static ExportExcelResult ok(byte[] fileData, String filename) {
        return ExportExcelResult.builder()
                .success(true)
                .statusCode(200)
                .message("Export ready")
                .fileData(fileData)
                .filename(filename != null ? filename : "transactions_export.xlsx")
                .build();
    }

    public static ExportExcelResult failure(int statusCode, String message) {
        return ExportExcelResult.builder()
                .success(false)
                .statusCode(statusCode)
                .message(message)
                .fileData(null)
                .filename(null)
                .build();
    }
}
