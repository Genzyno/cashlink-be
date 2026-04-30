package com.john.ledger.config;

import com.john.ledger.entry.entity.RoleEntity;
import com.john.ledger.entry.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class DefaultRolesSeeder {

    private static final Logger log = LoggerFactory.getLogger(DefaultRolesSeeder.class);

    private static final String SUPER_ADMIN = "Super Admin";

    /** Contract screen keys: dashboard, business, team, book, bookCategory, transactions, roleMaster */
    private static final List<String> SCREENS = List.of("dashboard", "business", "team", "book", "bookCategory", "transactions", "roleMaster");
    private static final List<String> VIEW_ONLY = List.of("view");
    private static final List<String> ALL_ACTIONS = List.of("view", "create", "update", "delete");

    @Bean
    @Order(1)
    CommandLineRunner seedDefaultRoles(RoleRepository roleRepository) {
        return args -> {
            roleRepository.findByRoleName(SUPER_ADMIN).ifPresentOrElse(
                    existing -> {
                        existing.setPermissions(allPermissions());
                        existing.setPermissionScopes(fullAccessScopes());
                        roleRepository.save(existing);
                        log.info("Super Admin role synced with full access (roleMaster & team create/delete)");
                    },
                    () -> seedRoleIfMissing(roleRepository, SUPER_ADMIN, allPermissions(), fullAccessScopes()));
        };
    }

    private void seedRoleIfMissing(RoleRepository roleRepository, String roleName,
                                   Map<String, List<String>> permissions,
                                   Map<String, Map<String, String>> permissionScopes) {
        RoleEntity role = RoleEntity.builder()
                .roleName(roleName)
                .permissions(permissions)
                .permissionScopes(permissionScopes)
                .build();
        roleRepository.save(role);
        log.info("Default role seeded: {}", roleName);
    }

    private static Map<String, List<String>> allPermissions() {
        return SCREENS.stream()
                .collect(Collectors.toMap(
                        s -> s,
                        s -> "dashboard".equals(s) ? List.copyOf(VIEW_ONLY) : List.copyOf(ALL_ACTIONS),
                        (a, b) -> a, LinkedHashMap::new));
    }

    /** Full access: every action has scope "all" (no "assigned" restriction). */
    private static Map<String, Map<String, String>> fullAccessScopes() {
        Map<String, Map<String, String>> scopes = new LinkedHashMap<>();
        scopes.put("dashboard", Map.of("view", "all"));
        for (String screen : List.of("business", "team", "book", "bookCategory", "transactions", "roleMaster")) {
            scopes.put(screen, Map.of("view", "all", "create", "all", "update", "all", "delete", "all"));
        }
        return scopes;
    }
}
