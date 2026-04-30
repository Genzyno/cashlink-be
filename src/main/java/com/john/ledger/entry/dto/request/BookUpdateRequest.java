package com.john.ledger.entry.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class BookUpdateRequest {

    private java.util.UUID id;
    private String bookName;
    private List<java.util.UUID> assignedUserIds;
    private java.util.UUID businessId;
}
