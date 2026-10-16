# Railway Ticket Management System

An Android application developed using **Kotlin**, **XML Layouts**, and Android's built-in **SQLite Database** (`SQLiteOpenHelper`).

This application was designed as an academic college project demonstrating Android fundamentals, relational database operations, transactions, login session management using `SharedPreferences`, dynamic form inputs, and `RecyclerView` components.

---

## 1. Project Introduction & Objectives

### Objective
To create a local, offline-capable Railway Ticket Management System where users can:
1. Search trains across multiple popular railway routes immediately upon launching the app (without requiring login).
2. View real-time available seats and fares in a clean, table-like layout.
3. Authenticate securely (Login / Register) with their credentials stored locally in SQLite.
4. Seamlessly continue to booking the selected train without re-searching.
5. Dynamically enter details for 1 to 6 passengers with automatic fare calculation.
6. Simulate payment and confirm bookings using SQLite database transactions.
7. Automatically deduct booked seats from the database and prevent overbooking.
8. View booking history in "My Bookings" and cancel tickets to automatically restore available seats.
9. Manage and update user profile information.

---

## 2. Technologies Used

- **IDE:** Android Studio
- **Programming Language:** Kotlin
- **UI Design:** Android XML Layouts, Material Design 3 Components
- **Local Database:** Android Built-in SQLite
  - `SQLiteOpenHelper`
  - `SQLiteDatabase`
  - `ContentValues`
  - `Cursor` and `rawQuery()`
  - Transactions: `beginTransaction()`, `setTransactionSuccessful()`, `endTransaction()`
- **Session Management:** `SharedPreferences`
- **List Display:** `RecyclerView` with custom `CardView` items
- **Architecture:** Activity-based clean architecture suitable for college viva explanation.

---

## 3. Database Design (`RailwayDB`)

The application uses an Android SQLite database named **`RailwayDB`** managed by `DatabaseHelper.kt`:

```kotlin
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "RailwayDB", null, 1)
```

Foreign key constraints are enabled via `onConfigure()`:
```kotlin
db.setForeignKeyConstraintsEnabled(true)
```

### Database Tables:

#### 1. `USERS` Table
Stores registered passenger accounts.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique user identifier |
| `name` | TEXT | NOT NULL | Passenger full name |
| `email` | TEXT | UNIQUE NOT NULL | User email address |
| `mobile` | TEXT | NOT NULL | 10-digit mobile number |
| `password` | TEXT | NOT NULL | User account password |

#### 2. `TRAINS` Table
Stores demonstration train schedules, routes, fares, and available seats.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `train_id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique train ID |
| `train_number` | TEXT | NOT NULL | Railway train number (e.g. "12902") |
| `train_name` | TEXT | NOT NULL | Train name (e.g. "Gujarat Mail") |
| `source` | TEXT | NOT NULL | Departure station |
| `destination` | TEXT | NOT NULL | Arrival station |
| `departure_time`| TEXT | NOT NULL | Departure time (e.g. "10:55 PM") |
| `arrival_time` | TEXT | NOT NULL | Arrival time (e.g. "06:15 AM") |
| `fare` | REAL | NOT NULL | Fare per passenger in ₹ |
| `total_seats` | INTEGER | NOT NULL | Total train seat capacity |
| `available_seats`| INTEGER| NOT NULL | Real-time seats available |

#### 3. `BOOKINGS` Table
Stores confirmed and cancelled ticket reservations.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `booking_id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique booking reference |
| `user_id` | INTEGER | FOREIGN KEY -> USERS(id) | Passenger who booked |
| `train_id` | INTEGER | FOREIGN KEY -> TRAINS(train_id) | Train reserved |
| `journey_date` | TEXT | NOT NULL | Date of journey (YYYY-MM-DD) |
| `passenger_count`| INTEGER| NOT NULL | Number of passengers (1-6) |
| `total_fare` | REAL | NOT NULL | Total ticket cost in ₹ |
| `booking_status`| TEXT | NOT NULL | "CONFIRMED" or "CANCELLED" |
| `booking_date` | TEXT | NOT NULL | Timestamp when booked |

