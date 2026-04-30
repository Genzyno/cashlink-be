# My Ledger – All API Endpoints (End-to-End)

**Full base URL:** `http://localhost:8080/myledger-api` or `https://<your-host>/myledger-api`

**Android:** Use your PC’s LAN IP or emulator `10.0.2.2`, not `localhost`. See **`docs/ANDROID_API_CONNECTIVITY.md`**.

**Health (no auth):** `GET /myledger-api/public/ping` — returns `{"status":"ok",...}` to verify the API is reachable.

**Auth (protected endpoints):** Send header:
```http
Authorization: Bearer <accessToken>
```
Optional for some APIs: `X-Logged-User-Id: <user-uuid>` (must match token).

---

## Auth – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/myledger-api/auth/send-otp` | No | Send OTP to email |
| POST | `/myledger-api/auth/verify-otp` | No | Verify OTP and login/register |
| POST | `/myledger-api/auth/logout` | Optional | Logout (then discard tokens on client) |
| POST | `/myledger-api/auth/refresh` | No | Refresh access token |
| GET | `/myledger-api/auth/me` | **Yes** | Get current user info |
| GET | `/myledger-api/auth/google` | No | Redirect to Google Sign-In |
| POST | `/myledger-api/auth/google/id-token` | No | Android/Flutter: verify Google ID token, return JWTs |
| GET | `/myledger-api/auth/google/callback` | No | Google OAuth callback |
| POST | `/myledger-api/auth/forgot-password` | No | Send password reset link |
| POST | `/myledger-api/auth/send-verification-email` | No | Send email verification link |
| POST | `/myledger-api/auth/verify-email` | No | Verify email (body: token) |
| GET | `/myledger-api/auth/verify-email` | No | Verify email (query: token) |

---

## Users – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/myledger-api/users/save-user` | **Yes** | Create user |
| GET | `/myledger-api/users/get-all-user` | **Yes** | Paginated user list |
| PUT | `/myledger-api/users/update-user/{id}` | **Yes** | Update user |
| DELETE | `/myledger-api/users/delete-user/{id}` | **Yes** | Delete user |
| GET | `/myledger-api/users/search-user` | **Yes** | Search users (query: searchTerm, page, size) |
| POST | `/myledger-api/users/send-invite` | **Yes** | Send invite emails |
| GET | `/myledger-api/users/invite-by-token` | No | Get invite details (query: token) |
| POST | `/myledger-api/users/accept-invite` | No | Accept invitation |
| POST | `/myledger-api/users/reject-invite` | No | Reject invitation |
| GET | `/myledger-api/users/accepted-invite-notifications` | **Yes** | Accepted/rejected invites (query: page, size, businessId) |

---

## Business – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/myledger-api/business/save-business-type` | **Yes** | Create business type |
| GET | `/myledger-api/business/get-all-business-type` | **Yes** | List business types |
| PUT | `/myledger-api/business/update-business-type/{id}` | **Yes** | Update business type |
| DELETE | `/myledger-api/business/delete-business-type/{id}` | **Yes** | Delete business type |
| POST | `/myledger-api/business/save-business` | **Yes** | Create business |
| GET | `/myledger-api/business/get-all-business` | **Yes** | Paginated business list |
| PUT | `/myledger-api/business/update-business/{id}` | **Yes** | Update business |
| DELETE | `/myledger-api/business/delete-business/{id}` | **Yes** | Delete business |
| GET | `/myledger-api/business/search-business` | **Yes** | Search businesses (query: searchTerm, page, size) |

---

