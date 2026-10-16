package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.Booking
import com.google.android.material.button.MaterialButton

class BookingConfirmationActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private var bookingId: Long = -1L
    private var booking: Booking? = null

    private lateinit var tvConfirmBookingId: TextView
    private lateinit var tvConfirmStatus: TextView
    private lateinit var tvConfirmTrainName: TextView
    private lateinit var tvConfirmRoute: TextView
    private lateinit var tvConfirmJourneyDate: TextView
    private lateinit var tvConfirmPassengerList: TextView
    private lateinit var tvConfirmFarePerPassenger: TextView
    private lateinit var tvConfirmPaymentStatus: TextView
    private lateinit var tvConfirmTotalFare: TextView
    private lateinit var btnBackToHome: MaterialButton
    private lateinit var btnViewMyBookings: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_confirmation)

        val headerView: android.view.View? = findViewById(R.id.headerLayout)
        com.example.railwayticketmanagement.utils.InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        bookingId = intent.getLongExtra("EXTRA_BOOKING_ID", -1L)

        if (bookingId == -1L) {
            Toast.makeText(this, "Booking reference not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadBookingDetails()
        setupListeners()
        setupBackDispatcher()
    }

    private fun initViews() {
        tvConfirmBookingId = findViewById(R.id.tvConfirmBookingId)
        tvConfirmStatus = findViewById(R.id.tvConfirmStatus)
        tvConfirmTrainName = findViewById(R.id.tvConfirmTrainName)
        tvConfirmRoute = findViewById(R.id.tvConfirmRoute)
        tvConfirmJourneyDate = findViewById(R.id.tvConfirmJourneyDate)
        tvConfirmPassengerList = findViewById(R.id.tvConfirmPassengerList)
        tvConfirmFarePerPassenger = findViewById(R.id.tvConfirmFarePerPassenger)
        tvConfirmPaymentStatus = findViewById(R.id.tvConfirmPaymentStatus)
        tvConfirmTotalFare = findViewById(R.id.tvConfirmTotalFare)
        btnBackToHome = findViewById(R.id.btnBackToHome)
        btnViewMyBookings = findViewById(R.id.btnViewMyBookings)
    }

    private fun loadBookingDetails() {
        booking = dbHelper.getBookingById(bookingId)
        if (booking == null) {
            Toast.makeText(this, "Unable to load booking details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val b = booking!!
        tvConfirmBookingId.text = "Booking ID: #${b.bookingId}"
        tvConfirmStatus.text = b.bookingStatus
        tvConfirmTrainName.text = "${b.trainName} (#${b.trainNumber})"
        tvConfirmRoute.text = "${b.source} ➔ ${b.destination}"
        tvConfirmJourneyDate.text = "Journey Date: ${b.journeyDate}"
        tvConfirmFarePerPassenger.text = "Fare per Passenger: ₹${b.farePerPassenger.toInt()}"
        tvConfirmPaymentStatus.text = "Payment: ${b.paymentStatus}"
        tvConfirmTotalFare.text = "₹${String.format("%.2f", b.totalFare)}"

        if (b.passengers.isNotEmpty()) {
            val listText = StringBuilder()
            b.passengers.forEachIndexed { index, p ->
                listText.append("${index + 1}. ${p.passengerName} (Age: ${p.age}, Gender: ${p.gender})\n")
            }
            tvConfirmPassengerList.text = listText.toString().trim()
        } else {
            tvConfirmPassengerList.text = "${b.passengerCount} Passenger(s)"
        }
    }

    private fun setupListeners() {
        btnBackToHome.setOnClickListener {
            navigateToHome()
        }

        btnViewMyBookings.setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setupBackDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    private fun navigateToHome() {
        val intent = Intent(this, SearchTrainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}
