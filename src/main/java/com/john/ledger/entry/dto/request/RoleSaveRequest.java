package com.john.ledger.entry.dto.request;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSaveRequest {
    private String roleName;
    private Map<String, List<String>> permissions;
    /** Optional. Per-screen action scope: "all" | "assigned". */
    private Map<String, Map<String, String>> permissionScopes;
}
