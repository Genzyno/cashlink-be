# Business Type API – Endpoints and Payloads

Base path: `{apiUrl}/business` (e.g. `http://localhost:8080/myledger-api/business`).

The frontend uses these endpoints for the **Business Type** dropdown in the Business module (Add/Edit Business drawer): **Get all**, **Save**, **Update**, and **Delete**. Use the same request headers as other APIs (`Authorization: Bearer <token>`, `X-Logged-User-Id`).

---

## 1. Get All Business Types

**Endpoint:** `GET /business/get-all-business-type`

**Headers:**  
`Authorization: Bearer <access_token>`  
`X-Logged-User-Id: <user-uuid>`

**Query parameters:** None.

**Success response:**  
Return a JSON body that the frontend can use as a list. The frontend currently uses `res.data` as the array of business types (if your wrapper uses `data` for the list). If you use a paginated wrapper with `data.content`, the frontend would need to use `res.data.content`; currently it assigns `res.data` to the list.

**Recommended response (array in `data`):**

```json
{
  "data": [
    {
      "id": "uuid-string",
      "businessType": "Retail"
    },
    {
      "id": "uuid-string",
      "businessType": "Services"
    }
  ],
  "message": "Success",
  "statusCode": 200
}
```

| Field (each item) | Type   | Description        |
|-------------------|--------|--------------------|
| id                | string | UUID of business type |
| businessType      | string | Display name       |

When there are no business types, the backend returns `200` with `data: []`.

---

## 2. Save Business Type (Add New)

**Endpoint:** `POST /business/save-business-type`

**Headers:**  
`Authorization: Bearer <access_token>`  
`X-Logged-User-Id: <user-uuid>`  
`Content-Type: application/json`

**Request body:**

```json
{
  "businessType": "Retail"
}
```

| Field         | Type   | Required | Description        |
|---------------|--------|----------|--------------------|
| businessType  | string | Yes      | Name of the type   |

**Success response:**  
Return the created business type and a success status. The frontend uses `res.data.id` to auto-select the new type in the dropdown when the user clicks **Save & Select**.

```json
{
  "data": {
    "id": "uuid-string",
    "businessType": "Retail"
  },
  "message": "Business type created",
  "statusCode": 201
}
```

| Field (data)   | Type   | Description        |
|----------------|--------|--------------------|
| id             | string | UUID of new type   |
| businessType   | string | Name saved         |

**Conflict (409):** If a business type with the same name already exists.

---

## 3. Update Business Type (Edit)

**Endpoint:** `PUT /business/update-business-type/{id}`

**Headers:**  
`Authorization: Bearer <access_token>`  
`X-Logged-User-Id: <user-uuid>`  
`Content-Type: application/json`

**Path parameter:**

| Parameter | Type   | Description           |
|-----------|--------|-----------------------|
| id        | string | UUID of business type |

**Request body:**

```json
{
  "businessType": "Retail & E-commerce"
}
```

| Field         | Type   | Required | Description      |
|---------------|--------|----------|------------------|
| businessType  | string | Yes      | Updated name     |

**Success response:**

```json
{
  "data": {
    "id": "uuid-string",
    "businessType": "Retail & E-commerce"
  },
  "message": "Business type updated",
  "statusCode": 200
}
```

**404:** Business type not found. **409:** Another business type already has that name.

---

## 4. Delete Business Type

**Endpoint:** `DELETE /business/delete-business-type/{id}`

**Headers:**  
`Authorization: Bearer <access_token>`  
`X-Logged-User-Id: <user-uuid>`

**Path parameter:**

| Parameter | Type   | Description           |
|-----------|--------|-----------------------|
| id        | string | UUID of business type |

**Request body:** None.

**Success response:**

```json
{
  "data": null,
  "message": "Business type deleted",
  "statusCode": 200
}
```

**404:** Business type not found. **409:** Cannot delete because the business type is in use by one or more businesses.

---

## Summary

| Action   | Method | Endpoint                              | Payload / Notes                    |
|----------|--------|----------------------------------------|------------------------------------|
| Get all  | GET    | `/business/get-all-business-type`     | None; response `data` = array      |
| Save     | POST   | `/business/save-business-type`        | `{ "businessType": "string" }`     |
| Update   | PUT    | `/business/update-business-type/{id}`  | `{ "businessType": "string" }`    |
| Delete   | DELETE | `/business/delete-business-type/{id}`  | None                               |

---

## Frontend usage

- **Get all:** Called on load and after Save/Update/Delete to refresh the dropdown.
- **Save:** Called when user clicks **Save & Select** or **Save & Add More** in the "Add New Business Type" modal. Frontend may set the selected business type from `res.data.id` when **Save & Select** is used.
- **Update:** Called when user edits an existing type and clicks **Update** in the modal.
- **Delete:** Called after user confirms deletion in the confirmation dialog.

If your backend returns a different response shape (e.g. list inside `data.content`), either adapt the backend to expose `data` as the array or the frontend can be changed to use `res.data.content` for the list and `res.data` (or `res.data.content[0]`) for the created/updated item.

---

## Optional: Currency and Financial Year (for backend integration)

Currently **Currency** and **Financial Year** in the Business form are managed in-memory on the frontend (add/edit/delete in the dropdown). If you want to persist them in the backend, the frontend can be wired to the following contract.

### Currency

| Action  | Method | Endpoint (example)           | Payload / Notes |
|---------|--------|------------------------------|------------------|
| Get all | GET    | `/business/get-all-currency` | None; `data` = array of `{ "code": "INR", "name": "Indian Rupee" }` |
| Save    | POST   | `/business/save-currency`    | `{ "code": "INR", "name": "Indian Rupee" }` |
| Update  | PUT    | `/business/update-currency/{code}` | `{ "code": "INR", "name": "Indian Rupee" }` |
| Delete  | DELETE | `/business/delete-currency/{code}` | None |

### Financial Year

| Action  | Method | Endpoint (example)                | Payload / Notes |
|---------|--------|-----------------------------------|------------------|
| Get all | GET    | `/business/get-all-financial-year` | None; `data` = array of strings e.g. `["2024-2025","2025-2026"]` |
| Save    | POST   | `/business/save-financial-year`   | `{ "value": "2025-2026" }` |
| Update  | PUT    | `/business/update-financial-year` | `{ "oldValue": "2024-2025", "newValue": "2024-2026" }` or similar |
| Delete  | DELETE | `/business/delete-financial-year/{value}` | None |

Once these endpoints exist, the frontend can call them instead of using in-memory lists.
