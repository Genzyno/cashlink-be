package com.john.ledger.entry.dto.response;

import com.john.ledger.common.enums.CategoryType;
import com.john.ledger.common.enums.Status;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategoryResponse {

    private java.util.UUID id;
    private java.util.UUID businessId;
    private String categoryName;
    private CategoryType categoryType;
    private String colorCode;
    private Status status;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private java.util.UUID adminId;
}