#### 4. `PASSENGERS` Table
Stores individual traveler details for each booking.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `passenger_id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique passenger record |
| `booking_id` | INTEGER | FOREIGN KEY -> BOOKINGS(booking_id)| Linked booking ID |
| `passenger_name`| TEXT | NOT NULL | Traveler full name |
| `age` | INTEGER | NOT NULL | Age (1-120) |
| `gender` | TEXT | NOT NULL | "Male", "Female", or "Other" |

#### 5. `PAYMENTS` Table
Stores simulated payment transaction logs.
| Column | Type | Constraints | Description |
|---|---|---|---|
| `payment_id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Unique payment ID |
| `booking_id` | INTEGER | FOREIGN KEY -> BOOKINGS(booking_id)| Linked booking ID |
| `amount` | REAL | NOT NULL | Paid amount in ₹ |
| `payment_method`| TEXT | NOT NULL | UPI / Card / Net Banking (Simulated) |
| `payment_status`| TEXT | NOT NULL | "SUCCESS" |
| `payment_date` | TEXT | NOT NULL | Timestamp of payment |

---

## 4. Key SQLite Operations & Coding Style

In line with standard college syllabus and academic guidelines:

1. **Insert Records (`db.insert()` & `ContentValues`):**
   ```kotlin
   val values = ContentValues().apply {
       put("name", user.name)
       put("email", user.email)
       put("mobile", user.mobile)
       put("password", user.password)
   }
   db.insert("USERS", null, values)
   ```

2. **Query Records (`db.rawQuery()` & `Cursor`):**
   ```kotlin
   val cursor = db.rawQuery(
       "SELECT * FROM TRAINS WHERE LOWER(source) = LOWER(?) AND LOWER(destination) = LOWER(?)",
       arrayOf(source, destination)
   )
   ```

3. **Update Records (`db.update()`):**
   ```kotlin
   val values = ContentValues().apply {
       put("available_seats", newSeats)
   }
   db.update("TRAINS", values, "train_id = ?", arrayOf(trainId.toString()))
   ```

4. **Transactional Safety:**
   Ensures booking creation, passenger insertion, payment logging, and seat deduction happen as an atomic unit:
   ```kotlin
   db.beginTransaction()
   try {
       // Check seats, insert booking, passengers, payment, update seats
       db.setTransactionSuccessful()
   } finally {
       db.endTransaction()
   }
   ```

---

## 5. Application Screens & Flow

### 1. `SearchTrainActivity.kt` (Startup & Home Screen)
- **Always opens first** when the user launches the app.
- No login or registration required to search trains.
- Features:
  - Branded railway header with custom train logo.
  - User status indicator ("Welcome, Guest!" or "Hello, <User>!").
  - From Station and To Station with auto-suggestions and Station Swap button.
  - DatePicker dialog to select journey date (today or future).
  - "Search Train" button to filter matching trains.
  - "Show All" button to browse all 18 demonstration trains.
  - Table-like card list displaying Train No, Name, Timings, Route, Fare, and Available Seats.
  - "Book Ticket" button on each train result.

### 2. `LoginActivity.kt`
- Accessible from header navigation or automatically when a guest clicks "Book Ticket".
- Authenticates against SQLite `USERS` table.
- Stores session in `SharedPreferences`.
- Automatically redirects to the pending train booking screen so the user does not have to search again.

### 3. `RegisterActivity.kt`
- Form fields: Full Name, Email, Mobile (10 digits), Password, Confirm Password.
- Validates empty fields, email format, 10-digit mobile, and prevents duplicate emails.
- Saves user into SQLite and redirects to Login.

### 4. `BookTicketActivity.kt`
- Accessible only to logged-in users.
- Displays selected train summary and fresh available seat count from SQLite.
- Dynamic passenger form supporting **1 to 6 passengers**:
  - "+ Add Passenger" button dynamically inflates passenger input cards.
  - Trash/remove button removes specific passenger cards.
  - Automatic live calculation: `Total Fare = Fare per passenger × Passenger Count`.
- Validates passenger names, ages (1-120), and gender selections.
- Re-checks seat availability before proceeding to payment.

### 5. `PaymentActivity.kt`
- Displays booking summary, journey date, passenger count, and total fare.
- Simulated payment options: UPI, Debit/Credit Card, Net Banking.
- Clearly states that it is a simulation for an academic project (no real credentials or money).
- On "Pay Now": performs atomic SQLite transaction:
  - Re-verifies seats.
  - Inserts booking into `BOOKINGS` (`CONFIRMED`).
  - Inserts each passenger into `PASSENGERS`.
  - Inserts payment into `PAYMENTS` (`SUCCESS`).
  - Deducts available seats from `TRAINS`.
  - Commits transaction.

