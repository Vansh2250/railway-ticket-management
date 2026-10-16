package com.example.railwayticketmanagement

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.railwayticketmanagement.models.Booking
import com.example.railwayticketmanagement.models.Passenger
import com.example.railwayticketmanagement.models.Payment
import com.example.railwayticketmanagement.models.Train
import com.example.railwayticketmanagement.models.User
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "RailwayDB"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "USERS"
        const val TABLE_TRAINS = "TRAINS"
        const val TABLE_BOOKINGS = "BOOKINGS"
        const val TABLE_PASSENGERS = "PASSENGERS"
        const val TABLE_PAYMENTS = "PAYMENTS"

        // USERS columns
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_MOBILE = "mobile"
        const val COL_USER_PASSWORD = "password"

        // TRAINS columns
        const val COL_TRAIN_ID = "train_id"
        const val COL_TRAIN_NUMBER = "train_number"
        const val COL_TRAIN_NAME = "train_name"
        const val COL_TRAIN_SOURCE = "source"
        const val COL_TRAIN_DESTINATION = "destination"
        const val COL_TRAIN_DEP_TIME = "departure_time"
        const val COL_TRAIN_ARR_TIME = "arrival_time"
        const val COL_TRAIN_FARE = "fare"
        const val COL_TRAIN_TOTAL_SEATS = "total_seats"
        const val COL_TRAIN_AVAIL_SEATS = "available_seats"

        // BOOKINGS columns
        const val COL_BOOKING_ID = "booking_id"
        const val COL_BOOKING_USER_ID = "user_id"
        const val COL_BOOKING_TRAIN_ID = "train_id"
        const val COL_BOOKING_JOURNEY_DATE = "journey_date"
        const val COL_BOOKING_PASSENGER_COUNT = "passenger_count"
        const val COL_BOOKING_TOTAL_FARE = "total_fare"
        const val COL_BOOKING_STATUS = "booking_status"
        const val COL_BOOKING_DATE = "booking_date"

        // PASSENGERS columns
        const val COL_PASSENGER_ID = "passenger_id"
        const val COL_PASSENGER_BOOKING_ID = "booking_id"
        const val COL_PASSENGER_NAME = "passenger_name"
        const val COL_PASSENGER_AGE = "age"
        const val COL_PASSENGER_GENDER = "gender"

        // PAYMENTS columns
        const val COL_PAYMENT_ID = "payment_id"
        const val COL_PAYMENT_BOOKING_ID = "booking_id"
        const val COL_PAYMENT_AMOUNT = "amount"
        const val COL_PAYMENT_METHOD = "payment_method"
        const val COL_PAYMENT_STATUS = "payment_status"
        const val COL_PAYMENT_DATE = "payment_date"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Create USERS table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_NAME TEXT NOT NULL,
                $COL_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COL_USER_MOBILE TEXT NOT NULL,
                $COL_USER_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        // 2. Create TRAINS table
        val createTrainsTable = """
            CREATE TABLE $TABLE_TRAINS (
                $COL_TRAIN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TRAIN_NUMBER TEXT NOT NULL,
                $COL_TRAIN_NAME TEXT NOT NULL,
                $COL_TRAIN_SOURCE TEXT NOT NULL,
                $COL_TRAIN_DESTINATION TEXT NOT NULL,
                $COL_TRAIN_DEP_TIME TEXT NOT NULL,
                $COL_TRAIN_ARR_TIME TEXT NOT NULL,
                $COL_TRAIN_FARE REAL NOT NULL,
                $COL_TRAIN_TOTAL_SEATS INTEGER NOT NULL,
                $COL_TRAIN_AVAIL_SEATS INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTrainsTable)

        // 3. Create BOOKINGS table
        val createBookingsTable = """
            CREATE TABLE $TABLE_BOOKINGS (
                $COL_BOOKING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BOOKING_USER_ID INTEGER NOT NULL,
                $COL_BOOKING_TRAIN_ID INTEGER NOT NULL,
                $COL_BOOKING_JOURNEY_DATE TEXT NOT NULL,
                $COL_BOOKING_PASSENGER_COUNT INTEGER NOT NULL,
                $COL_BOOKING_TOTAL_FARE REAL NOT NULL,
                $COL_BOOKING_STATUS TEXT NOT NULL,
                $COL_BOOKING_DATE TEXT NOT NULL,
                FOREIGN KEY ($COL_BOOKING_USER_ID) REFERENCES $TABLE_USERS($COL_USER_ID),
                FOREIGN KEY ($COL_BOOKING_TRAIN_ID) REFERENCES $TABLE_TRAINS($COL_TRAIN_ID)
            )
        """.trimIndent()
        db.execSQL(createBookingsTable)

        // 4. Create PASSENGERS table
        val createPassengersTable = """
            CREATE TABLE $TABLE_PASSENGERS (
                $COL_PASSENGER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PASSENGER_BOOKING_ID INTEGER NOT NULL,
                $COL_PASSENGER_NAME TEXT NOT NULL,
                $COL_PASSENGER_AGE INTEGER NOT NULL,
                $COL_PASSENGER_GENDER TEXT NOT NULL,
                FOREIGN KEY ($COL_PASSENGER_BOOKING_ID) REFERENCES $TABLE_BOOKINGS($COL_BOOKING_ID)
            )
        """.trimIndent()
        db.execSQL(createPassengersTable)

        // 5. Create PAYMENTS table
        val createPaymentsTable = """
            CREATE TABLE $TABLE_PAYMENTS (
                $COL_PAYMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PAYMENT_BOOKING_ID INTEGER NOT NULL,
                $COL_PAYMENT_AMOUNT REAL NOT NULL,
                $COL_PAYMENT_METHOD TEXT NOT NULL,
                $COL_PAYMENT_STATUS TEXT NOT NULL,
                $COL_PAYMENT_DATE TEXT NOT NULL,
                FOREIGN KEY ($COL_PAYMENT_BOOKING_ID) REFERENCES $TABLE_BOOKINGS($COL_BOOKING_ID)
            )
        """.trimIndent()
        db.execSQL(createPaymentsTable)

        // Seed initial sample train data
        insertSampleTrains(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PASSENGERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRAINS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ==========================================
    // SEED SAMPLE DATA
    // ==========================================
    private fun insertSampleTrains(db: SQLiteDatabase) {
        val sampleTrains = listOf(
            // Ahmedabad <-> Surat
            Train(0, "19034", "Gujarat Queen", "Ahmedabad", "Surat", "06:10 AM", "11:20 AM", 210.0, 90, 90),
            Train(0, "12010", "Shatabdi Express", "Ahmedabad", "Surat", "03:10 PM", "06:15 PM", 550.0, 80, 80),
            Train(0, "20902", "Vande Bharat Express", "Ahmedabad", "Surat", "05:45 AM", "08:50 AM", 750.0, 100, 100),
            Train(0, "19033", "Gujarat Queen", "Surat", "Ahmedabad", "05:10 AM", "10:20 AM", 210.0, 90, 90),
            Train(0, "12933", "Karnavati Express", "Surat", "Ahmedabad", "06:50 PM", "10:45 PM", 340.0, 85, 85),

            // Ahmedabad <-> Mumbai
            Train(0, "12902", "Gujarat Mail", "Ahmedabad", "Mumbai", "10:55 PM", "06:15 AM", 450.0, 60, 60),
            Train(0, "12932", "Double Decker Express", "Ahmedabad", "Mumbai", "06:00 AM", "01:05 PM", 520.0, 75, 75),
            Train(0, "12901", "Gujarat Mail", "Mumbai", "Ahmedabad", "10:05 PM", "05:35 AM", 450.0, 60, 60),
            Train(0, "20901", "Vande Bharat Express", "Mumbai", "Ahmedabad", "01:50 PM", "07:30 PM", 1250.0, 100, 100),
            Train(0, "12931", "Double Decker Express", "Mumbai", "Ahmedabad", "02:30 PM", "09:25 PM", 520.0, 75, 75),

            // Surat <-> Mumbai
            Train(0, "12926", "Paschim Express", "Surat", "Mumbai", "10:20 AM", "02:45 PM", 280.0, 50, 50),
            Train(0, "19038", "Avadh Express", "Surat", "Mumbai", "12:15 PM", "04:30 PM", 250.0, 60, 60),

            // Ahmedabad <-> Delhi
            Train(0, "12957", "Swarna Jayanti Rajdhani", "Ahmedabad", "Delhi", "05:45 PM", "07:30 AM", 1850.0, 60, 60),
            Train(0, "12915", "Ashram Express", "Ahmedabad", "Delhi", "07:15 PM", "10:00 AM", 680.0, 80, 80),
            Train(0, "12958", "Swarna Jayanti Rajdhani", "Delhi", "Ahmedabad", "07:55 PM", "09:30 AM", 1850.0, 60, 60),
            Train(0, "12916", "Ashram Express", "Delhi", "Ahmedabad", "03:20 PM", "06:20 AM", 680.0, 80, 80),

            // Mumbai <-> Delhi
            Train(0, "12951", "Mumbai Rajdhani", "Mumbai", "Delhi", "05:00 PM", "08:32 AM", 2100.0, 80, 80),
            Train(0, "12925", "Paschim Express", "Mumbai", "Delhi", "11:25 AM", "10:40 AM", 790.0, 70, 70)
        )

        for (train in sampleTrains) {
            val values = ContentValues().apply {
                put(COL_TRAIN_NUMBER, train.trainNumber)
                put(COL_TRAIN_NAME, train.trainName)
                put(COL_TRAIN_SOURCE, train.source)
                put(COL_TRAIN_DESTINATION, train.destination)
                put(COL_TRAIN_DEP_TIME, train.departureTime)
                put(COL_TRAIN_ARR_TIME, train.arrivalTime)
                put(COL_TRAIN_FARE, train.fare)
                put(COL_TRAIN_TOTAL_SEATS, train.totalSeats)
                put(COL_TRAIN_AVAIL_SEATS, train.availableSeats)
            }
            db.insert(TABLE_TRAINS, null, values)
        }
    }

    // ==========================================
    // USER OPERATIONS
    // ==========================================

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, user.name)
            put(COL_USER_EMAIL, user.email.trim().lowercase())
            put(COL_USER_MOBILE, user.mobile.trim())
            put(COL_USER_PASSWORD, user.password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun checkLogin(email: String, password: String): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE LOWER($COL_USER_EMAIL) = ? AND $COL_USER_PASSWORD = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email.trim().lowercase(), password))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL))
            val mobile = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_MOBILE))
            user = User(id, name, userEmail, mobile)
        }
        cursor.close()
        return user
    }

    fun checkEmailExists(email: String): Boolean {
        val db = readableDatabase
        val query = "SELECT $COL_USER_ID FROM $TABLE_USERS WHERE LOWER($COL_USER_EMAIL) = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email.trim().lowercase()))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserById(userId: Long): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COL_USER_ID = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(userId.toString()))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL))
            val mobile = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_MOBILE))
            user = User(id, name, email, mobile)
        }
        cursor.close()
        return user
    }

    fun updateUser(userId: Long, name: String, mobile: String, newPassword: String? = null): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_NAME, name)
            put(COL_USER_MOBILE, mobile.trim())
            if (!newPassword.isNullOrBlank()) {
                put(COL_USER_PASSWORD, newPassword)
            }
        }
        val rows = db.update(TABLE_USERS, values, "$COL_USER_ID = ?", arrayOf(userId.toString()))
        return rows > 0
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE LOWER($COL_USER_EMAIL) = ?"
        val cursor: Cursor = db.rawQuery(query, arrayOf(email.trim().lowercase()))
        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_USER_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME))
            val userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL))
            val mobile = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_MOBILE))
            user = User(id, name, userEmail, mobile)
        }
        cursor.close()
        return user
    }

    fun updatePasswordByEmail(email: String, newPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_PASSWORD, newPassword)
        }
        val rows = db.update(TABLE_USERS, values, "LOWER($COL_USER_EMAIL) = ?", arrayOf(email.trim().lowercase()))
        return rows > 0
    }

    // ==========================================
    // TRAIN OPERATIONS
    // ==========================================

    fun getAllTrains(): List<Train> {
        val trainList = mutableListOf<Train>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRAINS ORDER BY $COL_TRAIN_NAME ASC", null)
        if (cursor.moveToFirst()) {
            do {
                trainList.add(cursorToTrain(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return trainList
    }

    fun searchTrains(source: String, destination: String): List<Train> {
        val trainList = mutableListOf<Train>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_TRAINS WHERE LOWER($COL_TRAIN_SOURCE) = LOWER(?) AND LOWER($COL_TRAIN_DESTINATION) = LOWER(?) ORDER BY $COL_TRAIN_DEP_TIME ASC"
        val cursor = db.rawQuery(query, arrayOf(source.trim(), destination.trim()))
        if (cursor.moveToFirst()) {
            do {
                trainList.add(cursorToTrain(cursor))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return trainList
    }

    fun getTrainById(trainId: Long): Train? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRAINS WHERE $COL_TRAIN_ID = ?", arrayOf(trainId.toString()))
        var train: Train? = null
        if (cursor.moveToFirst()) {
            train = cursorToTrain(cursor)
        }
        cursor.close()
        return train
    }

    fun getAvailableSeats(trainId: Long): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_TRAIN_AVAIL_SEATS FROM $TABLE_TRAINS WHERE $COL_TRAIN_ID = ?", arrayOf(trainId.toString()))
        var seats = 0
        if (cursor.moveToFirst()) {
            seats = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRAIN_AVAIL_SEATS))
        }
        cursor.close()
        return seats
    }

    private fun cursorToTrain(cursor: Cursor): Train {
        return Train(
            trainId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TRAIN_ID)),
            trainNumber = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_NUMBER)),
            trainName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_NAME)),
            source = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_SOURCE)),
            destination = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_DESTINATION)),
            departureTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_DEP_TIME)),
            arrivalTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_ARR_TIME)),
            fare = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRAIN_FARE)),
            totalSeats = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRAIN_TOTAL_SEATS)),
            availableSeats = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRAIN_AVAIL_SEATS))
        )
    }

    // ==========================================
    // BOOKING OPERATIONS (TRANSACTIONAL)
    // ==========================================

    /**
     * Creates a ticket booking inside a SQLite transaction:
     * 1. Checks that available_seats >= passenger_count.
     * 2. Inserts booking record into BOOKINGS.
     * 3. Inserts each passenger into PASSENGERS.
     * 4. Inserts payment simulation record into PAYMENTS.
     * 5. Decreases available_seats in TRAINS by passenger_count.
     * 6. Commits transaction and returns the new bookingId, or -1 on failure.
     */
    fun createBooking(
        booking: Booking,
        passengers: List<Passenger>,
        paymentMethod: String
    ): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // 1. Re-check current seat availability from SQLite
            val cursor = db.rawQuery(
                "SELECT $COL_TRAIN_AVAIL_SEATS FROM $TABLE_TRAINS WHERE $COL_TRAIN_ID = ?",
                arrayOf(booking.trainId.toString())
            )
            if (!cursor.moveToFirst()) {
                cursor.close()
                return -1L
            }
            val currentSeats = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRAIN_AVAIL_SEATS))
            cursor.close()

            if (currentSeats < booking.passengerCount) {
                // Not enough seats available
                return -1L
            }

            // 2. Insert booking record
            val bookingValues = ContentValues().apply {
                put(COL_BOOKING_USER_ID, booking.userId)
                put(COL_BOOKING_TRAIN_ID, booking.trainId)
                put(COL_BOOKING_JOURNEY_DATE, booking.journeyDate)
                put(COL_BOOKING_PASSENGER_COUNT, booking.passengerCount)
                put(COL_BOOKING_TOTAL_FARE, booking.totalFare)
                put(COL_BOOKING_STATUS, "CONFIRMED")
                val currentDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                put(COL_BOOKING_DATE, currentDateStr)
            }
            val bookingId = db.insert(TABLE_BOOKINGS, null, bookingValues)
            if (bookingId == -1L) {
                return -1L
            }

            // 3. Insert passengers
            for (p in passengers) {
                val pValues = ContentValues().apply {
                    put(COL_PASSENGER_BOOKING_ID, bookingId)
                    put(COL_PASSENGER_NAME, p.passengerName)
                    put(COL_PASSENGER_AGE, p.age)
                    put(COL_PASSENGER_GENDER, p.gender)
                }
                val pId = db.insert(TABLE_PASSENGERS, null, pValues)
                if (pId == -1L) {
                    return -1L
                }
            }

            // 4. Insert payment record (SIMULATED SUCCESS)
            val paymentValues = ContentValues().apply {
                put(COL_PAYMENT_BOOKING_ID, bookingId)
                put(COL_PAYMENT_AMOUNT, booking.totalFare)
                put(COL_PAYMENT_METHOD, paymentMethod)
                put(COL_PAYMENT_STATUS, "SUCCESS")
                val paymentDateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                put(COL_PAYMENT_DATE, paymentDateStr)
            }
            val paymentId = db.insert(TABLE_PAYMENTS, null, paymentValues)
            if (paymentId == -1L) {
                return -1L
            }

            // 5. Deduct seats from TRAINS
            val newAvailableSeats = currentSeats - booking.passengerCount
            val trainValues = ContentValues().apply {
                put(COL_TRAIN_AVAIL_SEATS, newAvailableSeats)
            }
            val updated = db.update(
                TABLE_TRAINS,
                trainValues,
                "$COL_TRAIN_ID = ?",
                arrayOf(booking.trainId.toString())
            )
            if (updated <= 0) {
                return -1L
            }

            // Mark transaction as successful
            db.setTransactionSuccessful()
            return bookingId
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Cancels a booking inside a SQLite transaction:
     * 1. Checks that booking exists, belongs to userId, and status is "CONFIRMED".
     * 2. Updates booking status to "CANCELLED".
     * 3. Increases available_seats in TRAINS by passenger_count.
     * 4. Commits transaction and returns true.
     */
    fun cancelBooking(bookingId: Long, userId: Long): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Check booking details
            val query = "SELECT * FROM $TABLE_BOOKINGS WHERE $COL_BOOKING_ID = ? AND $COL_BOOKING_USER_ID = ?"
            val cursor = db.rawQuery(query, arrayOf(bookingId.toString(), userId.toString()))
            if (!cursor.moveToFirst()) {
                cursor.close()
                return false
            }

            val currentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKING_STATUS))
            if (currentStatus != "CONFIRMED") {
                // Already cancelled or not confirmed, prevent double cancellation
                cursor.close()
                return false
            }

            val trainId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKING_TRAIN_ID))
            val passengerCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKING_PASSENGER_COUNT))
            cursor.close()

            // Update booking status to CANCELLED
            val bValues = ContentValues().apply {
                put(COL_BOOKING_STATUS, "CANCELLED")
            }
            val rowsUpdated = db.update(
                TABLE_BOOKINGS,
                bValues,
                "$COL_BOOKING_ID = ? AND $COL_BOOKING_STATUS = 'CONFIRMED'",
                arrayOf(bookingId.toString())
            )
            if (rowsUpdated <= 0) {
                return false
            }

            // Restore seats
            val trainCursor = db.rawQuery(
                "SELECT $COL_TRAIN_AVAIL_SEATS, $COL_TRAIN_TOTAL_SEATS FROM $TABLE_TRAINS WHERE $COL_TRAIN_ID = ?",
                arrayOf(trainId.toString())
            )
            if (!trainCursor.moveToFirst()) {
                trainCursor.close()
                return false
            }
            val currentSeats = trainCursor.getInt(trainCursor.getColumnIndexOrThrow(COL_TRAIN_AVAIL_SEATS))
            val totalSeats = trainCursor.getInt(trainCursor.getColumnIndexOrThrow(COL_TRAIN_TOTAL_SEATS))
            trainCursor.close()

            val restoredSeats = (currentSeats + passengerCount).coerceAtMost(totalSeats)
            val tValues = ContentValues().apply {
                put(COL_TRAIN_AVAIL_SEATS, restoredSeats)
            }
            db.update(TABLE_TRAINS, tValues, "$COL_TRAIN_ID = ?", arrayOf(trainId.toString()))

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Retrieves all bookings for a specific user, enriched with train and payment details.
     */
    fun getUserBookings(userId: Long): List<Booking> {
        val bookings = mutableListOf<Booking>()
        val db = readableDatabase

        val query = """
            SELECT b.*, t.$COL_TRAIN_NUMBER, t.$COL_TRAIN_NAME, t.$COL_TRAIN_SOURCE,
                   t.$COL_TRAIN_DESTINATION, t.$COL_TRAIN_DEP_TIME, t.$COL_TRAIN_ARR_TIME,
                   t.$COL_TRAIN_FARE,
                   p.$COL_PAYMENT_STATUS, p.$COL_PAYMENT_METHOD
            FROM $TABLE_BOOKINGS b
            JOIN $TABLE_TRAINS t ON b.$COL_BOOKING_TRAIN_ID = t.$COL_TRAIN_ID
            LEFT JOIN $TABLE_PAYMENTS p ON b.$COL_BOOKING_ID = p.$COL_PAYMENT_BOOKING_ID
            WHERE b.$COL_BOOKING_USER_ID = ?
            ORDER BY b.$COL_BOOKING_ID DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            do {
                val booking = cursorToBooking(cursor)
                // Also load passengers
                booking.passengers = getPassengersByBookingId(booking.bookingId)
                bookings.add(booking)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return bookings
    }

    /**
     * Retrieves a single booking by ID with train, payment, and passenger details.
     */
    fun getBookingById(bookingId: Long): Booking? {
        val db = readableDatabase
        val query = """
            SELECT b.*, t.$COL_TRAIN_NUMBER, t.$COL_TRAIN_NAME, t.$COL_TRAIN_SOURCE,
                   t.$COL_TRAIN_DESTINATION, t.$COL_TRAIN_DEP_TIME, t.$COL_TRAIN_ARR_TIME,
                   t.$COL_TRAIN_FARE,
                   p.$COL_PAYMENT_STATUS, p.$COL_PAYMENT_METHOD
            FROM $TABLE_BOOKINGS b
            JOIN $TABLE_TRAINS t ON b.$COL_BOOKING_TRAIN_ID = t.$COL_TRAIN_ID
            LEFT JOIN $TABLE_PAYMENTS p ON b.$COL_BOOKING_ID = p.$COL_PAYMENT_BOOKING_ID
            WHERE b.$COL_BOOKING_ID = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(bookingId.toString()))
        var booking: Booking? = null
        if (cursor.moveToFirst()) {
            booking = cursorToBooking(cursor)
            booking.passengers = getPassengersByBookingId(bookingId)
        }
        cursor.close()
        return booking
    }

    private fun cursorToBooking(cursor: Cursor): Booking {
        val bId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKING_ID))
        val uId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKING_USER_ID))
        val tId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_BOOKING_TRAIN_ID))
        val jDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKING_JOURNEY_DATE))
        val pCount = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKING_PASSENGER_COUNT))
        val fare = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_BOOKING_TOTAL_FARE))
        val status = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKING_STATUS))
        val bDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BOOKING_DATE))

        val trainNum = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_NUMBER))
        val trainName = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_NAME))
        val src = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_SOURCE))
        val dst = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_DESTINATION))
        val depTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_DEP_TIME))
        val arrTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_TRAIN_ARR_TIME))
        val farePerP = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRAIN_FARE))

        val payStatus = if (cursor.getColumnIndex(COL_PAYMENT_STATUS) != -1) {
            cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_STATUS)) ?: "SUCCESS"
        } else "SUCCESS"

        val payMethod = if (cursor.getColumnIndex(COL_PAYMENT_METHOD) != -1) {
            cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_METHOD)) ?: "UPI"
        } else "UPI"

        return Booking(
            bookingId = bId,
            userId = uId,
            trainId = tId,
            journeyDate = jDate,
            passengerCount = pCount,
            totalFare = fare,
            bookingStatus = status,
            bookingDate = bDate,
            trainNumber = trainNum,
            trainName = trainName,
            source = src,
            destination = dst,
            departureTime = depTime,
            arrivalTime = arrTime,
            farePerPassenger = farePerP,
            paymentStatus = payStatus,
            paymentMethod = payMethod
        )
    }

    fun getPassengersByBookingId(bookingId: Long): List<Passenger> {
        val list = mutableListOf<Passenger>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PASSENGERS WHERE $COL_PASSENGER_BOOKING_ID = ? ORDER BY $COL_PASSENGER_ID ASC",
            arrayOf(bookingId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Passenger(
                        passengerId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PASSENGER_ID)),
                        bookingId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PASSENGER_BOOKING_ID)),
                        passengerName = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSENGER_NAME)),
                        age = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PASSENGER_AGE)),
                        gender = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSENGER_GENDER))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getPaymentByBookingId(bookingId: Long): Payment? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_PAYMENTS WHERE $COL_PAYMENT_BOOKING_ID = ?",
            arrayOf(bookingId.toString())
        )
        var payment: Payment? = null
        if (cursor.moveToFirst()) {
            payment = Payment(
                paymentId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PAYMENT_ID)),
                bookingId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_PAYMENT_BOOKING_ID)),
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_PAYMENT_AMOUNT)),
                paymentMethod = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_METHOD)),
                paymentStatus = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_STATUS)),
                paymentDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_PAYMENT_DATE))
            )
        }
        cursor.close()
        return payment
    }
}
