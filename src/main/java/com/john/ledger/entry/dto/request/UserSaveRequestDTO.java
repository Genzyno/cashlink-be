package com.john.ledger.entry.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UserSaveRequestDTO {
    private String userName;
    private String userEmail;
    private String userMobile;
    private UUID roleId;
}
