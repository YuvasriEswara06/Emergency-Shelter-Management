# Security & RBAC Requirements
## Emergency Shelter Management System

**Status:** Final  
**Roles:** Exactly 2 — `Admin`, `Staff`  
**Authentication:** Spring Security + JWT  
**Database:** PostgreSQL  
**Schema:** Finalized and must not be modified

---

## 1. Purpose

Implement authentication and authorization for the existing Emergency Shelter Management System using Spring Security and JWT while strictly preserving the finalized database schema and existing business logic.

This document is the source of truth for the security implementation.

---

## 2. Fixed Role Model

The system has **exactly 2 roles**:

- `Admin`
- `Staff`

Do **NOT** introduce:

- `Volunteer`
- `Shelter Staff`
- Any third role
- Any additional role

### Spring Security Mapping

Map the database roles as:

- `Admin` → `ROLE_ADMIN`
- `Staff` → `ROLE_STAFF`

The existing `Users.role` column and its database constraint must remain unchanged.

Do not convert the role into an enum if that requires changing the database contract.

---

## 3. Authentication Requirements

Use:

- Spring Security
- JWT-based authentication
- Stateless authentication
- BCrypt password hashing
- `AuthenticationManager`
- `UserDetailsService`
- Existing `Users` entity
- Existing `UsersRepository`

### Login

Create:

`POST /api/auth/login`

Login should:

1. Receive username/email and password.
2. Load the user using the existing user repository.
3. Validate the password using BCrypt.
4. Authenticate through Spring Security.
5. Generate a JWT after successful authentication.
6. Return the JWT to the client.

The login endpoint must be publicly accessible.

All other application endpoints must require authentication.

---

## 4. JWT Requirements

JWT must:

- Be signed using a secret stored in configuration/environment.
- Never hardcode the secret in Java source code.
- Have an expiration time.
- Contain only necessary claims.
- Never contain the raw password or `password_hash`.
- Be used for stateless authentication.

Expected request format:

`Authorization: Bearer <JWT>`

A JWT authentication filter should:

1. Read the Authorization header.
2. Extract the Bearer token.
3. Validate the JWT.
4. Extract the username/user identity and role.
5. Set the authenticated user in Spring Security's `SecurityContext`.

---

## 5. HTTP Security Rules

### Public

Only authentication endpoints such as:

`POST /api/auth/login`

should be publicly accessible.

### Protected

All other `/api/**` endpoints require authentication.

### Authentication Errors

- Missing/invalid/expired JWT → `401 Unauthorized`
- Authenticated user without required permission → `403 Forbidden`

Do not expose sensitive authentication details in error responses.

---

# 6. ADMIN Permissions

`Admin` has full system access.

Admin can:

### Shelter Management

- Create shelters
- View shelters
- Update shelters
- Manage shelter status

### Victim Management

- Create victims
- View victims
- Update victims
- Manage victim records

### Bed Management

- Create/manage beds
- View bed availability
- Update bed status

### Bed Allocation

- Allocate beds
- Vacate beds
- Manage allocations

### Volunteer Management

- Create volunteers
- View volunteers
- Update volunteer information
- Manage volunteer records

### Inventory

- Manage inventory locations
- Manage inventory stock
- Manage inventory transactions
- View inventory information

### Supply Requests

- Create supply requests
- View supply requests
- Approve requests
- Reject requests
- Fulfill requests

### User Management

- Create users
- View users
- Update users
- Assign `Admin` or `Staff` roles

Do not expose raw `password_hash`.

### Activity Logs

- View activity/audit logs

Admin has system-wide access.

---

# 7. STAFF Permissions

`Staff` is an operational user.

Staff can access and manage operational resources required for shelter operations, according to the existing application design.

### Allowed

#### Shelters

- View shelter information required for operations.

#### Victims

- Create victims
- View victims
- Update victim information

#### Beds

- View beds
- View availability
- Manage bed status where permitted by existing business logic

#### Bed Allocations

- Allocate beds
- Vacate beds

#### Volunteers

- View volunteer information
- Manage operational volunteer information where supported by existing business logic

#### Inventory

- View inventory
- Manage shelter inventory stock
- Perform permitted inventory operations

#### Supply Requests

- Create supply requests
- View supply requests
- Approve requests
- Reject requests
- Fulfill requests where permitted by existing business logic

### Not Allowed

Staff cannot:

- Create/update/delete users
- Assign roles
- Manage Admin accounts
- Access raw `password_hash`
- Access unrestricted system administration functionality
- Modify security configuration
- Access audit/activity logs unless explicitly permitted by an existing project requirement

---

# 8. Shelter-Level Authorization

The project currently has exactly 2 roles: `Admin` and `Staff`.

Do NOT create a third `Shelter Staff` role.

