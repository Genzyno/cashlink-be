# User Invite â€“ UI Integration Guide

Base URL for all APIs: **`{apiBaseUrl}`** (from `environment.apiUrl`)  
Example: `https://your-api.com/cashlink-api` (no trailing slash)  
Local: `http://localhost:8080/cashlink-api`

**See also:** [Add Member to this book](UI_INTEGRATION_ADD_MEMBER_TO_BOOK.md) â€“ drawer on Transactions page (get users, get books, update book members, send invite to this book).

---

## 1. TypeScript DTOs / Interfaces

Located in `src/app/dto/users/`:

- **UserInviteRequest.ts** â€“ `UserInviteItem` (email, roleId?), `UserInviteRequest` (invites[], allBooks?, bookIds?, businessId?)
- **AcceptInviteRequest.ts** â€“ `AcceptInviteRequest` (token, userName, password, userMobile?)
- **RejectInviteRequest.ts** â€“ `RejectInviteRequest` (token)
- **InviteDetailsResponse.ts** â€“ `InviteDetailsResponse` (email, roleId, roleName, valid, message)
- **AcceptInviteResponse.ts** â€“ `AcceptInviteResponse` (id, userName, userEmail, userMobile, roleId, roleName, status)

---

## 2. API Endpoints Summary

| Action              | Method | Endpoint                          | Auth required |
|---------------------|--------|-----------------------------------|----------------|
| Send invite         | POST   | `{apiUrl}/users/send-invite`      | Yes (Bearer)   |
| Get invite details  | GET    | `{apiUrl}/users/invite-by-token?token=` | No   |
| Accept invite       | POST   | `{apiUrl}/users/accept-invite`   | No             |
| Reject invite       | POST   | `{apiUrl}/users/reject-invite`   | No             |

---

## 3. Send Invite (Team / User Management screen)

**When:** User clicks "Invite Users", adds one or more rows (email + role per row), then "Send invite".

**Request**

- **URL:** `POST {apiUrl}/users/send-invite`
- **Headers:** `Content-Type: application/json`, `Authorization: Bearer <accessToken>`
- **Body:**
  - `invites` (required): `[ { "email": "...", "roleId": "..." | null }, ... ]`
  - `allBooks` (optional): `true` to grant access to all books for the given business. Omit or `false` when using `bookIds`.
  - `bookIds` (optional): `["id1", "id2", ...]` â€“ book IDs to grant when `allBooks` is not set. Empty or omit = no book access.
  - `businessId` (required when `allBooks` is true): business context for book access. Frontend sends the currently selected business from the header.

  Example: `{ "invites": [{ "email": "a@b.com", "roleId": "role-1" }], "allBooks": true, "businessId": "business-uuid" }`  
  Or: `{ "invites": [...], "bookIds": ["book-1", "book-2"], "businessId": "business-uuid" }`

**Success:** `200` â€“ show `message`, close drawer, refresh list.  
**Error:** Show `response.body.message` (or generic "Failed to send invites") in toast.

---

## 4. Get Invite Details (Accept-invite page load)

**When:** User opens link from email: `{frontendUrl}/accept-invite?token=...`

**Request**

- **URL:** `GET {apiUrl}/users/invite-by-token?token={token}`
- **Headers:** None (no auth).

**Success 200:** `data.valid === true` â†’ show email + role, show form.  
**Success 200, invalid:** `data.valid === false` â†’ show `data.message`, hide form.

---

## 5. Accept Invite (Create account)

**When:** User fills form and clicks "Accept invitation".

**Request**

- **URL:** `POST {apiUrl}/users/accept-invite`
- **Headers:** `Content-Type: application/json` (no auth).
- **Body:** `{ "token": "...", "userName": "...", "password": "...", "userMobile": "..." | null }`

**Success 201:** Show success, redirect to `/passport/login` with `?email=...` for pre-fill.  
**Error:** Show `response.body.message`.

---

## 6. Reject Invite (Decline invitation)

**When:** User clicks the "Reject invitation" link in the email: `{frontendUrl}/reject-invite?token=...`  
Rejecting sets the invite to REJECTED so **no account is created** and the **admin can send a new invite** to the same email later.

**Request**

- **URL:** `POST {apiUrl}/users/reject-invite`
- **Headers:** `Content-Type: application/json` (no auth).
- **Body:** `{ "token": "..." }`

**Success 200:** Show "You have declined the invitation. The admin can send you a new invite if needed." and link to login.  
**Error:** Show `response.body.message`.

---

## 7. Frontend implementation

- **Send invite:** `UserService.sendInvite(payload)` â€“ Team screen invite drawer.
- **Get invite:** `UserService.getInviteByToken(token)` â€“ accept-invite page (no auth; `X-Skip-Auth: true`).
- **Accept invite:** `UserService.acceptInvite(payload)` â€“ accept-invite form (no auth; `X-Skip-Auth: true`).
- **Reject invite:** `UserService.rejectInvite(payload)` â€“ reject-invite page (no auth; `X-Skip-Auth: true`).
- **Routes:** `/accept-invite` (token from query â†’ get details â†’ form â†’ accept); `/reject-invite` (token from query â†’ call reject â†’ show success + link to login).

---

## 8. Email link format

Backend sends two links in the invite email:

- **Accept:** `{app.frontend.url}/accept-invite?token={token}`
- **Reject:** `{app.frontend.url}/reject-invite?token={token}`

Example: `http://localhost:4200/accept-invite?token=abc123...` and `http://localhost:4200/reject-invite?token=abc123...`

Ensure frontend base URL and routes match backend `app.frontend.url`.
