package com.john.ledger.entry.dto.response;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private UUID id;
    private String roleName;
    private Map<String, List<String>> permissions;
    /** Per-screen action scope: "all" | "assigned". Omit or empty treated as "all". */
    private Map<String, Map<String, String>> permissionScopes;
}
