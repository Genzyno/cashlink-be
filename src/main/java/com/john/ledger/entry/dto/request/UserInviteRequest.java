package com.john.ledger.entry.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInviteRequest {

    /** Required. One or more invitees (email + optional role per row). */
    private List<InviteItem> invites;

    /** When true, grant access to all books for the given business. Requires businessId. */
    private Boolean allBooks;

    /** When allBooks is not set, these book IDs are granted. Empty/omit = no book access. */
    private List<UUID> bookIds;

    /** Required when allBooks is true – UUID of the current business. */
    private UUID businessId;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InviteItem {
        private String email;
        private UUID roleId;
    }
}
