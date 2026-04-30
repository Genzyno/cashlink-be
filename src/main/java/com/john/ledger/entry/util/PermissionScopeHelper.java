package com.john.ledger.entry.util;

import com.john.ledger.common.util.CurrentUserHolder;
import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.entity.UserEntity;
import com.john.ledger.entry.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Resolves permission scope ("all" | "assigned") for the current user per screen/action.
 * Used to filter lists (e.g. show only books assigned to the user when scope is "assigned").
 */
@Component
public class PermissionScopeHelper {

    @Autowired
    private UserRepository userRepository;

    /**
     * Returns the scope for the given screen and action for the current request user.
     * "all" = full access; "assigned" = only records assigned to the user.
     */
    public String getScope(String screen, String action) {
        Optional<UUID> userId = CurrentUserHolder.getUserId();
        if (userId.isEmpty()) return "all";
        Optional<UserEntity> userOpt = userRepository.findById(userId.get());
        if (userOpt.isEmpty()) return "all";
        RoleEntity role = userOpt.get().getRoleEntity();
        if (role == null || role.getPermissionScopes() == null) return "all";
        Map<String, String> screenScopes = role.getPermissionScopes().get(screen);
        if (screenScopes == null) return "all";
        return screenScopes.getOrDefault(action, "all");
    }

    public boolean isAssignedScope(String screen, String action) {
        return "assigned".equalsIgnoreCase(getScope(screen, action));
    }

    public Optional<UUID> getCurrentUserId() {
        return CurrentUserHolder.getUserId();
    }
}
