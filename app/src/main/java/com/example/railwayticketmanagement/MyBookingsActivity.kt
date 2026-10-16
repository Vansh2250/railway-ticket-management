package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.railwayticketmanagement.adapters.BookingAdapter
import com.example.railwayticketmanagement.models.Booking
import com.example.railwayticketmanagement.utils.SessionManager

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var rvMyBookings: RecyclerView
    private lateinit var tvNoBookings: TextView
    private lateinit var btnHeaderBack: ImageButton
    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        val headerView: android.view.View? = findViewById(R.id.headerLayout)
        com.example.railwayticketmanagement.utils.InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Please log in to view your bookings", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        loadUserBookings()
    }

    override fun onResume() {
        super.onResume()
        loadUserBookings()
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE
        btnHeaderBack.setOnClickListener {
            finish()
        }

        rvMyBookings = findViewById(R.id.rvMyBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)
    }

    private fun setupRecyclerView() {
        rvMyBookings.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(emptyList()) { booking ->
            showCancelConfirmationDialog(booking)
        }
        rvMyBookings.adapter = bookingAdapter
    }

    private fun loadUserBookings() {
        val userId = session.getUserId()
        if (userId == -1L) return

        val bookings = dbHelper.getUserBookings(userId)
        if (bookings.isEmpty()) {
            rvMyBookings.visibility = View.GONE
            tvNoBookings.visibility = View.VISIBLE
        } else {
            rvMyBookings.visibility = View.VISIBLE
            tvNoBookings.visibility = View.GONE
            bookingAdapter.updateList(bookings)
        }
    }

    private fun showCancelConfirmationDialog(booking: Booking) {
        val currentUserId = session.getUserId()

        AlertDialog.Builder(this)
            .setTitle("Cancel Ticket Confirmation")
            .setMessage("Are you sure you want to cancel this ticket for ${booking.trainName}?\n\nPassenger Count: ${booking.passengerCount}\nTotal Fare: ₹${String.format("%.2f", booking.totalFare)}")
            .setPositiveButton("Yes, Cancel") { _, _ ->
                val success = dbHelper.cancelBooking(booking.bookingId, currentUserId)
                if (success) {
                    Toast.makeText(
                        this,
                        "Ticket cancelled successfully. Refunds are not processed because this is a simulated payment system.",
                        Toast.LENGTH_LONG
                    ).show()
                    loadUserBookings()
                } else {
                    Toast.makeText(
                        this,
                        "Cancellation failed or ticket is already cancelled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("No, Keep Booking", null)
            .show()
    }
}
