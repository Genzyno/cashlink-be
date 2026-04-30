package com.john.ledger.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerAuthUserInfo {
    private String id;
    private String name;
    private String email;
    private String roleName;
    private Map<String, List<String>> permissions;
    /** Per-screen action scope: "all" | "assigned". Used to filter data (e.g. show only assigned books). */
    private Map<String, Map<String, String>> permissionScopes;
}
