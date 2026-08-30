# Emergency Shelter Management System — Final Database Schema (PostgreSQL)

**12 tables. Implemented in PostgreSQL 17, database name: `emergency_shelter_db`.**

> **Note on syntax:** this document now reflects the actual PostgreSQL implementation, not generic SQL pseudocode. Key differences from a MySQL-style draft: `SERIAL` instead of `AUTO_INCREMENT`, `TIMESTAMP` instead of `DATETIME`, `REFERENCES` instead of `FK ->`, and `VARCHAR` + `CHECK (... IN (...))` instead of native `ENUM` types (Postgres enums exist but are more awkward to modify later — a `CHECK` gives identical validation with far less friction).

---

## 1. `Shelters`
```sql
CREATE TABLE Shelters (
    shelter_id      SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    location        VARCHAR(150) NOT NULL,
    capacity        INT NOT NULL CHECK (capacity > 0),
    status          VARCHAR(10) NOT NULL DEFAULT 'Active' CHECK (status IN ('Active','Inactive'))
);
```

## 2. `Items` *(catalog)*
```sql
CREATE TABLE Items (
    item_id     SERIAL PRIMARY KEY,
    item_name   VARCHAR(100) NOT NULL UNIQUE,
    unit        VARCHAR(20) NOT NULL
);
```
> One row per distinct item type (Water, Rice, Blankets...), independent of where it's stocked.

## 3. `Victims`
```sql
CREATE TABLE Victims (
    victim_id           SERIAL PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    age                 INT CHECK (age >= 0),
    gender              VARCHAR(10) CHECK (gender IN ('Male','Female','Other')),
    phone               VARCHAR(15),
    address             VARCHAR(200),
    registration_date   DATE NOT NULL DEFAULT CURRENT_DATE
);
```
> No `shelter_id` or `status` column. Current shelter and sheltered/discharged state are both derived via `Bed_Allocations → Beds → Shelters`, avoiding redundant/conflicting data.

## 4. `Beds`
```sql
CREATE TABLE Beds (
    bed_id       SERIAL PRIMARY KEY,
    shelter_id   INT NOT NULL REFERENCES Shelters(shelter_id),
    bed_number   INT NOT NULL,
    status       VARCHAR(10) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available','Occupied')),
    UNIQUE (shelter_id, bed_number)
);
```
> Created at runtime when a shelter is added (one bed row per unit of capacity), in the same transaction as shelter creation.

## 5. `Bed_Allocations`
```sql
CREATE TABLE Bed_Allocations (
    allocation_id      SERIAL PRIMARY KEY,
    bed_id             INT NOT NULL REFERENCES Beds(bed_id),
    victim_id          INT NOT NULL REFERENCES Victims(victim_id),
    allocated_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vacated_date       TIMESTAMP,
    allocation_status  VARCHAR(10) NOT NULL DEFAULT 'Active' CHECK (allocation_status IN ('Active','Vacated'))
);
```
> **Business rule:** only ONE row per `bed_id` may have `allocation_status = 'Active'` at a time — enforced in application/service logic.
> **Design note:** `allocation_status` and `vacated_date` must always be updated together in the same transaction.
> **Optional enhancement:** Postgres supports **partial unique indexes**, which CAN enforce this rule at the DB level:
> `CREATE UNIQUE INDEX one_active_allocation_per_bed ON Bed_Allocations(bed_id) WHERE allocation_status = 'Active';`
> This is a genuine Postgres-specific feature MySQL can't easily replicate — worth mentioning in your report as a "bonus" DB-level safeguard if you choose to add it.

## 6. `Volunteers`
```sql
CREATE TABLE Volunteers (
    volunteer_id   SERIAL PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    phone          VARCHAR(15),
    skill          VARCHAR(50),
    availability   VARCHAR(15) NOT NULL DEFAULT 'Available' CHECK (availability IN ('Available','Unavailable')),
    shelter_id     INT REFERENCES Shelters(shelter_id)
);
```

