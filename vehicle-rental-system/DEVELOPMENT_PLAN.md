# Six-Member Development Plan & GitHub Workflow
**Group 2026-Y2-S1-MLB-B3G1-10 — Web-based Vehicle Rental System**

This document tells each member exactly what to build, which files are
theirs, and how to commit their work with a realistic, explainable history.
No commits have been made on your behalf — you must run these commands
yourselves so the GitHub history reflects your own work, per the
assignment's "Special Note: Use of AI Tools".

---

## 1. One-Time GitHub Setup (do this together, once)

```bash
# 1. One member creates the repository on GitHub (e.g. "vehicle-rental-system"),
#    Private, with the other 5 added as Collaborators.

# 2. Clone it locally (each member does this on their own machine)
git clone https://github.com/<your-org-or-username>/vehicle-rental-system.git
cd vehicle-rental-system

# 3. Copy in the starter project provided (this whole folder), then:
git add .
git commit -m "Initial project setup: Spring Boot skeleton, pom.xml, base structure"
git push origin main
```

## 2. Create the Six Member Branches

Run this once (any member, from `main`, after the initial commit above):

```bash
git checkout main
git pull origin main

git checkout -b member1 && git push -u origin member1
git checkout main
git checkout -b member2 && git push -u origin member2
git checkout main
git checkout -b member3 && git push -u origin member3
git checkout main
git checkout -b member4 && git push -u origin member4
git checkout main
git checkout -b member5 && git push -u origin member5
git checkout main
git checkout -b member6 && git push -u origin member6
```

Each member then works **only on their own branch** day-to-day:

```bash
git checkout member1     # (replace with your own branch name)
git pull origin member1
# ... make changes ...
git add <files>
git commit -m "Meaningful message"
git push origin member1
```

## 3. Branch → Member Mapping

| Branch | Member | Student ID | Module |
|---|---|---|---|
| `member1` | Panapitiya P.K.S.C. | IT25101915 | Vehicle Fleet Management |
| `member2` | Sandaruwan K.G.A. | IT25102862 | Booking & Reservation Management (+ Payment) |
| `member3` | Kithushan M. | IT25103726 | Emergency & Roadside Assistance |
| `member4` | Bathigama P.L. | IT25101875 | Vehicle Handover, Return & Damage Assessment |
| `member5` | Hitigedara S.S. | IT25103718 | Vehicle Recommendations & Trip Planning |
| `member6` | Malagahamuduna R.P.D.S. | IT25100976 | Maintenance Operations & Driver Allocation |

---

## 4. Member 1 — Panapitiya P.K.S.C. (IT25101915)
### Vehicle Fleet Management — branch `member1`

**Files you own:**
- `entity/Vehicle.java`, `entity/VehicleCategory.java`
- `repository/VehicleRepository.java`, `repository/VehicleCategoryRepository.java`
- `service/VehicleService.java`, `service/VehicleCategoryService.java`
- `controller/VehicleController.java`
- `templates/vehicle/list.html`, `templates/vehicle/form.html`

**Database tables:** `vehicles`, `vehicle_categories`

**APIs/functions implemented:**
- `listVehicles()` — GET `/vehicles`
- `showAddForm()` / `showEditForm()` — GET `/vehicles/add`, `/vehicles/edit/{id}`
- `saveVehicle()` — POST `/vehicles/save` (handles both create and update)
- `deleteVehicle()` — GET `/vehicles/delete/{id}`
- `markAsRented()` / `markAsAvailable()` — used internally by Member 2 and Member 6

**Suggested commit sequence:**
1. `Create Vehicle and VehicleCategory entities`
2. `Add Vehicle and VehicleCategory repositories`
3. `Implement VehicleService with CRUD operations`
4. `Add VehicleController with list and form endpoints`
5. `Create vehicle list page (Thymeleaf)`
6. `Create add/edit vehicle form page`
7. `Add vehicle availability status badges to list view`
8. `Fix vehicle category dropdown binding`
9. `Add basic validation for registration number`

**What to explain to the instructor:**
- How `AvailabilityStatus` prevents a rented/under-maintenance vehicle from
  appearing in the booking dropdown (see `VehicleService.getAvailableVehicles()`).