If the existing finalized schema/code provides a valid way to determine which shelter a Staff user belongs to, enforce shelter-level authorization using that existing relationship.

If the current schema/code does NOT provide enough information to determine Staff-to-Shelter ownership:

**STOP and report the exact limitation.**

Do NOT:

- Add a new database column
- Add a new table
- Change a foreign key
- Change the finalized schema
- Invent a relationship
- Create a hidden workaround

The database schema is frozen.

---

# 9. Authorization Implementation

Use least-privilege authorization.

Use a combination of:

- URL-level authorization in `SecurityFilterChain`
- `@PreAuthorize` where appropriate
- Existing service-layer business rules

Examples:

```java
@PreAuthorize("hasRole('ADMIN')")
```

For operations available to both roles:

```java
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
```

Do not rely only on frontend restrictions.

Authorization must be enforced on the backend.

---

# 10. Password Security

Passwords must:

- Be hashed using BCrypt.
- Never be stored as plaintext.
- Never be returned through API responses.
- Never appear in logs.
- Never appear in JWT claims.

The existing `password_hash` database field must remain protected.

Use DTOs or response projections where necessary to prevent accidental exposure.

---

# 11. Existing Architecture Must Be Preserved

Maintain:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Security should integrate with the existing architecture.

Do NOT unnecessarily rewrite:

- Existing entities
- Existing repositories
- Existing services
- Existing controllers
- Existing business logic

Reuse existing code wherever possible.

---

# 12. Database Schema Restrictions

The database schema is FINAL.

Do NOT:

- Add tables
- Remove tables
- Rename tables
- Add columns
- Remove columns
- Rename columns
- Change PKs
- Change FKs
- Change constraints
- Add roles
- Modify `Users.role`

The existing database remains the source of truth.

Hibernate should continue using:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

# 13. Configuration Requirements

Security configuration values must not be hardcoded.

Use application configuration/environment variables for:

- JWT secret
- JWT expiration
- Other security-sensitive configuration

Do not commit real secrets to GitHub.

---

# 14. Error Handling

Implement appropriate responses for:

### 401 Unauthorized

When:

- No JWT is supplied
- JWT is invalid
- JWT is expired
- Authentication fails
- User credentials are invalid

### 403 Forbidden

When:

- User is authenticated
- But their role does not have permission for the requested operation

Do not expose stack traces or sensitive internal information to API clients.

---

# 15. Required Security Testing

Test at minimum:

## Authentication

- Valid Admin login → success + JWT
- Valid Staff login → success + JWT
- Invalid password → `401`
- Unknown user → `401`
- Missing JWT → `401`
- Invalid JWT → `401`
- Expired JWT → `401`

## Authorization

### Admin

Verify Admin can access all intended administrative and operational endpoints.

### Staff

Verify Staff can access permitted operational endpoints.

Verify Staff cannot:

- Manage users
- Assign roles
- Perform Admin-only operations
- Access protected audit/activity functionality

### Wrong Role

Verify an authenticated user attempting a restricted operation receives:

`403 Forbidden`

---

# 16. Implementation Rules for Copilot

Before modifying code:

1. Inspect the existing `Users` entity.
2. Inspect `UsersRepository`.
3. Inspect existing services and controllers.
4. Inspect current security-related dependencies/configuration.
5. Inspect `docs/database-schema.sql`.
6. Confirm the implementation is compatible with the existing 2-role model.

During implementation:

- Follow this document.
- Follow `.github/copilot-instructions.md`.
- Treat `docs/database-schema.sql` as the database source of truth.
- Preserve existing business logic.
- Do not introduce a third role.
- Do not modify the database schema.
- Do not silently work around missing schema relationships.

If a requirement cannot be implemented because of the finalized schema:

**STOP and report the exact discrepancy instead of changing the schema.**

---

# 17. Expected Authentication Flow

```text
Client
  ↓
POST /api/auth/login
  ↓
AuthenticationManager
  ↓
UserDetailsService
  ↓
UsersRepository
  ↓
PostgreSQL
  ↓
BCrypt password validation
  ↓
JWT generation
  ↓
Client receives JWT
```

For protected requests:

```text
Client
  ↓
Authorization: Bearer JWT
  ↓
JWT Filter
  ↓
JWT Validation
  ↓
SecurityContext
  ↓
Role Authorization
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
PostgreSQL
```

---

# 18. Final Security Scope

The final security implementation must provide:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Stateless authentication
- Exactly 2 roles: `Admin` and `Staff`
- `ROLE_ADMIN` and `ROLE_STAFF` Spring Security authorities
- Protected application endpoints
- Role-based authorization
- `401 Unauthorized` handling
- `403 Forbidden` handling
- Password protection
- Secure JWT configuration
- Backend-enforced authorization
- Security testing
- No database schema modifications
- No additional roles
