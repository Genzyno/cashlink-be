# Transaction Export Excel â€“ Endpoint and Payload

Used when the user clicks **Export Excel** on the Transactions page, opens the export drawer (with the same **multiselect** filter fields as the main filter), and clicks **Export**.

**Base URL:** `{apiBaseUrl}` (e.g. `http://localhost:8080/cashlink-api`)  
**Auth:** `Authorization: Bearer <accessToken>`

---

## Endpoint

| Item | Value |
|------|--------|
| **Method** | POST |
| **URL** | `{apiUrl}/transaction/export-excel` |
| **Content-Type** | `application/json` |

---

## Payload (request body)

All filter fields are **optional**. Only `bookId` is required. All multi-value fields are **arrays**; empty or omitted = no restriction (all).

```json
{
  "bookId": "<book-uuid>",
  "fromDate": "2025-02-01",
  "toDate": "2025-02-28",
  "transactionTypes": ["CASH_IN", "CASH_OUT"],
  "categoryIds": ["<category-uuid-1>", "<category-uuid-2>"],
  "paymentModeIds": ["<payment-mode-uuid>"],
  "minAmount": 0,
  "maxAmount": 100000,
  "createdByIds": ["<user-uuid-1>"],
  "updatedByIds": ["<user-uuid-2>"]
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `bookId` | string (UUID) | Yes | UUID of the book whose transactions to export. |
| `fromDate` | string | No | Start date (inclusive), `yyyy-MM-dd`. |
| `toDate` | string | No | End date (inclusive), `yyyy-MM-dd`. |
| `transactionTypes` | string[] | No | `CASH_IN`, `CASH_OUT`. Empty/omit = both. |
| `categoryIds` | string[] (UUID) | No | Filter by category UUIDs. Empty/omit = all. |
| `paymentModeIds` | string[] (UUID) | No | Filter by payment mode UUIDs. Empty/omit = all. |
| `minAmount` | number | No | Minimum transaction amount. |
| `maxAmount` | number | No | Maximum transaction amount. |
| `createdByIds` | string[] (UUID) | No | Filter by creator user ids. Empty/omit = all. |
| `updatedByIds` | string[] (UUID) | No | Filter by last updater user ids. Empty/omit = all. |

---

## Response

**Success (200):**

The backend returns the Excel file directly with:

- **Content-Type:** `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- **Content-Disposition:** `attachment; filename="transactions_export.xlsx"`

The response body is the binary Excel file. The frontend treats the response as a blob, creates a download link, triggers download with the filename from `Content-Disposition` (or default `transactions_export.xlsx`), and shows a success message (e.g. â€œExport readyâ€).

**Error (4xx / 5xx):**

JSON body with `statusCode`, `message` (and optionally `data: null`). Example:

```json
{
  "statusCode": 400,
  "message": "bookId is required.",
  "data": null
}
```

The UI shows that message or a generic â€œExport failedâ€.

---

## Summary

| Action | Method | Endpoint | Payload |
|--------|--------|----------|---------|
| Export transactions to Excel | POST | `transaction/export-excel` | `bookId` required; `fromDate`, `toDate`, `transactionTypes[]`, `categoryIds[]`, `paymentModeIds[]`, `minAmount`, `maxAmount`, `createdByIds[]`, `updatedByIds[]` optional (arrays = multiselect). |

Export is limited to 10,000 rows per request. Columns in the Excel file: Date, Time, Type, Amount, Category, Payment Mode, Remarks, Created By, Updated By.
