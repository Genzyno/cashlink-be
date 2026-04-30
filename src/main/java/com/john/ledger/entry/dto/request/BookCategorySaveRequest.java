package com.john.ledger.entry.dto.request;

import com.john.ledger.common.enums.CategoryType;
import com.john.ledger.common.enums.Status;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCategorySaveRequest {

    private java.util.UUID businessId;
    private String categoryName;
    private CategoryType categoryType;
    private String colorCode;
    private Status status;
}
