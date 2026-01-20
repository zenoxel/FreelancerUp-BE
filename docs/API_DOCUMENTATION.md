# FreelancerUp API Documentation

## Overview

FreelancerUp provides a comprehensive REST API for managing freelance projects, bids, payments, and more. The API is documented using OpenAPI 3.0 specification and accessible via Swagger UI.

## Accessing the API Documentation

### Swagger UI (Interactive Documentation)

**Development Environment:**
```
http://localhost:8081/swagger-ui/index.html
```

**Production Environment:**
```
https://api.freelancerup.com/swagger-ui/index.html
```

### OpenAPI JSON Specification

```
http://localhost:8081/v3/api-docs
```

### OpenAPI YAML Specification

```
http://localhost:8081/v3/api-docs.yaml
```

## API Groups

The API is organized into the following groups:

| Group | Base Path | Description |
|-------|-----------|-------------|
| **Client** | `/api/v1/clients` | Client profile management |
| **Project** | `/api/v1/projects` | Project CRUD operations |
| **Bid** | `/api/v1/**/bids` | Bidding system |
| **Auth** | `/api/v1/auth` | Authentication endpoints |
| **User** | `/api/v1/users` | User management |

## Authentication

Most endpoints require JWT Bearer authentication. To authenticate:

1. Obtain a JWT token by logging in:
   ```http
   POST /api/v1/auth/login
   Content-Type: application/json

   {
     "email": "user@example.com",
     "password": "your-password"
   }
   ```

2. Include the token in subsequent requests:
   ```http
   Authorization: Bearer <your-jwt-token>
   ```

### In Swagger UI

1. Click the **Authorize** button (lock icon) in the top-right corner
2. Enter your JWT token (e.g., `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`)
3. Click **Authorize**
4. All subsequent requests will include the token

## Common Response Format

All API responses follow this standard structure:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Operation completed successfully",
  "data": { ... },
  "error": null,
  "timestamp": "2024-01-20T10:30:00"
}
```

### Error Response

```json
{
  "success": false,
  "statusCode": 400,
  "message": "Validation failed",
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed for one or more fields",
    "fieldErrors": [
      {
        "field": "email",
        "message": "Email is required",
        "rejectedValue": null
      }
    ]
  },
  "timestamp": "2024-01-20T10:30:00"
}
```

## Status Codes

| Code | Description |
|------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid input or validation error |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error - Server error |

## Pagination

List endpoints support pagination via query parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Page number (0-based) |
| `size` | int | 10 | Page size (max 100) |
| `sort` | string | createdAt,desc | Sort criteria (field,direction) |

**Example:**
```http
GET /api/v1/projects/search?page=0&size=20&sort=createdAt,desc
```

**Response:**
```json
{
  "content": [ ... ],
  "pageable": { ... },
  "totalPages": 5,
  "totalElements": 100,
  "size": 20,
  "number": 0,
  "sort": { ... }
}
```

## Rate Limiting

- **Authenticated requests**: 100 requests per minute
- **Public endpoints**: 30 requests per minute

Rate limit headers are included in responses:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642694400
```

## Key Endpoints

### Client API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/clients` | Register client profile | USER |
| GET | `/api/v1/clients/{userId}` | Get client profile | CLIENT |
| PUT | `/api/v1/clients/{userId}` | Update client profile | CLIENT |
| GET | `/api/v1/clients/{userId}/stats` | Get client statistics | CLIENT |
| DELETE | `/api/v1/clients/{userId}` | Delete client profile | CLIENT |

### Project API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/projects` | Create project | CLIENT |
| GET | `/api/v1/projects/search` | Search projects | None |
| GET | `/api/v1/projects/{projectId}` | Get project details | None |
| PUT | `/api/v1/projects/{projectId}` | Update project | CLIENT (owner) |
| PATCH | `/api/v1/projects/{projectId}/status` | Update project status | CLIENT (owner) |
| DELETE | `/api/v1/projects/{projectId}` | Delete project | CLIENT (owner) |

### Bid API

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/v1/projects/{projectId}/bids` | Submit bid | FREELANCER |
| GET | `/api/v1/projects/{projectId}/bids` | List project bids | CLIENT (owner) |
| PATCH | `/api/v1/bids/{bidId}/accept` | Accept bid | CLIENT (owner) |
| PATCH | `/api/v1/bids/{bidId}/reject` | Reject bid | CLIENT (owner) |
| DELETE | `/api/v1/bids/{bidId}` | Withdraw bid | FREELANCER (owner) |

## Example Requests

### Create Project

```bash
curl -X POST http://localhost:8081/api/v1/projects \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Build Mobile App",
    "description": "Need a React Native mobile app for iOS and Android",
    "requirements": "3+ years experience with React Native",
    "skills": ["React Native", "iOS", "Android"],
    "budget": {
      "minAmount": 5000,
      "maxAmount": 10000,
      "currency": "USD",
      "isNegotiable": true
    },
    "duration": 60,
    "type": "FIXED_PRICE",
    "deadline": "2024-03-20T10:00:00"
  }'
```

### Submit Bid

```bash
curl -X POST http://localhost:8081/api/v1/projects/project123/bids \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "proposal": "I have 5 years of experience in React Native development...",
    "price": 7500,
    "estimatedDuration": 45
  }'
```

### Search Projects

```bash
curl -X GET "http://localhost:8081/api/v1/projects/search?keyword=mobile&page=0&size=10&sort=createdAt,desc"
```

## SDK & Libraries

Official SDKs are available for:
- JavaScript/TypeScript
- Python
- Java
- Go

See the [SDK Documentation](https://docs.freelancerup.com/sdks) for more details.

## Support

For API issues or questions:
- Documentation: https://docs.freelancerup.com
- Email: api-support@freelancerup.com
- GitHub Issues: https://github.com/freelancerup/api/issues

---

**Note:** This documentation is auto-generated from the OpenAPI specification. Always refer to the live Swagger UI for the most up-to-date API information.
