package com.john.ledger.entry.dto.request;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleUpdateRequest {
    private UUID id;
    private String roleName;
    private Map<String, List<String>> permissions;
    private Map<String, Map<String, String>> permissionScopes;
}