## 7. `Inventory_Locations`
```sql
CREATE TABLE Inventory_Locations (
    location_id     SERIAL PRIMARY KEY,
    location_name   VARCHAR(100) NOT NULL,
    location_type   VARCHAR(10) NOT NULL CHECK (location_type IN ('Central','Shelter')),
    shelter_id      INT UNIQUE REFERENCES Shelters(shelter_id),
    CHECK (
        (location_type = 'Central' AND shelter_id IS NULL) OR
        (location_type = 'Shelter' AND shelter_id IS NOT NULL)
    )
);
```
> Represents where inventory physically sits — one Central Warehouse, plus one location per shelter. Postgres **always enforces `CHECK` constraints** (no version caveat, unlike older MySQL) — this rule is genuinely guaranteed at the DB level here.
> **`UNIQUE(shelter_id)`** enforces one inventory location per shelter. **Caution:** like MySQL, Postgres also allows multiple `NULL` values in a `UNIQUE` column, so this does NOT by itself guarantee only one Central location — the single Central Warehouse is created during system initialization, and the application must prevent additional `location_type='Central'` rows from being created afterward.

## 8. `Inventory_Stock`
```sql
CREATE TABLE Inventory_Stock (
    stock_id             SERIAL PRIMARY KEY,
    item_id              INT NOT NULL REFERENCES Items(item_id),
    location_id          INT NOT NULL REFERENCES Inventory_Locations(location_id),
    quantity             INT NOT NULL CHECK (quantity >= 0),
    low_stock_threshold  INT NOT NULL DEFAULT 10 CHECK (low_stock_threshold >= 0),
    UNIQUE (item_id, location_id)
);
```
> How much of a given item exists at a given location. One row per item-location pair.

## 9. `Supply_Requests`
```sql
CREATE TABLE Supply_Requests (
    request_id           SERIAL PRIMARY KEY,
    shelter_id            INT NOT NULL REFERENCES Shelters(shelter_id),
    item_id               INT NOT NULL REFERENCES Items(item_id),
    quantity_requested     INT NOT NULL CHECK (quantity_requested > 0),
    priority              VARCHAR(10) NOT NULL DEFAULT 'Medium' CHECK (priority IN ('Low','Medium','High')),
    status                VARCHAR(10) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending','Approved','Rejected','Fulfilled')),
    request_date          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_date         TIMESTAMP
);
```
> A request says "I want Water," not "I want Central's stock-row #4 of Water." Points to `Items`, not a specific location's stock row.

## 10. `Inventory_Transactions`
```sql
CREATE TABLE Inventory_Transactions (
    transaction_id     SERIAL PRIMARY KEY,
    item_id            INT NOT NULL REFERENCES Items(item_id),
    from_location_id   INT NOT NULL REFERENCES Inventory_Locations(location_id),
    to_location_id     INT NOT NULL REFERENCES Inventory_Locations(location_id),
    quantity           INT NOT NULL CHECK (quantity > 0),
    transaction_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (from_location_id <> to_location_id)
);
```
> Records actual movement of supplies. No `transaction_type` column — every row is implicitly a transfer at this project's scope.

## 11. `Users`
```sql
CREATE TABLE Users (
    user_id          SERIAL PRIMARY KEY,
    username         VARCHAR(50) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    role             VARCHAR(10) NOT NULL CHECK (role IN ('Admin','Staff')),
    shelter_id       INT REFERENCES Shelters(shelter_id)
);
```
> Password stored only as a hash, never plain text. Admin's `shelter_id` may be NULL (system-wide access); Staff is tied to a shelter.