- Why `saveVehicle()` is used for both create and update (JPA's `save()`
  behavior: insert if ID is null, update if ID exists).
- How `EntityConverters` turns the category `<select>` value (just an ID)
  back into a full `VehicleCategory` object.

---

## 5. Member 2 — Sandaruwan K.G.A. (IT25102862)
### Booking & Reservation Management (+ Payment) — branch `member2`

**Files you own:**
- `entity/Reservation.java`, `entity/Payment.java`
- `repository/ReservationRepository.java`, `repository/PaymentRepository.java`
- `service/ReservationService.java`, `service/PaymentService.java`
- `controller/ReservationController.java`
- `templates/reservation/list.html`, `templates/reservation/form.html`

**Database tables:** `reservations`, `payments`

**APIs/functions implemented:**
- `createReservation()` — POST `/reservations/save` (calculates total
  amount, prevents booking an unavailable vehicle)
- `confirmReservation()` — GET `/reservations/confirm/{id}` (Operations
  Supervisor approval → marks vehicle as RENTED)
- `cancelReservation()` — GET `/reservations/cancel/{id}`
- `recordPayment()` — POST `/reservations/pay/{id}`

**Suggested commit sequence:**
1. `Create Reservation and Payment entities`
2. `Add Reservation and Payment repositories`
3. `Implement booking creation logic in ReservationService`
4. `Add vehicle availability check before booking`
5. `Implement total amount calculation based on category rate`
6. `Add ReservationController with booking endpoints`
7. `Create booking list and booking form pages`
8. `Implement reservation status workflow (pending/confirmed/cancelled)`
9. `Add payment recording feature`
10. `Fix double-booking prevention bug`

