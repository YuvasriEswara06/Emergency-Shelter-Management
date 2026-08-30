-- Emergency Shelter Management System — PostgreSQL schema
-- Run this whole script in pgAdmin's Query Tool, connected to emergency_shelter_db

-- 1. Shelters
CREATE TABLE Shelters (
                          shelter_id      SERIAL PRIMARY KEY,
                          name            VARCHAR(100) NOT NULL,
                          location        VARCHAR(150) NOT NULL,
                          capacity        INT NOT NULL CHECK (capacity > 0),
                          status          VARCHAR(10) NOT NULL DEFAULT 'Active' CHECK (status IN ('Active','Inactive'))
);

-- 2. Items (catalog)
CREATE TABLE Items (
                       item_id     SERIAL PRIMARY KEY,
                       item_name   VARCHAR(100) NOT NULL UNIQUE,
                       unit        VARCHAR(20) NOT NULL
);

-- 3. Victims
CREATE TABLE Victims (
                         victim_id           SERIAL PRIMARY KEY,
                         name                VARCHAR(100) NOT NULL,
                         age                 INT CHECK (age >= 0),
                         gender              VARCHAR(10) CHECK (gender IN ('Male','Female','Other')),
                         phone               VARCHAR(15),
                         address             VARCHAR(200),
                         registration_date   DATE NOT NULL DEFAULT CURRENT_DATE
);

-- 4. Beds
CREATE TABLE Beds (
                      bed_id       SERIAL PRIMARY KEY,
                      shelter_id   INT NOT NULL REFERENCES Shelters(shelter_id),
                      bed_number   INT NOT NULL,
                      status       VARCHAR(10) NOT NULL DEFAULT 'Available' CHECK (status IN ('Available','Occupied')),
                      UNIQUE (shelter_id, bed_number)
);

-- 5. Bed_Allocations
CREATE TABLE Bed_Allocations (
                                 allocation_id      SERIAL PRIMARY KEY,
                                 bed_id             INT NOT NULL REFERENCES Beds(bed_id),
                                 victim_id          INT NOT NULL REFERENCES Victims(victim_id),
                                 allocated_date     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 vacated_date       TIMESTAMP,
                                 allocation_status  VARCHAR(10) NOT NULL DEFAULT 'Active' CHECK (allocation_status IN ('Active','Vacated'))
);

-- 6. Volunteers
CREATE TABLE Volunteers (
                            volunteer_id   SERIAL PRIMARY KEY,
                            name           VARCHAR(100) NOT NULL,
                            phone          VARCHAR(15),
                            skill          VARCHAR(50),
                            availability   VARCHAR(15) NOT NULL DEFAULT 'Available' CHECK (availability IN ('Available','Unavailable')),
                            shelter_id     INT REFERENCES Shelters(shelter_id)
);

-- 7. Inventory_Locations
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

-- 8. Inventory_Stock
CREATE TABLE Inventory_Stock (
                                 stock_id             SERIAL PRIMARY KEY,
                                 item_id              INT NOT NULL REFERENCES Items(item_id),
                                 location_id          INT NOT NULL REFERENCES Inventory_Locations(location_id),
                                 quantity             INT NOT NULL CHECK (quantity >= 0),
                                 low_stock_threshold  INT NOT NULL DEFAULT 10 CHECK (low_stock_threshold >= 0),
                                 UNIQUE (item_id, location_id)
);

-- 9. Supply_Requests
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

-- 10. Inventory_Transactions
CREATE TABLE Inventory_Transactions (
                                        transaction_id     SERIAL PRIMARY KEY,
                                        item_id            INT NOT NULL REFERENCES Items(item_id),
                                        from_location_id   INT NOT NULL REFERENCES Inventory_Locations(location_id),
                                        to_location_id     INT NOT NULL REFERENCES Inventory_Locations(location_id),
                                        quantity           INT NOT NULL CHECK (quantity > 0),
                                        transaction_date   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        CHECK (from_location_id <> to_location_id)
);

-- 11. Users
CREATE TABLE Users (
                       user_id          SERIAL PRIMARY KEY,
                       username         VARCHAR(50) NOT NULL UNIQUE,
                       password_hash    VARCHAR(255) NOT NULL,
                       role             VARCHAR(10) NOT NULL CHECK (role IN ('Admin','Staff')),
                       shelter_id       INT REFERENCES Shelters(shelter_id)
);

-- 12. Activity_Log
CREATE TABLE Activity_Log (
                              log_id           SERIAL PRIMARY KEY,
                              user_id          INT NOT NULL REFERENCES Users(user_id),
                              action           VARCHAR(100) NOT NULL,
                              affected_table   VARCHAR(50) NOT NULL,
                              affected_id      INT NOT NULL,
                              timestamp        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