## 12. `Activity_Log`
```sql
CREATE TABLE Activity_Log (
    log_id           SERIAL PRIMARY KEY,
    user_id          INT NOT NULL REFERENCES Users(user_id),
    action           VARCHAR(100) NOT NULL,
    affected_table   VARCHAR(50) NOT NULL,
    affected_id      INT NOT NULL,
    timestamp        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
> `affected_id` is intentionally NOT a foreign key — deliberate polymorphic audit-log pattern, can reference rows in multiple different tables.

---

## Entity Relationship Overview

```
Shelters (1) ──< Beds (many)
Shelters (1) ──< Volunteers (many)
Shelters (1) ──< Supply_Requests (many)
Shelters (1) ──< Users (many)
Shelters (1) ── (0..1) Inventory_Locations   <- one shelter has at most one inventory location (UNIQUE(shelter_id))

Beds (1) ──< Bed_Allocations (many)
Victims (1) ──< Bed_Allocations (many)     <- Victim's shelter is derived through this chain

Items (1) ──< Inventory_Stock (many)
Items (1) ──< Supply_Requests (many)
Items (1) ──< Inventory_Transactions (many)

Inventory_Locations (1) ──< Inventory_Stock (many)
Inventory_Locations (1) ──< Inventory_Transactions (many, as from_location and to_location)

Users (1) ──< Activity_Log (many)
```

## Features → Tables/Logic Mapping

| Feature (from original 12) | Implementation |
|---|---|
| Shelter Management | `Shelters` |
| Victim Registration | `Victims` |
| Bed Allocation Management | `Beds` + `Bed_Allocations` |
| Volunteer Management | `Volunteers` |
| Relief Inventory Management | `Items` + `Inventory_Stock` + `Inventory_Locations` |
| Supply Request Management | `Supply_Requests` + `Inventory_Transactions` |
| Dashboard & Reporting | SQL views/aggregates across above tables |
| Search & Filtering | `WHERE` / `LIKE` (or Postgres `ILIKE` for case-insensitive search) — no dedicated table |
| User Management | `Users` |
| Activity Logging | `Activity_Log` |
| Database Integrity & Validation | PK / FK / CHECK / UNIQUE / NOT NULL across all tables |
| Analytics & Statistics | SQL views/aggregates (COUNT, SUM, AVG, GROUP BY) |

## The Central Showcase Transaction — Fulfilling a Supply Request

```
BEGIN TRANSACTION

