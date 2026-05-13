# Add Member to this book â€“ Endpoints & Payload

Used by the **Add Member to this book** drawer on the Transactions page (per-book members + invite).

Base URL: **`{apiBaseUrl}`** (e.g. `http://localhost:8080/cashlink-api`).

---

## 1. Get all users (for member list)

**When:** Opening the "Add Member to this book" drawer.

| Item     | Value |
|----------|--------|
| **Method** | GET |
| **Endpoint** | `{apiUrl}/users/get-all-user?page=0&size=500` |
| **Optional query** | `assignedOnly=true` (when user scope is assigned-only; backend may support in future) |
| **Headers** | `Authorization: Bearer <accessToken>` |

**Response (example):**

- `statusCode`: 200  
- `data.content`: array of `UserResponseDTO`  
- `data.meta`: `totalElements`, etc.

**Payload:** None.

---

## 2. Get all books (to resolve current book & assignedUserIds)

**When:** Opening the "Add Member to this book" drawer (after users load). Used to find the current book by `bookId` and get `assignedUserIds` / `bookName` for the update.

| Item     | Value |
|----------|--------|
| **Method** | GET |
| **Endpoint** | `{apiUrl}/book/get-all-book?businessId={businessId}&page=0&size=500` |
| **Optional query** | `assignedOnly=true` (when supported) |
| **Headers** | `Authorization: Bearer <accessToken>` |

**Response (example):**

- `statusCode`: 200  
- `data.content`: array of `BookResponse` (`id`, `bookName`, `assignedUserIds[]`, `businessId`)  
- `data.meta`: pagination

**Payload:** None.

---

## 3. Update book (save members)

**When:** User clicks **Save members** in the drawer.

| Item     | Value |
|----------|--------|
| **Method** | PUT |
| **Endpoint** | `{apiUrl}/book/update-book/{bookId}` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <accessToken>` |

**Payload (body):** `BookUpdateRequest`

```json
{
  "id": "<book-uuid>",
  "bookName": "My Ledger",
  "assignedUserIds": ["<user-uuid-1>", "<user-uuid-2>"],
  "businessId": "<business-uuid>"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | string (UUID) | Book UUID (same as path `bookId`) |
| `bookName` | string | Current book name (unchanged) |
| `assignedUserIds` | string[] (UUIDs) | Full list of user IDs that should have access to this book |
| `businessId` | string (UUID) | Current business context |

**Success:** `200` â€“ show success message, keep drawer open with updated selection.  
**Error:** Show `response.body.message` or generic "Failed to update members".

---

## 4. Send invite (Invite new users to this book)

**When:** User clicks **Invite new users** in the drawer, fills emails (and optional role), then **Send invite**.

| Item     | Value |
|----------|--------|
| **Method** | POST |
| **Endpoint** | `{apiUrl}/users/send-invite` |
| **Headers** | `Content-Type: application/json`, `Authorization: Bearer <accessToken>` |

**Payload (body):** `UserInviteRequest`

```json
{
  "invites": [
    { "email": "user@example.com", "roleId": "<role-uuid-or-null>" }
  ],
  "bookIds": ["<current-book-uuid>"],
  "businessId": "<business-uuid>"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `invites` | array | List of `{ email, roleId? }` |
| `bookIds` | string[] | For this flow: `[currentBookId]` so invitees get access to this book only |
| `businessId` | string (UUID) | Current business (from header) |
| `allBooks` | boolean | Not used in "Add Member to this book"; omit or `false` |

**Success:** `200` / `201` â€“ show success, close invite drawer, reload users in members drawer.  
**Error:** Show `response.body.message` or "Failed to send invites".

---

## Summary

| Action | Method | Endpoint | Payload |
|--------|--------|----------|---------|
| Load users for list | GET | `users/get-all-user?page=0&size=500` | â€” |
| Load books (get current book) | GET | `book/get-all-book?businessId=...&page=0&size=500` | â€” |
| Save members | PUT | `book/update-book/{bookId}` | `BookUpdateRequest` |
| Invite new users to book | POST | `users/send-invite` | `UserInviteRequest` (with `bookIds: [bookId]`) |

---

## Backend reference

- **UserController:** `GET /users/get-all-user`, `POST /users/send-invite`
- **BookController:** `GET /book/get-all-book`, `PUT /book/update-book/{id}`
- **DTOs:** `BookUpdateRequest` (id, bookName, assignedUserIds, businessId), `UserInviteRequest` (invites, bookIds, businessId; allBooks optional). See `UI_INTEGRATION_USER_INVITE.md` for full invite payload.
