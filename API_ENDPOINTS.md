# My Ledger â€“ All API Endpoints (End-to-End)

**Full base URL:** `http://localhost:8080/cashlink-api` or `https://<your-host>/cashlink-api`

**Android:** Use your PCâ€™s LAN IP or emulator `10.0.2.2`, not `localhost`. See **`docs/ANDROID_API_CONNECTIVITY.md`**.

**Health (no auth):** `GET /cashlink-api/public/ping` â€” returns `{"status":"ok",...}` to verify the API is reachable.

**Auth (protected endpoints):** Send header:
```http
Authorization: Bearer <accessToken>
```
Optional for some APIs: `X-Logged-User-Id: <user-uuid>` (must match token).

---
#testing
## Auth â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/cashlink-api/auth/send-otp` | No | Send OTP to email |
| POST | `/cashlink-api/auth/verify-otp` | No | Verify OTP and login/register |
| POST | `/cashlink-api/auth/logout` | Optional | Logout (then discard tokens on client) |
| POST | `/cashlink-api/auth/refresh` | No | Refresh access token |
| GET | `/cashlink-api/auth/me` | **Yes** | Get current user info |
| GET | `/cashlink-api/auth/google` | No | Redirect to Google Sign-In |
| POST | `/cashlink-api/auth/google/id-token` | No | Android/Flutter: verify Google ID token, return JWTs |
| GET | `/cashlink-api/auth/google/callback` | No | Google OAuth callback |
| POST | `/cashlink-api/auth/forgot-password` | No | Send password reset link |
| POST | `/cashlink-api/auth/send-verification-email` | No | Send email verification link |
| POST | `/cashlink-api/auth/verify-email` | No | Verify email (body: token) |
| GET | `/cashlink-api/auth/verify-email` | No | Verify email (query: token) |

---

## Users â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/cashlink-api/users/save-user` | **Yes** | Create user |
| GET | `/cashlink-api/users/get-all-user` | **Yes** | Paginated user list |
| PUT | `/cashlink-api/users/update-user/{id}` | **Yes** | Update user |
| DELETE | `/cashlink-api/users/delete-user/{id}` | **Yes** | Delete user |
| GET | `/cashlink-api/users/search-user` | **Yes** | Search users (query: searchTerm, page, size) |
| POST | `/cashlink-api/users/send-invite` | **Yes** | Send invite emails |
| GET | `/cashlink-api/users/invite-by-token` | No | Get invite details (query: token) |
| POST | `/cashlink-api/users/accept-invite` | No | Accept invitation |
| POST | `/cashlink-api/users/reject-invite` | No | Reject invitation |
| GET | `/cashlink-api/users/accepted-invite-notifications` | **Yes** | Accepted/rejected invites (query: page, size, businessId) |

---

## Business â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/cashlink-api/business/save-business-type` | **Yes** | Create business type |
| GET | `/cashlink-api/business/get-all-business-type` | **Yes** | List business types |
| PUT | `/cashlink-api/business/update-business-type/{id}` | **Yes** | Update business type |
| DELETE | `/cashlink-api/business/delete-business-type/{id}` | **Yes** | Delete business type |
| POST | `/cashlink-api/business/save-business` | **Yes** | Create business |
| GET | `/cashlink-api/business/get-all-business` | **Yes** | Paginated business list |
| PUT | `/cashlink-api/business/update-business/{id}` | **Yes** | Update business |
| DELETE | `/cashlink-api/business/delete-business/{id}` | **Yes** | Delete business |
| GET | `/cashlink-api/business/search-business` | **Yes** | Search businesses (query: searchTerm, page, size) |

---

