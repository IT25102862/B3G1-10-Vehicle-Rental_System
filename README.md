# Web-based Vehicle Rental System
**SE2030 Software Engineering — Group 2026-Y2-S1-MLB-B3G1-10**

A simple Java Spring Boot web application implementing the six major functions
from the group's proposal report and EER diagram.

---

## 1. System Overview

Customers can register, browse the vehicle fleet, book a vehicle, plan trips
and get vehicle recommendations, and report roadside emergencies. Staff can
manage the fleet, approve bookings, record payments, process vehicle
handover/return and damage assessment, schedule maintenance, and allocate
drivers.

## 2. Technology Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 17 | Required by the assignment brief (Java-based web app) |
| Framework | Spring Boot 3.2 (Spring MVC) | Industry standard, well documented, taught alongside OOP |
| View | Thymeleaf | Server-rendered HTML, simple `th:` tags, no separate frontend build |
| Data access | Spring Data JPA (Hibernate) | Removes hand-written SQL for basic CRUD |
| Database | MySQL 8 | Matches the "Central Relational Database - MySQL" in the EER diagram |
| Build tool | Maven | Single `pom.xml`, one command to run |
| Password hashing | Spring Security Crypto (BCrypt only) | Satisfies NFR 4.1 without adding a full security filter chain |

We deliberately did **not** use Spring Security's full authentication
framework, Lombok's `@Builder`/`@Data` complex features, or React/Angular
frontends. Login is a plain `HttpSession` + a servlet interceptor
(`SessionInterceptor`) — this is the simplest correct way to gatekeep pages
and is easy to explain in a viva.

## 3. Architecture (3-layer)

```
Browser (Thymeleaf HTML)
        │
   Controller  (@Controller classes - handle HTTP requests)
        │
    Service    (@Service classes - business rules / validation)
        │
  Repository   (Spring Data JPA interfaces - talk to the database)
        │
      MySQL
```

Each of the six major functions follows this exact same pattern, so once a
member understands one module, they can explain all of them.

## 4. Six Major Functions → Package Mapping

| # | Module | Owner | Entities | Key files |
|---|---|---|---|---|
| 1 | Vehicle Fleet Management | IT25101915 Panapitiya P.K.S.C. | `Vehicle`, `VehicleCategory` | `VehicleController`, `VehicleService`, `vehicle/*.html` |
| 2 | Booking & Reservation Mgmt (+Payment) | IT25102862 Sandaruwan K.G.A. | `Reservation`, `Payment` | `ReservationController`, `ReservationService`, `PaymentService`, `reservation/*.html` |
| 3 | Emergency & Roadside Assistance | IT25103726 Kithushan M. | `EmergencyRequest` | `EmergencyController`, `EmergencyService`, `emergency/*.html` |
| 4 | Vehicle Handover, Return & Damage Assessment | IT25101875 Bathigama P.L. | `VehicleReturn`, `DamageAssessment`, `Damage` | `VehicleReturnController`, `VehicleReturnService`, `DamageAssessmentService`, `handover/*.html` |
| 5 | Vehicle Recommendations & Trip Planning | IT25103718 Hitigedara S.S. | `TripPlan`, `VehicleRecommendation` | `TripPlanController`, `TripPlanService`, `tripplan/*.html` |
| 6 | Maintenance Operations Mgmt (+ Driver Allocation) | IT25100976 Malagahamuduna R.P.D.S. | `MaintenanceRecord`, `DriverAssignment`, `Employee` | `MaintenanceController`, `DriverController`, `MaintenanceService`, `DriverAssignmentService`, `maintenance/*.html` |

**Shared infrastructure** (built once, in Phase 1, so every member can build
on top of it): `User`, `Role`, `AuthController`, `AuthService`,
`SessionInterceptor`, `WebConfig`, `EntityConverters`, `style.css`,
`common/navbar.html`. Whoever sets this up first (recommend Member 1, since
Fleet Management is the simplest module to start on `main`) should push it
before the others branch off, so nobody duplicates login code.

## 5. Assumptions Made (documented per the assignment's request)

1. **Payment & Billing** appears in the proposal's objectives/scope but was
   **not** listed as one of the "Six Major Functions" table (Section 6 of
   the proposal only lists Fleet, Booking, Emergency, Handover, Trip
   Planning, Maintenance). We therefore implemented Payment as a
   sub-feature of Member 2's Booking & Reservation module, since a
   reservation must exist before it can be paid for.
2. **Driver Allocation** is grouped with Maintenance under Member 6's
   "Maintenance Operations Management System", following the proposal's own
   wording ("Core Capabilities" for that function include assigning
   drivers).
