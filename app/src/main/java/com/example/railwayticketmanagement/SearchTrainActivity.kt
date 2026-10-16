package com.example.railwayticketmanagement

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.railwayticketmanagement.adapters.TrainAdapter
import com.example.railwayticketmanagement.models.Train
import com.example.railwayticketmanagement.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SearchTrainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var tvUserGreeting: TextView
    private lateinit var layoutGuestActions: LinearLayout
    private lateinit var layoutLoggedInActions: LinearLayout
    private lateinit var btnNavLogin: MaterialButton
    private lateinit var btnNavRegister: MaterialButton
    private lateinit var btnNavMyBookings: MaterialButton
    private lateinit var btnNavProfile: MaterialButton
    private lateinit var btnNavLogout: MaterialButton

    private lateinit var actFromStation: AutoCompleteTextView
    private lateinit var actToStation: AutoCompleteTextView
    private lateinit var btnSwapStations: ImageButton
    private lateinit var etJourneyDate: TextInputEditText
    private lateinit var btnSearchTrain: MaterialButton
    private lateinit var btnShowAllTrains: MaterialButton

    private lateinit var tvResultsCount: TextView
    private lateinit var tvSelectedDateSummary: TextView
    private lateinit var rvTrainResults: RecyclerView
    private lateinit var tvNoTrainsFound: TextView

    private lateinit var trainAdapter: TrainAdapter
    private var selectedJourneyDate: String = ""

    private val sampleStations = arrayOf("Ahmedabad", "Surat", "Mumbai", "Delhi")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_train)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        val headerView: View? = findViewById(R.id.headerLayout)
        com.example.railwayticketmanagement.utils.InsetsUtil.applyStatusBarPadding(headerView)

        initViews()
        setupStationAdapters()
        setupDatePicker()
        setupListeners()
        setupRecyclerView()

        // Set default journey date to today
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        selectedJourneyDate = dateFormat.format(calendar.time)
        etJourneyDate.setText(selectedJourneyDate)

        // Initial load: show all available sample trains
        loadAllTrains()
    }

    override fun onResume() {
        super.onResume()
        updateUserHeader()
        // Refresh train seat counts in case seats changed from recent bookings or cancellations
        refreshCurrentResults()
    }

    private fun initViews() {
        tvUserGreeting = findViewById(R.id.tvUserGreeting)
        layoutGuestActions = findViewById(R.id.layoutGuestActions)
        layoutLoggedInActions = findViewById(R.id.layoutLoggedInActions)
        btnNavLogin = findViewById(R.id.btnNavLogin)
        btnNavRegister = findViewById(R.id.btnNavRegister)
        btnNavMyBookings = findViewById(R.id.btnNavMyBookings)
        btnNavProfile = findViewById(R.id.btnNavProfile)
        btnNavLogout = findViewById(R.id.btnNavLogout)

        actFromStation = findViewById(R.id.actFromStation)
        actToStation = findViewById(R.id.actToStation)
        btnSwapStations = findViewById(R.id.btnSwapStations)
        etJourneyDate = findViewById(R.id.etJourneyDate)
        btnSearchTrain = findViewById(R.id.btnSearchTrain)
        btnShowAllTrains = findViewById(R.id.btnShowAllTrains)

        tvResultsCount = findViewById(R.id.tvResultsCount)
        tvSelectedDateSummary = findViewById(R.id.tvSelectedDateSummary)
        rvTrainResults = findViewById(R.id.rvTrainResults)
        tvNoTrainsFound = findViewById(R.id.tvNoTrainsFound)
    }

    private fun setupStationAdapters() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sampleStations)
        actFromStation.setAdapter(adapter)
        actToStation.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        etJourneyDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val chosenCalendar = Calendar.getInstance()
                    chosenCalendar.set(selectedYear, selectedMonth, selectedDay)
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    selectedJourneyDate = sdf.format(chosenCalendar.time)
                    etJourneyDate.setText(selectedJourneyDate)
                },
                year, month, day
            )
            // Allow only today or future dates
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun setupListeners() {
        // Swap stations with smooth rotation animation
        btnSwapStations.setOnClickListener {
            btnSwapStations.animate().rotationBy(180f).setDuration(250).start()
            val from = actFromStation.text.toString()
            val to = actToStation.text.toString()
            actFromStation.setText(to)
            actToStation.setText(from)
        }

        // Quick Route Chips
        findViewById<TextView?>(R.id.chipRoute1)?.setOnClickListener {
            actFromStation.setText("Ahmedabad")
            actToStation.setText("Mumbai")
            performSearch()
        }
        findViewById<TextView?>(R.id.chipRoute2)?.setOnClickListener {
            actFromStation.setText("Ahmedabad")
            actToStation.setText("Surat")
            performSearch()
        }
        findViewById<TextView?>(R.id.chipRoute3)?.setOnClickListener {
            actFromStation.setText("Surat")
            actToStation.setText("Mumbai")
            performSearch()
        }
        findViewById<TextView?>(R.id.chipRoute4)?.setOnClickListener {
            actFromStation.setText("Ahmedabad")
            actToStation.setText("Delhi")
            performSearch()
        }

        // Quick Date Chips
        findViewById<TextView?>(R.id.chipDateToday)?.setOnClickListener {
            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedJourneyDate = sdf.format(cal.time)
            etJourneyDate.setText(selectedJourneyDate)
        }
        findViewById<TextView?>(R.id.chipDateTomorrow)?.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            selectedJourneyDate = sdf.format(cal.time)
            etJourneyDate.setText(selectedJourneyDate)
        }

        // Search button
        btnSearchTrain.setOnClickListener {
            performSearch()
        }

        // Show all trains
        btnShowAllTrains.setOnClickListener {
            loadAllTrains()
        }

        // Guest action: Login
        btnNavLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Guest action: Register
        btnNavRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Logged-in action: My Bookings
        btnNavMyBookings.setOnClickListener {
            startActivity(Intent(this, MyBookingsActivity::class.java))
        }

        // Logged-in action: Profile
        btnNavProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Logged-in action: Logout with Confirmation Dialog
        btnNavLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out from your account?")
            .setPositiveButton("Logout") { _, _ ->
                session.logout()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                updateUserHeader()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupRecyclerView() {
        rvTrainResults.layoutManager = LinearLayoutManager(this)
        trainAdapter = TrainAdapter(emptyList(), selectedJourneyDate) { train ->
            handleTrainBookingClick(train)
        }
        rvTrainResults.adapter = trainAdapter
    }

    private fun performSearch() {
        val from = actFromStation.text.toString().trim()
        val to = actToStation.text.toString().trim()
        val date = etJourneyDate.text.toString().trim()

        if (from.isEmpty()) {
            actFromStation.error = "Please enter From station"
            actFromStation.requestFocus()
            return
        }

        if (to.isEmpty()) {
            actToStation.error = "Please enter To station"
            actToStation.requestFocus()
            return
        }

        if (from.equals(to, ignoreCase = true)) {
            Toast.makeText(this, "Source and Destination cannot be the same", Toast.LENGTH_SHORT).show()
            return
        }

        if (date.isEmpty()) {
            etJourneyDate.error = "Please select Journey Date"
            return
        }

        selectedJourneyDate = date
        val results = dbHelper.searchTrains(from, to)

        if (results.isEmpty()) {
            tvResultsCount.text = "No trains found"
            tvSelectedDateSummary.text = "Route: $from ➔ $to"
            rvTrainResults.visibility = View.GONE
            tvNoTrainsFound.visibility = View.VISIBLE
        } else {
            tvResultsCount.text = "Matching Trains (${results.size})"
            tvSelectedDateSummary.text = "Date: $selectedJourneyDate"
            rvTrainResults.visibility = View.VISIBLE
            tvNoTrainsFound.visibility = View.GONE
            trainAdapter.updateList(results, selectedJourneyDate)
        }
    }

    private fun loadAllTrains() {
        val allTrains = dbHelper.getAllTrains()
        tvResultsCount.text = "All Available Trains (${allTrains.size})"
        tvSelectedDateSummary.text = "Date: $selectedJourneyDate"
        rvTrainResults.visibility = View.VISIBLE
        tvNoTrainsFound.visibility = View.GONE
        trainAdapter.updateList(allTrains, selectedJourneyDate)
    }

    private fun refreshCurrentResults() {
        val from = actFromStation.text.toString().trim()
        val to = actToStation.text.toString().trim()
        if (from.isNotEmpty() && to.isNotEmpty() && !from.equals(to, ignoreCase = true)) {
            val results = dbHelper.searchTrains(from, to)
            if (results.isNotEmpty()) {
                trainAdapter.updateList(results, selectedJourneyDate)
                return
            }
        }
        loadAllTrains()
    }

    private fun handleTrainBookingClick(train: Train) {
        // Critical requirement 9: Check login state
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Please log in to book your ticket", Toast.LENGTH_SHORT).show()
            // Save pending train so user doesn't have to search again
            session.savePendingBooking(train.trainId, selectedJourneyDate)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            // User is logged in: navigate directly to Book Ticket
            val intent = Intent(this, BookTicketActivity::class.java).apply {
                putExtra("EXTRA_TRAIN_ID", train.trainId)
                putExtra("EXTRA_JOURNEY_DATE", selectedJourneyDate)
            }
            startActivity(intent)
        }
    }

    private fun updateUserHeader() {
        if (session.isLoggedIn()) {
            val name = session.getUserName()
            tvUserGreeting.text = "Hello, $name 👋"
            layoutGuestActions.visibility = View.GONE
            layoutLoggedInActions.visibility = View.VISIBLE
        } else {
            tvUserGreeting.text = "Welcome, Guest!"
            layoutGuestActions.visibility = View.VISIBLE
            layoutLoggedInActions.visibility = View.GONE
        }
    }
}
