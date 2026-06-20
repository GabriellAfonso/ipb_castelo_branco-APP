# API Contract: Monthly Birthdays

## GET /ipbcb/members/birthdays/

Fetches birthdays for a given month.

### Request

```
GET /ipbcb/members/birthdays/?month={1-12}
Authorization: Bearer {jwt_token}
```

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| month     | Int  | Yes      | Month number (1 = January, 12 = December) |

### Response: 200 OK (with birthdays)

```json
{
  "birthdays": [
    { "name": "Alice", "birth_day": 5 },
    { "name": "Bob", "birth_day": 12 },
    { "name": "Carlos", "birth_day": 25 }
  ]
}
```

### Response: 200 OK (empty month)

```json
{
  "birthdays": []
}
```

### Response: 401 Unauthorized

User not authenticated or token expired. Handled by `TokenAuthenticator` (auto-refresh) and `BaseSnapshotRepository` (emits `SnapshotState.Error`).

### Notes

- Results are ordered by `birth_day` ascending
- No pagination (monthly birthday count is small)
- No ETag support
