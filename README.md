Vehicle Identification System

A JavaFX desktop application integrated with PostgreSQL for managing vehicle information, customers, insurance, police violations, and administrative operations.

---

 Features

 Workshop Module

* Add, update, delete vehicles
* Manage service records
* Search vehicles by registration number or make

 Customer Module

* View customer profile
* View owned vehicles
* Submit and track customer queries

 Insurance Module

* Manage insurance policies
* Manage insurance claims
* Track claim status

## Police Module

* Record violations
* Record police reports
* Filter violations by date range

## Admin Module

* User management
* Activate/deactivate users
* Respond to customer queries

## Dashboard Module

* Total vehicles
* Total customers
* Pending claims
* Active users
* Open violations

## Statistics Module

* Pie charts
* Bar charts
* Line charts

---

# Technologies Used

* Java 17
* JavaFX
* PostgreSQL
* JDBC
* Maven
* Git & GitHub

---

# Database Features

## View

```sql
CREATE OR REPLACE VIEW v_vehicle_details AS
SELECT v.vehicle_id, v.registration_number, v.make, v.model, v.year,
       c.customer_id, c.name AS owner_name, c.phone, c.email
FROM Vehicle v
JOIN Customer c ON v.owner_id = c.customer_id;
```

## Stored Procedure

```sql
CREATE OR REPLACE PROCEDURE add_violation(
    vid INT,
    vdate DATE,
    vtype VARCHAR,
    amount DECIMAL
)
LANGUAGE plpgsql AS $$
BEGIN
    INSERT INTO Violation (
        vehicle_id,
        violation_date,
        violation_type,
        fine_amount,
        status
    )
    VALUES (
        vid,
        vdate,
        vtype,
        amount,
        'Unpaid'
    );
END;
$$;
```

---

# Project Structure

```text
VehicleIdentificationSystem/
├── src/main/java/com/vis/
├── controllers/
├── models/
├── dao/
├── db/
├── utils/
├── src/main/resources/
├── sql/
└── screenshots/
```

---

# Setup Instructions

## Prerequisites

* Java 17 or later
* PostgreSQL 14+
* Git
* IntelliJ IDEA / NetBeans / VS Code

---

# How to Run

## 1. Clone Repository

```bash
git clone https://github.com/LINDIWE-78/VehicleIdentificationSystem.git
```

## 2. Create Database

Create database:

```sql
CREATE DATABASE vehicle_db;
```

## 3. Run SQL Script

Execute:

```text
sql/setup.sql
```

This creates:

* tables
* relationships
* sample data
* views
* stored procedures

## 4. Configure Database

Edit:

```text
src/main/resources/DatabaseConfig.properties
```

```properties
db.url=jdbc:postgresql://localhost:5432/vehicle_db
db.user=postgres
db.password=yourpassword
```

## 5. Run Application

Run:

```text
MainApp.java
```

---

# Sample Login Credentials

| Role   | Username | Password |
| ------ | -------- | -------- |
| ADMIN  | admin    | admin123 |
| POLICE | police1  | pass     |

---

# Screenshots

## Login Screen

![Login](screenshots/login.png)

## Admin Dashboard

![Admin](screenshots/admin.png)

## Workshop Module

![Workshop](screenshots/workshop.png)

## Customer Module

![Customer](screenshots/customer.png)

## Insurance Module

![Insurance](screenshots/insurance.png)

## Police Module

![Police](screenshots/police.png)

## Statistics Module

![Statistics](screenshots/statistics.png)

---

# OOP Concepts Demonstrated

* Inheritance
* Polymorphism
* Encapsulation
* Abstraction
* MVC Pattern
* DAO Pattern

---

# Exception Handling

* Try-catch blocks for database operations
* User-friendly alerts
* JDBC exception handling

---

# GitHub Repository

https://github.com/LINDIWE-78/VehicleIdentificationSystem

---

# Author

LINDIWE-78

---

# License

This project is for educational purposes.