**What to explain to the instructor:**
- The overlap/double-booking guard in `createReservation()`.
- How confirming a reservation calls back into `VehicleService.markAsRented()`
  (cross-module collaboration between Member 1 and Member 2's code).
- Why no external payment gateway is used (Zero External Dependencies
  constraint from the proposal) and how `Payment` is just an internal record.

---

## 6. Member 3 — Kithushan M. (IT25103726)
### Emergency & Roadside Assistance — branch `member3`

**Files you own:**
- `entity/EmergencyRequest.java`
- `repository/EmergencyRequestRepository.java`
- `service/EmergencyService.java`
- `controller/EmergencyController.java`
- `templates/emergency/list.html`, `templates/emergency/form.html`

**Database tables:** `emergency_requests`

**APIs/functions implemented:**
- `createRequest()` — POST `/emergencies/save`
- `assignTechnician()` — GET `/emergencies/assign/{id}/{technicianId}`
- `updateStatus()` / resolve — GET `/emergencies/resolve/{id}`
- `cancelRequest()` — GET `/emergencies/cancel/{id}`

**Suggested commit sequence:**
1. `Create EmergencyRequest entity`
2. `Add EmergencyRequestRepository`
3. `Implement EmergencyService with request lifecycle`
4. `Add EmergencyController and emergency report form`
5. `Create emergency request list page with status badges`
6. `Implement technician assignment feature`
7. `Add resolve and cancel actions`
8. `Add emergency type dropdown (breakdown/accident/flat tyre/fuel)`

**What to explain to the instructor:**
- The `RequestStatus` state machine: `OPEN → TECHNICIAN_ASSIGNED → IN_PROGRESS → RESOLVED` (or `CANCELLED`).
- How this module reuses the shared `Employee` entity (built by Member 6 /
  shared infrastructure) rather than duplicating a technician table.

---

## 7. Member 4 — Bathigama P.L. (IT25101875)
### Vehicle Handover, Return & Damage Assessment — branch `member4`

**Files you own:**
- `entity/VehicleReturn.java`, `entity/DamageAssessment.java`, `entity/Damage.java`
- `repository/VehicleReturnRepository.java`, `repository/DamageAssessmentRepository.java`, `repository/DamageRepository.java`
- `service/VehicleReturnService.java`, `service/DamageAssessmentService.java`
- `controller/VehicleReturnController.java`
- `templates/handover/list.html`, `templates/handover/return-form.html`, `templates/handover/assessment-form.html`

**Database tables:** `vehicle_returns`, `damage_assessments`, `damages`

**APIs/functions implemented:**
- `recordReturn()` — POST `/returns/save` (also completes the reservation)
- `saveAssessment()` — POST `/returns/assess/save`
- `addDamage()` — POST `/returns/damage/add`

**Suggested commit sequence:**
1. `Create VehicleReturn, DamageAssessment and Damage entities`
2. `Add repositories for return and damage records`
3. `Implement VehicleReturnService - record return and close booking`
4. `Add VehicleReturnController with return form`
5. `Create vehicle return list and form pages`
6. `Implement DamageAssessmentService`
7. `Add damage assessment form linked to a return record`
8. `Fix return date validation`

**What to explain to the instructor:**
- Why recording a return automatically calls
  `reservationService.updateStatus(..., COMPLETED)` — closing the loop
  between Member 2's booking and Member 4's return.
- The one-to-one relationship between `Reservation` → `VehicleReturn` →
  `DamageAssessment` → many `Damage` rows.

---

## 8. Member 5 — Hitigedara S.S. (IT25103718)
### Vehicle Recommendations & Trip Planning — branch `member5`

**Files you own:**
- `entity/TripPlan.java`, `entity/VehicleRecommendation.java`
- `repository/TripPlanRepository.java`, `repository/VehicleRecommendationRepository.java`
- `service/TripPlanService.java`
- `controller/TripPlanController.java`
- `templates/tripplan/list.html`, `templates/tripplan/form.html`, `templates/tripplan/recommendations.html`

**Database tables:** `trip_plans`, `vehicle_recommendations`

**APIs/functions implemented:**
- `createTripPlan()` — POST `/tripplans/save` (estimates fuel cost from distance)
- `generateRecommendations()` — GET `/tripplans/{id}/recommendations`
  (rule-based scoring of available vehicles vs. passenger count)

**Suggested commit sequence:**
1. `Create TripPlan and VehicleRecommendation entities`
2. `Add repositories for trip planning`
3. `Implement fuel cost estimation in TripPlanService`
4. `Add TripPlanController and trip plan form`
5. `Create trip plan list page`
6. `Implement rule-based vehicle recommendation algorithm`
7. `Create recommendations results page`
8. `Tune suitability score formula based on seating capacity`

**What to explain to the instructor:**
- The recommendation scoring formula in `generateRecommendations()` — how
  seating capacity vs. passenger count produces a 0–100 suitability score.
- Why recommendations only consider vehicles that are currently `AVAILABLE`
  (reusing `VehicleService.getAvailableVehicles()` from Member 1).

---

## 9. Member 6 — Malagahamuduna R.P.D.S. (IT25100976)
### Maintenance Operations & Driver Allocation — branch `member6`

**Files you own:**
- `entity/MaintenanceRecord.java`, `entity/DriverAssignment.java`, `entity/Employee.java`
- `repository/MaintenanceRecordRepository.java`, `repository/DriverAssignmentRepository.java`, `repository/EmployeeRepository.java`
- `service/MaintenanceService.java`, `service/DriverAssignmentService.java`, `service/EmployeeService.java`
- `controller/MaintenanceController.java`, `controller/DriverController.java`
- `templates/maintenance/*.html`

**Database tables:** `maintenance_records`, `driver_assignments`, `employees`

**APIs/functions implemented:**
- `scheduleMaintenance()` — POST `/maintenance/save` (also marks vehicle `UNDER_MAINTENANCE`)
- `updateStatus()` — GET `/maintenance/inservice/{id}`, `/maintenance/complete/{id}`
- `cancelMaintenance()` — GET `/maintenance/cancel/{id}`
- `assignDriver()` — POST `/drivers/save`
- `updateStatus()` (assignment) — GET `/drivers/complete/{id}`

**Suggested commit sequence:**
1. `Create Employee entity with role and driver status`
2. `Create MaintenanceRecord and DriverAssignment entities`
3. `Add repositories for employee, maintenance and driver assignment`
4. `Implement MaintenanceService - schedule and complete maintenance`
5. `Add MaintenanceController and maintenance list/form pages`
6. `Implement DriverAssignmentService`
7. `Add DriverController and driver allocation pages`
8. `Link maintenance status to vehicle availability status`
9. `Fix driver status not updating after assignment`

**What to explain to the instructor:**
- How scheduling maintenance flips the vehicle to `UNDER_MAINTENANCE`
  (cross-module reuse of Member 1's `VehicleService`), and completing it
  flips the vehicle back to `AVAILABLE`.
- The `Employee` table's dual purpose: general staff record + driver
  availability tracking (`AVAILABLE / ON_SHIFT / OFF_DUTY`).

---

## 10. Shared Infrastructure (built together, Phase 1, before branching out)

One member (recommended: Member 1, since Fleet Management is simplest)
should push these to `main` **first**, so every other branch can build on
top of a working login system:

- `entity/User.java`, `entity/Role.java`
- `repository/UserRepository.java`
- `service/AuthService.java`
- `controller/AuthController.java`, `controller/HomeController.java`
- `config/PasswordConfig.java`, `config/SessionInterceptor.java`, `config/WebConfig.java`, `config/EntityConverters.java`
- `templates/index.html`, `templates/auth/login.html`, `templates/auth/register.html`, `templates/dashboard.html`, `templates/common/navbar.html`
- `static/css/style.css`

Suggested shared commits on `main`:
1. `Initial project setup: Spring Boot skeleton, pom.xml, base structure`
2. `Create User and Role entities for authentication`
3. `Implement AuthService with BCrypt password hashing`
4. `Add AuthController - register, login, logout`
5. `Create login and register pages`
6. `Add SessionInterceptor for basic RBAC`
7. `Create dashboard page and shared navbar`
8. `Add shared stylesheet`
9. `Add EntityConverters for form dropdown binding`

## 11. How to Merge the Six Branches Back Into `main`

Do this progressively (Phase 3 of the timeline — "Sub-function completion &
first integration"), **not** all at once at the end:

```bash
git checkout main
git pull origin main

git merge member1 --no-ff -m "Merge member1: Vehicle Fleet Management"
git push origin main

git merge member2 --no-ff -m "Merge member2: Booking & Reservation Management"
git push origin main

# ...repeat for member3, member4, member5, member6...
```

If two members touched the same shared file (e.g. `WebConfig.java` or
`navbar.html`), Git will show a merge conflict. Resolve it by opening the
file, keeping both members' additions (don't delete either person's lines),
then:
```bash
git add <resolved-file>
git commit
git push origin main
```

**Tip:** merge the two branches that depend on each other first (e.g.
Member 1's Fleet Management before Member 2's Booking, since booking needs
`Vehicle`), matching Phase 3 of your proposal's timeline.

## 12. Running & Testing the Complete System

1. Everyone pulls the merged `main` branch.
2. Follow the "How to Run" steps in `README.md`.
3. Manual test flow for the demo:
   1. Register as `FLEET_MANAGER` → add 2–3 vehicles (Member 1).
   2. Register as `CUSTOMER` → book one of those vehicles (Member 2).
   3. As the customer, report an emergency for that booking (Member 3).
   4. Record the vehicle's return and assess any damage (Member 4).
   5. Plan a trip and view vehicle recommendations (Member 5).
   6. Schedule maintenance on a vehicle and assign a driver to a booking (Member 6).
4. Confirm each screen matches the CRUD operations listed in Section 3 of
   `README.md`.

## 13. Preparing for the Viva / Final Presentation

Each member should be ready to:
- Open **their own branch's commit history** on GitHub and narrate it
  (`git log --oneline`), showing incremental, meaningful commits.
- Explain their entity's fields and why each relationship (`@ManyToOne`,
  `@OneToOne`) was chosen.
- Trace one full request through their module: Browser → Controller →
  Service → Repository → Database → back to the Thymeleaf page.
- Point out one place where their module depends on another member's code
  (e.g. Member 2 depends on Member 1's `Vehicle`), to demonstrate they
  understand the whole system, not just their slice.
