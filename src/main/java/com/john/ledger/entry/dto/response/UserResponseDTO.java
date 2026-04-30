package com.john.ledger.entry.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID id;
    private String userName;
    private String userEmail;
    private String userMobile;
    private UUID roleId;
    private String roleName;
    private Boolean status;
}
