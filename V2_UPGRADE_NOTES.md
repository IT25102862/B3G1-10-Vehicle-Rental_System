# V2 Upgrade Notes — From University Demo to Production-Style System

This document explains what changed from the original submission and why,
so your team can explain it in the viva. **Nothing about the original six
modules, database mapping, or GitHub branch strategy was thrown away** —
this is an incremental upgrade on top of the same project.

## 1. What's New

| Area | Before (v1) | Now (v2) |
|---|---|---|
| Landing page | None (basic login/register only) | Hero banner, live search bar, featured vehicles, "why us" section |
| Browsing | Staff-only fleet table | Public `/catalog` with search, filter (type/transmission/fuel/seats), and sort |
| Vehicle photos | None | Upload on add/edit, shown as cards everywhere, placeholder fallback if missing |
| Vehicle detail page | None | `/catalog/{id}` with specs grid, live availability checker (AJAX), Book Now |
| Booking flow | One admin-style form | Full customer checkout: pick dates/times → confirm → pay → receipt |
| Availability checking | Vehicle status flag only | Real date-range overlap check (`ReservationService.isVehicleAvailable`) |
| Payment | Manual "Pay" button in admin list | Dedicated payment page + receipt, confirms the booking automatically |
| Customer dashboard | None | `/dashboard` overview + sidebar (Bookings / Trips / Emergency / Profile) |
| Booking history | Admin-only list of *everyone's* bookings | `/my-bookings` — the logged-in customer's own bookings only |
| Profile management | None | `/profile` — edit contact info, licence number, upload a profile photo |
| Admin dashboard | None | `/admin` — live stats (fleet, bookings, revenue, customers) + quick links |
| Customer management | None | `/admin/customers` — staff can see every registered customer |
| Authorization | "Are you logged in?" only | Role-based: customers can't reach staff pages and vice versa (`SessionInterceptor`) |
| Validation | Minimal | Bean Validation on `Vehicle`/`User`, manual checks on registration (email format, password length, confirm password match) |
| Design | Plain table-and-form pages | Full design system: CSS variables, card grid, responsive breakpoints, consistent navbar/footer everywhere |

## 2. New/Changed Files (on top of the v1 structure)

```
entity/
  Vehicle.java        (+ imageUrl, description, transmission, fuelType, seats, color, location)
  Reservation.java     (+ pickupTime, returnTime, dropoffLocation, notes)
  User.java             (+ address, drivingLicenceNo, profileImageUrl, @Email/@NotBlank validation)

service/
  FileStorageService.java   (NEW - saves uploaded images to disk, returns a public URL)
  UserService.java          (NEW - profile updates + customer list for admin)
  VehicleService.java       (+ searchCatalog() for filter/sort)
  ReservationService.java   (+ isVehicleAvailable(), getBookingsForCustomer(), createReservation() now takes the customer explicitly)

controller/
  CatalogController.java        (NEW - public browse/search/detail/availability JSON endpoint)
  BookingController.java        (NEW - the full customer checkout flow)
  MyBookingsController.java     (NEW - customer booking history)
  ProfileController.java        (NEW - customer profile view/edit)
  AdminDashboardController.java (NEW - stats overview + customer management)
  VehicleController.java        (+ multipart image upload, @Valid validation)
  AuthController.java           (+ password confirmation + stronger validation)
  HomeController.java           (+ featured vehicles on landing page, role-based /dashboard routing)

config/
  WebConfig.java             (+ serves /uploads/** from disk, new public/protected path rules)
  SessionInterceptor.java    (rewritten: adds role-based authorization, not just login check)

templates/
  index.html                    (rebuilt - real landing page)
  catalog/browse.html, detail.html            (NEW)
  booking/book-form.html, confirmation.html,
          payment.html, receipt.html          (NEW)
  customer/dashboard.html, my-bookings.html,
           profile.html, sidebar.html         (NEW)
  admin/dashboard.html, customers.html,
        sidebar.html                          (NEW)
  common/footer.html            (NEW)
  common/navbar.html            (rebuilt - role-aware links)
  vehicle/list.html, form.html  (+ photo thumbnail/upload)
  reservation/list.html, form.html (+ new date/time fields, admin actions)

static/css/style.css   (completely rebuilt design system, ~350 lines, fully commented)
```