## Book â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/cashlink-api/book/save-book-category` | **Yes** | Create book category |
| POST | `/cashlink-api/book/save-book-category/{businessId}` | **Yes** | Create book category for business |
| GET | `/cashlink-api/book/get-all-book-category/{businessId}` | **Yes** | List book categories |
| PUT | `/cashlink-api/book/update-book-category/{id}` | **Yes** | Update book category |
| DELETE | `/cashlink-api/book/delete-book-category/{id}` | **Yes** | Delete book category |
| POST | `/cashlink-api/book/save-book` | **Yes** | Create book |
| GET | `/cashlink-api/book/get-all-book` | **Yes** | Paginated book list (query: businessId, page, size) |
| PUT | `/cashlink-api/book/update-book/{id}` | **Yes** | Update book |
| DELETE | `/cashlink-api/book/delete-book/{id}` | **Yes** | Delete book |
| GET | `/cashlink-api/book/search-book` | **Yes** | Search books (query: businessId, searchTerm, page, size) |
| POST | `/cashlink-api/book/save-payment-mode` | **Yes** | Create payment mode |
| GET | `/cashlink-api/book/get-all-payment-mode/{businessId}` | **Yes** | List payment modes |
| PUT | `/cashlink-api/book/update-payment-mode/{id}` | **Yes** | Update payment mode |
| DELETE | `/cashlink-api/book/delete-payment-mode/{id}` | **Yes** | Delete payment mode |

---

## Transaction â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| POST | `/cashlink-api/transaction/save-transaction` | **Yes** | Create transaction (multipart/form-data) |
| PUT | `/cashlink-api/transaction/update-transaction/{id}` | **Yes** | Update transaction (multipart/form-data) |
| DELETE | `/cashlink-api/transaction/delete-transaction/{id}` | **Yes** | Delete transaction |
| POST | `/cashlink-api/transaction/bulk-delete` | **Yes** | Bulk delete transactions |
| GET | `/cashlink-api/transaction/get-all-transaction/{bookId}` | **Yes** | Paginated transactions (query: page, size) |
| GET | `/cashlink-api/transaction/filter-transaction/{bookId}` | **Yes** | Filter transactions (query params) |
| GET | `/cashlink-api/transaction/search-transaction/{bookId}` | **Yes** | Search (query: searchTerm, page, size) |
| GET | `/cashlink-api/transaction/dashboard/{bookId}` | **Yes** | Dashboard data for book |
| GET | `/cashlink-api/transaction/get-transaction-summary/{bookId}` | **Yes** | Transaction summary |
| POST | `/cashlink-api/transaction/export-excel` | **Yes** | Export to Excel (JSON body) |
| GET | `/cashlink-api/transaction/file/download/{fileId}` | **Yes** | Download attachment |
| GET | `/cashlink-api/transaction/file/view/{fileId}` | **Yes** | View attachment |

---

## Dashboard â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/cashlink-api/dashboard/overview` | **Yes** | Dashboard overview (query: businessId, etc.) |
| GET | `/cashlink-api/dashboard/cash-flow-trend` | **Yes** | Cash flow trend data |

---

## Analytics â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/cashlink-api/analytics/category-wise` | **Yes** | Category-wise analytics (query: businessId, bookId, fromDate, toDate) |
| GET | `/cashlink-api/analytics/month-wise` | **Yes** | Month-wise analytics (query: businessId, bookId, fromDate, toDate) |
| GET | `/cashlink-api/analytics/business-wise` | **Yes** | Business-wise analytics (query: fromDate, toDate) |
| GET | `/cashlink-api/analytics/time-series` | **Yes** | Time-series by day/month/year (query: businessId, bookId, fromDate, toDate, granularity) |

---

## Role â€“ full paths

| Method | Full path | Auth | Description |
|--------|-----------|------|-------------|
| GET | `/cashlink-api/role/get-all-role` | **Yes** | Paginated role list (query: page, size) |
| POST | `/cashlink-api/role/save-role` | **Yes** | Create role |
| PUT | `/cashlink-api/role/update-role/{id}` | **Yes** | Update role |
| DELETE | `/cashlink-api/role/delete-role/{id}` | **Yes** | Delete role |

---

## Auth summary

| Auth | Endpoints |
|------|-----------|
| **No auth** | send-otp, verify-otp, refresh, forgot-password, google, google/id-token, google/callback, send-verification-email, verify-email (GET/POST), invite-by-token, accept-invite, reject-invite |
| **Optional** | logout (can send Bearer for consistency) |
| **Bearer required** | All other endpoints (me, users/* except invite endpoints, business/*, book/*, transaction/*, dashboard/*, role/*) |

**Example request with auth:**
```http
GET /cashlink-api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Total:** 67 endpoints (including 4 analytics + `auth/google/id-token`)
