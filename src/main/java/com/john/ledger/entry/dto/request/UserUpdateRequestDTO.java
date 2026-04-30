package com.john.ledger.entry.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UserUpdateRequestDTO {
    private UUID id;
    private String userEmail;
    private String userName;
    private String userMobile;
    private UUID roleId;
    private Boolean status;
}