The original six-member module boundaries, entity relationships, and
database table mapping described in `README.md` and `DEVELOPMENT_PLAN.md`
are unchanged — this upgrade **adds** a public-facing layer and a
professional UI on top of the same backend.

## 3. How Image Uploads Work (explain this in the viva)

1. The form (`vehicle/form.html`, `customer/profile.html`) has
   `enctype="multipart/form-data"` and a `<input type="file">`.
2. The controller receives it as a `MultipartFile` parameter.
3. `FileStorageService.store(file, subFolder)` saves it to a real folder
   on disk (`uploads/vehicles/` or `uploads/profiles/`) **outside** the
   compiled JAR, with a randomly generated file name (so two people
   uploading "photo.jpg" never overwrite each other).
4. It returns a **web path** like `/uploads/vehicles/3f2a1c-photo.jpg`,
   which gets saved on the entity (`Vehicle.imageUrl`).
5. `WebConfig.addResourceHandlers()` tells Spring: "any request to
   `/uploads/**` should be served from that folder on disk." That's how
   `<img th:src="@{${vehicle.imageUrl}}">` displays the photo in the browser.

If no photo has been uploaded, templates fall back to a placeholder image
service (`https://placehold.co/...`) so the UI never looks broken.

## 4. How the Booking → Payment → Confirmation Flow Works

```
/catalog/{id}  (vehicle detail, check availability)
      │  clicks "Book Now"
      ▼
/book/{vehicleId}        GET  - shows date/time form
                          POST - validates dates, checks overlap,
                                 creates Reservation (PENDING_APPROVAL)
      ▼
/book/confirmation/{id}  GET  - shows booking summary
      │  clicks "Proceed to Payment"
      ▼
/book/payment/{id}       GET  - shows payment method form
                          POST - records a Payment row, then immediately
                                 sets the Reservation to CONFIRMED
      ▼
/book/receipt/{id}       GET  - final "Booking Confirmed" screen
```

This mirrors a real commercial rental site: the customer doesn't need
staff to manually approve a booking if they've already paid online. Staff
can still manually confirm/cancel bookings created over the phone via the
`/reservations` admin screen (`ReservationController`), which is a
separate, simpler path.

## 5. Known Simplifications (be upfront about these if asked)

- **Payment is simulated** — no real payment gateway is contacted (the
  proposal's "Zero External Dependencies" constraint). Card number/CVV
  fields on the payment page are decorative for now; only the selected
  *method* (Card / Bank Transfer / Cash) is actually recorded.
- **Authorization is role-group-based, not per-permission** — any
  non-CUSTOMER role (Fleet Manager, Operations Supervisor, Maintenance
  Staff, Driver) can currently reach every staff page. A production system
  would give each role its own specific permissions (e.g. only Fleet
  Manager can delete vehicles). This is flagged as a natural "Future Work"
  item for the final report.
- **Vehicle status vs. date-range availability** — `Vehicle.availabilityStatus`
  (AVAILABLE/RENTED/UNDER_MAINTENANCE) is still a single flag, while the
  new booking flow checks actual date overlaps. In a live system with many
  simultaneous future bookings, a full calendar-based availability model
  would replace the single status flag. For a small demo fleet, both work
  together correctly.
- **No automated tests** were added in this pass (kept out of scope to
  match the assignment's focus on functional CRUD + UI). Mention this as
  a natural extension for "Future Work" too.

## 6. Running It (same as before, nothing new to install)

Nothing changed in the run instructions from `README.md` — same
`mvn spring-boot:run`, same MySQL setup. The only new thing: the app will
create an `uploads/` folder next to wherever you run it from, the first
time someone uploads a photo. No extra configuration needed.
