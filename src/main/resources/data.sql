-- Optional starter/reference data.
-- By default this file is NOT auto-run (see note in README) because
-- spring.jpa.hibernate.ddl-auto=update creates tables from the entities,
-- and re-running INSERTs on every restart would duplicate rows.
-- To use it once for a demo: run these INSERT statements manually in
-- MySQL Workbench / CLI after the app has started once (so tables exist).

INSERT INTO vehicle_categories (category_name, description, seating_capacity, base_rate) VALUES
('Sedan', 'Comfortable 4-door family car', 5, 6000),
('SUV', 'Spacious sports utility vehicle', 7, 9500),
('Van', 'Large van for groups/cargo', 12, 12000),
('Motorcycle', 'Two-wheeler for solo trips', 1, 2000);

INSERT INTO vehicles (registration_no, brand, model, year, mileage, availability_status, category_id) VALUES
('WP-CAB-1234', 'Toyota', 'Corolla', 2022, 15000, 'AVAILABLE', 1),
('WP-CAJ-5678', 'Honda', 'CR-V', 2021, 22000, 'AVAILABLE', 2),
('WP-KL-9012', 'Toyota', 'HiAce', 2020, 40000, 'AVAILABLE', 3),
('WP-BC-3456', 'Bajaj', 'Pulsar', 2023, 5000, 'AVAILABLE', 4);

INSERT INTO employees (name, phone_no, email, position, role, driver_status) VALUES
('Nimal Perera', '0711234567', 'nimal.driver@vrs.lk', 'Senior Driver', 'DRIVER', 'AVAILABLE'),
('Kasun Silva', '0777654321', 'kasun.tech@vrs.lk', 'Maintenance Technician', 'MAINTENANCE_STAFF', NULL),
('Amara Fernando', '0765432198', 'amara.ops@vrs.lk', 'Operations Supervisor', 'OPERATIONS_SUPERVISOR', NULL);
