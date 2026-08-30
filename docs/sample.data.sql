-- Emergency Shelter Management System — sample data
-- Run AFTER create_tables.sql, in the same emergency_shelter_db database

-- 1. Shelters
INSERT INTO Shelters (name, location, capacity, status) VALUES
                                                            ('Anna Nagar Shelter', 'Anna Nagar, Chennai', 10, 'Active'),
                                                            ('Velachery Shelter', 'Velachery, Chennai', 10, 'Active'),
                                                            ('Adyar Shelter', 'Adyar, Chennai', 5, 'Active');

-- 2. Items (catalog)
INSERT INTO Items (item_name, unit) VALUES
                                        ('Water', 'litres'),
                                        ('Rice', 'kg'),
                                        ('Blankets', 'pieces'),
                                        ('First Aid Kits', 'pieces');

-- 3. Victims
INSERT INTO Victims (name, age, gender, phone, address) VALUES
                                                            ('Ravi Kumar', 32, 'Male', '9840012345', 'Adyar, Chennai'),
                                                            ('Priya Selvam', 27, 'Female', '9840012346', 'Anna Nagar, Chennai'),
                                                            ('Arun Das', 45, 'Male', '9840012347', 'Velachery, Chennai'),
                                                            ('Meena Raj', 19, 'Female', '9840012348', 'Adyar, Chennai');

-- 4. Beds (a handful per shelter, not all — enough for a demo)
INSERT INTO Beds (shelter_id, bed_number, status) VALUES
                                                      (1, 1, 'Available'), (1, 2, 'Available'), (1, 3, 'Available'),
                                                      (2, 1, 'Available'), (2, 2, 'Available'), (2, 3, 'Available'),
                                                      (3, 1, 'Available'), (3, 2, 'Available');

-- 5. Bed_Allocations (occupy a few beds, leave some free)
INSERT INTO Bed_Allocations (bed_id, victim_id, allocation_status) VALUES
                                                                       (1, 1, 'Active'),   -- Ravi -> Anna Nagar bed 1
                                                                       (4, 2, 'Active'),   -- Priya -> Velachery bed 1
                                                                       (7, 3, 'Active');   -- Arun -> Adyar bed 1

-- This also updates bed status to Occupied for those beds:
UPDATE Beds SET status = 'Occupied' WHERE bed_id IN (1, 4, 7);

-- 6. Volunteers
INSERT INTO Volunteers (name, phone, skill, availability, shelter_id) VALUES
                                                                          ('Kavya Iyer', '9840099001', 'First Aid', 'Available', 1),
                                                                          ('Suresh Babu', '9840099002', 'Food distribution', 'Available', 2),
                                                                          ('Divya Shankar', '9840099003', 'Logistics', 'Unavailable', NULL);

-- 7. Inventory_Locations (one Central + one per shelter)
INSERT INTO Inventory_Locations (location_name, location_type, shelter_id) VALUES
                                                                               ('Central Warehouse', 'Central', NULL),
                                                                               ('Anna Nagar Inventory', 'Shelter', 1),
                                                                               ('Velachery Inventory', 'Shelter', 2),
                                                                               ('Adyar Inventory', 'Shelter', 3);

-- 8. Inventory_Stock (Central well-stocked, shelters running low — good for demo)
INSERT INTO Inventory_Stock (item_id, location_id, quantity, low_stock_threshold) VALUES
                                                                                      (1, 1, 1000, 100),  -- Central: Water
                                                                                      (2, 1, 500, 50),    -- Central: Rice
                                                                                      (3, 1, 300, 30),    -- Central: Blankets
                                                                                      (4, 1, 100, 10),    -- Central: First Aid Kits
                                                                                      (1, 2, 20, 30),      -- Anna Nagar: Water (below threshold -> low stock)
                                                                                      (3, 2, 5, 20),       -- Anna Nagar: Blankets (below threshold)
                                                                                      (1, 3, 40, 30),      -- Velachery: Water (fine)
                                                                                      (2, 4, 8, 15);       -- Adyar: Rice (below threshold)

-- 9. Supply_Requests
INSERT INTO Supply_Requests (shelter_id, item_id, quantity_requested, priority, status) VALUES
                                                                                            (1, 3, 90, 'High', 'Pending'),      -- Anna Nagar wants 90 more Blankets
                                                                                            (2, 1, 200, 'Medium', 'Pending'),   -- Velachery wants 200 Water
                                                                                            (3, 2, 50, 'Medium', 'Approved');   -- Adyar approved for 50 Rice

-- 10. Inventory_Transactions (one already-completed transfer, for history)
INSERT INTO Inventory_Transactions (item_id, from_location_id, to_location_id, quantity) VALUES
    (2, 1, 4, 20);  -- 20kg Rice moved from Central to Adyar previously

-- 11. Users (passwords are placeholder hashes — replace with real BCrypt hashes from Spring Boot)
INSERT INTO Users (username, password_hash, role, shelter_id) VALUES
                                                                  ('admin', '$2a$10$placeholderhashforadmin000000000000000000000000000', 'Admin', NULL),
                                                                  ('staff_annanagar', '$2a$10$placeholderhashforstaff0000000000000000000000000', 'Staff', 1);

-- 12. Activity_Log
INSERT INTO Activity_Log (user_id, action, affected_table, affected_id) VALUES
                                                                            (1, 'Added shelter', 'Shelters', 1),
                                                                            (1, 'Registered victim', 'Victims', 1),
                                                                            (2, 'Allocated bed', 'Bed_Allocations', 1);