### 6. `BookingConfirmationActivity.kt`
- Displays green success checkmark and "Ticket Booked Successfully!".
- Formatted ticket confirmation displaying Booking ID, Train details, Route, Journey Date, Passenger list, Fare per passenger, Total Amount, and status badges (`CONFIRMED`, `SUCCESS`).
- "Back to Home" button returns to `SearchTrainActivity` where updated seat counts are instantly reflected.
- "View My Bookings" button navigates to booking history.

### 7. `MyBookingsActivity.kt`
- Displays only the currently logged-in user's bookings from SQLite.
- Shows Train details, Journey Date, Passenger count and names, Total Fare, and Status badge.
- For `CONFIRMED` tickets: displays a "Cancel Ticket" button.
- When cancelled:
  - Prompts for confirmation via dialog: "Are you sure you want to cancel this ticket?".
  - Updates booking status to `CANCELLED`.
  - Restores available seats in SQLite `TRAINS` table.
  - Displays message: "Ticket cancelled successfully. Refunds are not processed because this is a simulated payment system."
  - Prevents duplicate cancellation.

### 8. `ProfileActivity.kt`
- Displays logged-in user's Full Name, Email (read-only), and Mobile Number.
- Allows updating Full Name, Mobile Number (10 digits), and optionally changing Password.
- Saves updates in SQLite via `db.update()` and updates `SharedPreferences`.

---

## 6. Pre-populated Demonstration Train Data

18 demonstration trains are seeded in `DatabaseHelper.onCreate()` across major routes:
- **Ahmedabad ➔ Surat:** Gujarat Queen (#19034), Shatabdi Express (#12010), Vande Bharat Express (#20902)
- **Surat ➔ Ahmedabad:** Gujarat Queen (#19033), Karnavati Express (#12933)
- **Ahmedabad ➔ Mumbai:** Gujarat Mail (#12902), Double Decker Express (#12932)
- **Mumbai ➔ Ahmedabad:** Gujarat Mail (#12901), Vande Bharat Express (#20901), Double Decker Express (#12931)
- **Surat ➔ Mumbai:** Paschim Express (#12926), Avadh Express (#19038)
- **Ahmedabad ➔ Delhi:** Swarna Jayanti Rajdhani (#12957), Ashram Express (#12915)
- **Delhi ➔ Ahmedabad:** Swarna Jayanti Rajdhani (#12958), Ashram Express (#12916)
- **Mumbai ➔ Delhi:** Mumbai Rajdhani (#12951), Paschim Express (#12925)

---

## 7. How to Run the Project in Android Studio

1. Open **Android Studio**.
2. Click **File ➔ Open...** and select `e:\kotlinproject`.
3. Allow Gradle to sync the project.
4. Select an Android Emulator or connected physical device (Android 8.0+ / API 26+).
5. Click the green **Run (▶)** button or press `Shift + F10`.
6. The app will launch directly into the **Search Train** screen!

Alternatively, build the APK from terminal:
```bash
./gradlew assembleDebug
```
The generated APK is located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 8. Sample Viva Questions & Student Explanations

**Q1: Why does Search Train open first instead of Login?**
> In modern reservation systems (like IRCTC), users first look for train schedules and seat availability before deciding to log in. Login is only required when the user intends to perform a booking or view their personal reservations.

**Q2: How is seat availability kept consistent in SQLite?**
> We use SQLite transactions (`db.beginTransaction()`). When a booking is finalized, `available_seats` is reduced by the number of passengers (`available_seats = available_seats - passenger_count`). If anything fails, the entire transaction rolls back so seats are never lost or corrupted. When a ticket is cancelled, `available_seats` is increased by that same passenger count.

**Q3: How does the application remember the user after login?**
> We use Android's `SharedPreferences` via `SessionManager.kt`. When `checkLogin()` returns a valid user, we store the user's ID, Name, Email, and a boolean flag `isLoggedIn = true`.

**Q4: How does the guest booking flow remember the selected train?**
> If a guest clicks "Book Ticket", `SearchTrainActivity` saves the train's ID and journey date in `SessionManager.savePendingBooking()`. Once the guest registers or logs in, `LoginActivity` detects the pending booking and automatically takes the user directly to `BookTicketActivity`.

**Q5: What prevents a user from cancelling the same ticket twice?**
> In `DatabaseHelper.cancelBooking()`, the update query specifically targets `booking_status = 'CONFIRMED'`. If the ticket status is already `CANCELLED`, the query returns false and does not restore seats again.

---

## 9. Limitations of the Application

- Operates locally on the device using SQLite (no centralized server or live railway API).
- Payment is simulated for educational purposes (no real banking or gateway integration).
- SMS and email notifications are simulated via in-app confirmations.