1. Verify request.status = 'Approved'
2. Locate Central's Inventory_Stock row for the requested item
3. Check central_stock.quantity >= quantity_requested
4. Decrease central_stock.quantity
5. Find-or-create Inventory_Stock row for (item_id, shelter's location_id)
6. Increase that shelter stock row's quantity
7. Insert row into Inventory_Transactions
8. Update Supply_Requests.status = 'Fulfilled', set processed_date
9. Insert row into Activity_Log

COMMIT
-- If ANY step fails: ROLLBACK, nothing is left half-applied
```
This is your strongest DBMS showcase piece — a genuine multi-step, all-or-nothing transaction with rollback. In Spring Boot this maps directly to a `@Transactional` service method. Worth walking through explicitly in your report/demo.

## Key Integrity Rules Reference

| Rule | Enforced by |
|---|---|
| Shelter capacity > 0 | DB `CHECK` |
| Victim age ≥ 0 | DB `CHECK` |
| Bed number unique within shelter | `UNIQUE(shelter_id, bed_number)` |
| Only one active allocation per bed | Service/transaction logic (optional: Postgres partial unique index, see table 5) |
| allocation_status & vacated_date stay consistent | Service transaction (update together) |
| Item name unique in catalog | `UNIQUE` on `Items.item_name` |
| One stock row per item per location | `UNIQUE(item_id, location_id)` |
| Stock quantity ≥ 0 | DB `CHECK` |
| Supply request quantity > 0 | DB `CHECK` |
| Transfer quantity > 0 | DB `CHECK` |
| Transfer source ≠ destination | DB `CHECK` |
| Central location has no shelter_id / Shelter location must have one | DB `CHECK` — reliably enforced (PostgreSQL always applies CHECK, unlike MySQL < 8.0.16) |
| One inventory location per shelter | DB `UNIQUE(shelter_id)` |
| Only one Central Warehouse ever exists | Application logic (UNIQUE alone doesn't stop multiple NULLs, same behavior in Postgres) |
| Central stock cannot go negative during fulfillment | Transaction logic (+ note: no row-locking implemented — acceptable for single-user demo; Postgres supports `SELECT ... FOR UPDATE` if you want to mention the production-grade fix) |
| Username unique | DB `UNIQUE` |
| Password stored only as hash | Application layer (BCrypt via Spring Security) |
| Activity_Log.affected_id is polymorphic, not a true FK | Deliberate design choice |

## Key Design Decisions (for your project report)

1. **Victim's shelter is derived**, not stored — via `Bed_Allocations → Beds → Shelters`. Avoids duplication/update anomalies.
2. **Beds and Bed_Allocations are separate** — Beds = physical resource, Bed_Allocations = usage events over time.
3. **allocation_status kept alongside vacated_date** — minor accepted redundancy for readability and future extensibility (e.g. a 'Transferred' state), with the trade-off documented.
4. **Items catalog separated from Inventory_Stock** — avoids coupling a request/transfer to one specific location's stock row; "Water" is one concept regardless of where it's stocked.
5. **Activity_Log uses a polymorphic (affected_table + affected_id) pattern** — one log table for many entity types, at the cost of no enforced FK on affected_id.
6. **Role-based access kept to 2 roles (Admin/Staff)** — matches the actual access-control logic implemented, rather than adding unused roles for appearance.
7. **transaction_type omitted from Inventory_Transactions** — every row is implicitly a transfer at this project's scope.
8. **PostgreSQL chosen over MySQL** — guarantees `CHECK` constraint enforcement with no version dependency, which matters given how much of this schema's integrity relies on `CHECK` (capacity, quantities, the Central/Shelter location rule, transfer source ≠ destination).
9. **ENUM-like fields use `VARCHAR` + `CHECK (... IN (...))`** rather than native Postgres `ENUM` types — functionally identical validation, easier to modify later (native Postgres enums require `ALTER TYPE ... ADD VALUE`, which has its own quirks, e.g. can't run inside a transaction block in older versions).

---
*12 tables total. Implemented in PostgreSQL 17. Matches the running `emergency_shelter_db` database exactly.*

---

## Project setup & progress log

| Step | Status | Notes |
|---|---|---|
| Database software chosen | ✅ Done | PostgreSQL 17 — reliably enforces `CHECK` constraints |
| PostgreSQL + pgAdmin installed | ✅ Done | |
| Spring Boot project created | ✅ Done | Maven, Spring Web + Spring Data JPA + PostgreSQL Driver, linked to GitHub repo `Emergency-Shelter-Management` |
| Spring Boot ↔ PostgreSQL connection verified | ✅ Done | `application.properties` datasource pointed at `emergency_shelter_db`; clean startup logs confirmed |
| Database created | ✅ Done | `emergency_shelter_db` |
| All 12 tables created | ✅ Done | Ran via `create_tables.sql` in pgAdmin Query Tool |
| Sample data seeded | ✅ Done | Ran via `sample_data.sql`; verified rows in all 12 tables |
| JPA Entity classes | ⬜ Next | Starting with `Shelter.java`, then remaining 11 tables |
| Repository interfaces | ⬜ Pending | |
| Service layer (business logic + transactions) | ⬜ Pending | Bed allocation rule, supply fulfillment transaction |
| Controllers / REST endpoints | ⬜ Pending | |
| Authentication (Admin/Staff roles) | ⬜ Pending | |
| Frontend/UI | ⬜ Pending | |

### Setup notes for reference
- Passwords in `Users` table are placeholder hashes — replace with real BCrypt hashes once Spring Security is wired up.
- Development approach: keep a small seeded base (current sample data), then grow rows organically by using each feature through the app itself as it's built.
- GitHub repo: `Emergency-Shelter-Management` — Spring Boot project files merged into cloned repo folder, initial commit pushed.
- Database name in actual implementation is `emergency_shelter_db` (not `shelter_management`, which was an earlier placeholder name from planning).
