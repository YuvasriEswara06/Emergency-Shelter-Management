# Copilot Instructions — Emergency Shelter Management System

## Project overview
A Spring Boot + PostgreSQL application for managing emergency shelters during
disasters: shelter/bed management, victim registration, volunteer coordination,
relief inventory with central-to-shelter supply transfers, and role-based access.

- **Backend:** Java, Spring Boot (Maven), Spring Data JPA, Spring Web
- **Database:** PostgreSQL 17, database name `emergency_shelter_db`
- **Auth:** Spring Security, 2 roles only — `Admin` and `Staff` (do not introduce
  more roles unless explicitly asked)

## Database schema — treat as fixed, do not redesign
The schema is finalized (12 tables). Do not suggest new tables, renamed columns,
or restructured relationships unless explicitly asked. The 12 tables are:
`Shelters, Items, Victims, Beds, Bed_Allocations, Volunteers,
Inventory_Locations, Inventory_Stock, Supply_Requests, Inventory_Transactions,
Users, Activity_Log`.

Key design decisions already made — do not "fix" or second-guess these:
- `Victims` has NO `shelter_id` or `status` column. A victim's current shelter
  and sheltered/discharged state are always derived by joining
  `Bed_Allocations → Beds → Shelters`, never stored directly.
- `Supply_Requests.item_id` and `Inventory_Transactions.item_id` reference the
  `Items` catalog table, NOT a specific `Inventory_Stock` row.
- `Inventory_Locations.shelter_id` is `UNIQUE` (nullable) — one inventory
  location per shelter max; NULL means it's the Central Warehouse.
- Enum-like fields (`status`, `role`, `priority`, etc.) are `VARCHAR` +
  `CHECK (... IN (...))` in the DB, not native Postgres ENUM types. Mirror this
  choice in Java — use enums in Java entities is fine, but map them with
  `@Enumerated(EnumType.STRING)`, not ordinal.

## Business rules to enforce in code (not just comments)
- **Only one `Active` `Bed_Allocations` row per `bed_id` at a time.** Always
  check for an existing active allocation before creating a new one, inside a
  `@Transactional` method.
- **`allocation_status` and `vacated_date` must be updated together**, never
  independently, in the same transaction.
- **Supply request fulfillment is a multi-step `@Transactional` operation**:
  verify request is Approved → check central stock ≥ requested quantity →
  decrement central stock → find-or-create shelter stock row → increment it →
  insert an `Inventory_Transactions` row → mark request `Fulfilled` with
  `processed_date` → insert an `Activity_Log` row. If any step fails, the whole
  operation must roll back — don't split this across multiple non-transactional
  calls.
- **Every meaningful create/update/delete action should write an
  `Activity_Log` row** (user_id, action, affected_table, affected_id).
- Central Warehouse (`Inventory_Locations.location_type = 'Central'`) must
  never be duplicated and must always have `shelter_id = NULL`. Enforce this in
  service-layer validation, not just the DB CHECK.

## Coding conventions
- Package structure: `entity`, `repository`, `service`, `controller` (or
  `dto` if DTOs are introduced later).
- Entity class names are singular and match table names in PascalCase
  (`Shelter`, `BedAllocation`, `InventoryStock`, `SupplyRequest`, etc.) — table
  names in the DB are `PascalCase_With_Underscores`, don't assume `@Table` name
  matches the Java class name without an explicit `@Table(name = "...")`.
- Use constructor injection for services/repositories, not field injection with
  `@Autowired` on fields.
- Prefer `Optional<T>` return types from repository lookups where "not found"
  is a valid outcome (e.g. finding an active allocation for a bed).
- Passwords must be hashed with BCrypt (`PasswordEncoder`) — never store or log
  plain-text passwords, and never suggest printing `password_hash` values in
  logs or debug output.

## What NOT to do
- Don't auto-generate DDL via `spring.jpa.hibernate.ddl-auto=update` or
  `create` — the schema is hand-written and already exists in Postgres.
  `ddl-auto` should stay `none` (or `validate` once entities are stable).
- Don't add a `transaction_type` column to `Inventory_Transactions` — this was
  deliberately omitted at this project's scope.
- Don't add more than 2 roles to `Users.role` without being asked.
- Don't suggest MySQL-specific syntax (`AUTO_INCREMENT`, `ENGINE=InnoDB`,
  backtick identifiers) — this project is PostgreSQL only.