## Book – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/myledger-api/book/save-book-category` | **Yes** | Create book category |
| POST | `/myledger-api/book/save-book-category/{businessId}` | **Yes** | Create book category for business |
| GET | `/myledger-api/book/get-all-book-category/{businessId}` | **Yes** | List book categories |
| PUT | `/myledger-api/book/update-book-category/{id}` | **Yes** | Update book category |
| DELETE | `/myledger-api/book/delete-book-category/{id}` | **Yes** | Delete book category |
| POST | `/myledger-api/book/save-book` | **Yes** | Create book |
| GET | `/myledger-api/book/get-all-book` | **Yes** | Paginated book list (query: businessId, page, size) |
| PUT | `/myledger-api/book/update-book/{id}` | **Yes** | Update book |
| DELETE | `/myledger-api/book/delete-book/{id}` | **Yes** | Delete book |
| GET | `/myledger-api/book/search-book` | **Yes** | Search books (query: businessId, searchTerm, page, size) |
| POST | `/myledger-api/book/save-payment-mode` | **Yes** | Create payment mode |
| GET | `/myledger-api/book/get-all-payment-mode/{businessId}` | **Yes** | List payment modes |
| PUT | `/myledger-api/book/update-payment-mode/{id}` | **Yes** | Update payment mode |
| DELETE | `/myledger-api/book/delete-payment-mode/{id}` | **Yes** | Delete payment mode |

---

## Transaction – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/myledger-api/transaction/save-transaction` | **Yes** | Create transaction (multipart/form-data) |
| PUT | `/myledger-api/transaction/update-transaction/{id}` | **Yes** | Update transaction (multipart/form-data) |
| DELETE | `/myledger-api/transaction/delete-transaction/{id}` | **Yes** | Delete transaction |
| POST | `/myledger-api/transaction/bulk-delete` | **Yes** | Bulk delete transactions |
| GET | `/myledger-api/transaction/get-all-transaction/{bookId}` | **Yes** | Paginated transactions (query: page, size) |
| GET | `/myledger-api/transaction/filter-transaction/{bookId}` | **Yes** | Filter transactions (query params) |
| GET | `/myledger-api/transaction/search-transaction/{bookId}` | **Yes** | Search (query: searchTerm, page, size) |
| GET | `/myledger-api/transaction/dashboard/{bookId}` | **Yes** | Dashboard data for book |
| GET | `/myledger-api/transaction/get-transaction-summary/{bookId}` | **Yes** | Transaction summary |
| POST | `/myledger-api/transaction/export-excel` | **Yes** | Export to Excel (JSON body) |
| GET | `/myledger-api/transaction/file/download/{fileId}` | **Yes** | Download attachment |
| GET | `/myledger-api/transaction/file/view/{fileId}` | **Yes** | View attachment |

---

## Dashboard – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/myledger-api/dashboard/overview` | **Yes** | Dashboard overview (query: businessId, etc.) |
| GET | `/myledger-api/dashboard/cash-flow-trend` | **Yes** | Cash flow trend data |

---

## Analytics – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/myledger-api/analytics/category-wise` | **Yes** | Category-wise analytics (query: businessId, bookId, fromDate, toDate) |
| GET | `/myledger-api/analytics/month-wise` | **Yes** | Month-wise analytics (query: businessId, bookId, fromDate, toDate) |
| GET | `/myledger-api/analytics/business-wise` | **Yes** | Business-wise analytics (query: fromDate, toDate) |
| GET | `/myledger-api/analytics/time-series` | **Yes** | Time-series by day/month/year (query: businessId, bookId, fromDate, toDate, granularity) |

---

## Role – full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/myledger-api/role/get-all-role` | **Yes** | Paginated role list (query: page, size) |
| POST | `/myledger-api/role/save-role` | **Yes** | Create role |
| PUT | `/myledger-api/role/update-role/{id}` | **Yes** | Update role |
| DELETE | `/myledger-api/role/delete-role/{id}` | **Yes** | Delete role |

---

## Auth summary

| Auth | Endpoints |
|------|-----------|
| **No auth** | send-otp, verify-otp, refresh, forgot-password, google, google/id-token, google/callback, send-verification-email, verify-email (GET/POST), invite-by-token, accept-invite, reject-invite |
| **Optional** | logout (can send Bearer for consistency) |
| **Bearer required** | All other endpoints (me, users/* except invite endpoints, business/*, book/*, transaction/*, dashboard/*, role/*) |

**Example request with auth:**
```http
GET /myledger-api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Total:** 67 endpoints (including 4 analytics + `auth/google/id-token`)
