package com.john.ledger.entry.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionFileResponse {

    private java.util.UUID id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String downloadUrl;
    private String viewUrl;
    private LocalDateTime createdTime;
}