3. **Authentication/RBAC** is treated as shared infrastructure (per the
   proposal's own architecture diagram: "Shared Infrastructure: Auth, RBAC
   & Session Security"), not owned by a single member.
4. A single `User` table (with a `role` column) is used for both customers
   and staff logins, instead of separate Customer/Employee login tables, to
   keep the authentication code simple to explain. Staff-only operational
   details (position, driver status) still live in a separate `Employee`
   table, matching the EER diagram.
5. No external payment gateway is integrated, matching the proposal's
   "Zero External Dependencies" constraint — payments are recorded
   internally only.
6. Vehicle recommendation logic (Member 5) uses a simple rule-based scoring
   formula (seating capacity vs. passenger count), not machine learning,
   since the proposal only asks for "recommended vehicles" without
   specifying an algorithm.

## 6. Database Design

The database schema is generated automatically from the JPA entity classes
in `src/main/java/com/sliit/vrs/entity/` (see `spring.jpa.hibernate.ddl-auto=update`
in `application.properties`) — you do **not** need to write `CREATE TABLE`
statements by hand. This matches the EER diagram already produced for the
IT2140 Database Design assignment (Part B).

Table ↔ Entity ↔ EER diagram mapping:

| EER Entity | Java Entity | Table name |
|---|---|---|
| Customer + login | `User` | `users` |
| Employee | `Employee` | `employees` |
| Vehicle_Category | `VehicleCategory` | `vehicle_categories` |
| Vehicle | `Vehicle` | `vehicles` |
| Reservation | `Reservation` | `reservations` |
| Payment | `Payment` | `payments` |
| Emergency_Request | `EmergencyRequest` | `emergency_requests` |
| Vehicle_Return | `VehicleReturn` | `vehicle_returns` |
| Damage_Assessment | `DamageAssessment` | `damage_assessments` |
| Damage | `Damage` | `damages` |
| Trip_Plan | `TripPlan` | `trip_plans` |
| Vehicle_Recommendation | `VehicleRecommendation` | `vehicle_recommendations` |
| Maintenance_Record | `MaintenanceRecord` | `maintenance_records` |
| Driver assignment (from "Performed_In"/Employee) | `DriverAssignment` | `driver_assignments` |

`Branch`, `Vehicle_Exchange`, and multi-level `IS_A` vehicle subtypes
(Car/Van/Motorcycle) from the EER diagram were simplified for the
prototype — see "Future Work" in the final report. They can be added later
following the same entity → repository → service → controller pattern.

## 7. How to Run the Project Locally

### Prerequisites
- Java 17+ (`java -version`)
- Maven 3.8+ (`mvn -version`) — or use the included wrapper if you add one
- MySQL 8 running locally (or update the connection string for another host)

### Steps
1. Start MySQL and create the database (or let the app create it for you —
   `application.properties` already has `createDatabaseIfNotExist=true`):
   ```sql
   CREATE DATABASE vrs_db;
   ```
2. Open `src/main/resources/application.properties` and set your own MySQL
   `username` / `password`.
3. From the project root, run:
   ```bash
   mvn spring-boot:run
   ```
4. Open a browser at **http://localhost:8080**
5. Click **Register**, create an account (pick a role, e.g. `CUSTOMER` or
   `FLEET_MANAGER`), then **Login**.
6. (Optional) Insert the sample data in `src/main/resources/data.sql`
   manually (via MySQL Workbench or CLI) once the tables exist, to have
   some vehicles/employees to test with immediately.

### Building a runnable JAR
```bash
mvn clean package
java -jar target/vehicle-rental-system-1.0.0.jar
```

## 8. Design Patterns Used (for the "Application of Design Patterns" rubric)

- **MVC (Model–View–Controller)** — the whole application structure.
- **Repository Pattern** — `*Repository` interfaces isolate persistence
  logic from business logic (`Service` classes never write SQL directly).
- **Dependency Injection** — Spring `@Autowired` wires services and
  repositories together instead of using `new` everywhere.
- **DTO-free simple binding** via converters (`EntityConverters`) is a
  light form of the **Converter/Adapter pattern**, translating an HTML
  form's raw ID string into a full JPA entity.

## 9. Project Structure

```
vehicle-rental-system/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/sliit/vrs/
    │   ├── VrsApplication.java
    │   ├── entity/        (JPA @Entity classes - one per DB table)
    │   ├── repository/    (Spring Data JPA interfaces)
    │   ├── service/       (business logic, one service per module)
    │   ├── controller/    (HTTP endpoints, one controller per module)
    │   └── config/        (shared: security, session, form converters)
    └── resources/
        ├── application.properties
        ├── data.sql        (optional sample data - run manually)
        ├── static/css/style.css
        └── templates/      (Thymeleaf HTML pages, grouped per module)
```

See `DEVELOPMENT_PLAN.md` for the full GitHub branch strategy, member-by-
member commit sequence, and instructions for the viva.
