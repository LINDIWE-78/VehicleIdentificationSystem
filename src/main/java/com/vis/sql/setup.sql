

1. Customer table
CREATE TABLE IF NOT EXISTS Customer (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE
);

2. Vehicle table
CREATE TABLE IF NOT EXISTS Vehicle (
    vehicle_id SERIAL PRIMARY KEY,
    registration_number VARCHAR(20) UNIQUE NOT NULL,
    make VARCHAR(50),
    model VARCHAR(50),
    year INT,
    owner_id INT REFERENCES Customer(customer_id) ON DELETE CASCADE
);

3. ServiceRecord (Workshop module)
CREATE TABLE IF NOT EXISTS ServiceRecord (
    service_id SERIAL PRIMARY KEY,
    vehicle_id INT REFERENCES Vehicle(vehicle_id) ON DELETE CASCADE,
    service_date DATE NOT NULL,
    service_type VARCHAR(100),
    description TEXT,
    cost DECIMAL(10,2)
);

4. CustomerQuery
CREATE TABLE IF NOT EXISTS CustomerQuery (
    query_id SERIAL PRIMARY KEY,
    customer_id INT REFERENCES Customer(customer_id),
    vehicle_id INT REFERENCES Vehicle(vehicle_id),
    query_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    query_text TEXT NOT NULL,
    response_text TEXT
);

5. InsurancePolicy
CREATE TABLE IF NOT EXISTS InsurancePolicy (
    policy_id SERIAL PRIMARY KEY,
    vehicle_id INT REFERENCES Vehicle(vehicle_id),
    insurance_company VARCHAR(100),
    policy_number VARCHAR(50) UNIQUE,
    start_date DATE,
    end_date DATE,
    coverage_details TEXT
);

6. Claim
CREATE TABLE IF NOT EXISTS Claim (
    claim_id SERIAL PRIMARY KEY,
    policy_id INT REFERENCES InsurancePolicy(policy_id),
    claim_date DATE,
    claim_amount DECIMAL(10,2),
    status VARCHAR(20) CHECK (status IN ('Pending','Approved','Rejected'))
);

 7. PoliceReport
CREATE TABLE IF NOT EXISTS PoliceReport (
    report_id SERIAL PRIMARY KEY,
    vehicle_id INT REFERENCES Vehicle(vehicle_id),
    report_date DATE,
    report_type VARCHAR(50),
    description TEXT,
    officer_name VARCHAR(100)
);

 8. Violation
CREATE TABLE IF NOT EXISTS Violation (
    violation_id SERIAL PRIMARY KEY,
    vehicle_id INT REFERENCES Vehicle(vehicle_id),
    violation_date DATE,
    violation_type VARCHAR(100),
    fine_amount DECIMAL(8,2),
    status VARCHAR(20) CHECK (status IN ('Paid','Unpaid'))
);

9. AppUser (for authentication)
CREATE TABLE IF NOT EXISTS AppUser (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE
);


-- VIEW: v_vehicle_details (joins Vehicle + Customer)

CREATE OR REPLACE VIEW v_vehicle_details AS
SELECT
    v.vehicle_id,
    v.registration_number,
    v.make,
    v.model,
    v.year,
    c.customer_id,
    c.name AS owner_name,
    c.phone,
    c.email
FROM Vehicle v
JOIN Customer c ON v.owner_id = c.customer_id;


-- STORED PROCEDURE: add_violation

CREATE OR REPLACE PROCEDURE add_violation(
    vid INT,
    vdate DATE,
    vtype VARCHAR,
    amount DECIMAL
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO Violation (vehicle_id, violation_date, violation_type, fine_amount, status)
    VALUES (vid, vdate, vtype, amount, 'Unpaid');
END;
$$;


-- SAMPLE DATA (optional – for testing)


INSERT INTO Customer (name, address, phone, email) VALUES
('Mampo Joel', '123 Main St', '555-1234', 'mampoi@example.com'),
('Lerato Smith', '456 Oak Ave', '555-5678', 'Lerato@example.com')
ON CONFLICT (email) DO NOTHING;

INSERT INTO Vehicle (registration_number, make, model, year, owner_id) VALUES
('ABC-1234', 'Toyota', 'Camry', 2020, 1),
('XYZ-5678', 'Honda', 'Civic', 2019, 2)
ON CONFLICT (registration_number) DO NOTHING;

INSERT INTO AppUser (username, password, role, is_active) VALUES
('admin', 'admin123', 'ADMIN', TRUE),
('police1', 'pass', 'POLICE', TRUE)
ON CONFLICT (username) DO NOTHING;