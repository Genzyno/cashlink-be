package com.john.ledger.entry.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.UUID;

/**
 * One accepted (or rejected) invite notification for the inviter's bell/list.
 * The logged-in user sees invites they sent that were accepted or rejected.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Accepted or rejected invite notification for the user who sent the invite")
public class AcceptedInviteNotificationResponse {

    @Schema(description = "Unique id for the notification (invite id)")
    private String id;

    @Schema(description = "Id of the user who accepted (null if rejected)")
    private String userId;

    @Schema(description = "Display name of the user who accepted, or email if rejected")
    private String userName;

    @Schema(description = "Email of the invitee")
    private String userEmail;

    @Schema(description = "ISO 8601 date-time when the invite was accepted")
    private String acceptedAt;

    @Schema(description = "ISO 8601 date-time when the invite was rejected")
    private String rejectedAt;

    @Schema(description = "Status: ACCEPTED or REJECTED")
    private String status;

    @Schema(description = "Id of the user who sent the invite")
    private String invitedByUserId;

    @Schema(description = "Business context (from first book of invite)")
    private String businessId;

    @Schema(description = "Business display name")
    private String businessName;

    @Schema(description = "Role assigned to the user")
    private String roleName;
}
