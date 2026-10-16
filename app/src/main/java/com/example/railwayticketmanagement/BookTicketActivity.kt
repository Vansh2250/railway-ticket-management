package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.Passenger
import com.example.railwayticketmanagement.models.Train
import com.example.railwayticketmanagement.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class BookTicketActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private var trainId: Long = -1L
    private var journeyDate: String = ""
    private var train: Train? = null

    private lateinit var tvBookTrainName: TextView
    private lateinit var tvBookTrainNumber: TextView
    private lateinit var tvBookRoute: TextView
    private lateinit var tvBookJourneyDate: TextView
    private lateinit var tvBookTiming: TextView
    private lateinit var tvBookSeatsAvailable: TextView
    private lateinit var tvBookFarePerPassenger: TextView
    private lateinit var btnAddPassenger: MaterialButton
    private lateinit var containerPassengers: LinearLayout
    private lateinit var tvFareCalculationFormula: TextView
    private lateinit var tvTotalFare: TextView
    private lateinit var tvBottomTotalFare: TextView
    private lateinit var btnProceedToPayment: MaterialButton
    private lateinit var btnHeaderBack: ImageButton

    private val MAX_PASSENGERS = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_ticket)

        val headerView: View? = findViewById(R.id.headerLayout)
        com.example.railwayticketmanagement.utils.InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        // Session check: only logged-in users can access
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Please log in to book your ticket", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        trainId = intent.getLongExtra("EXTRA_TRAIN_ID", -1L)
        journeyDate = intent.getStringExtra("EXTRA_JOURNEY_DATE") ?: ""

        if (trainId == -1L) {
            Toast.makeText(this, "Invalid train selected", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        loadTrainDetails()
        setupListeners()

        // Add the first initial passenger row
        addPassengerRow()
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE

        tvBookTrainName = findViewById(R.id.tvBookTrainName)
        tvBookTrainNumber = findViewById(R.id.tvBookTrainNumber)
        tvBookRoute = findViewById(R.id.tvBookRoute)
        tvBookJourneyDate = findViewById(R.id.tvBookJourneyDate)
        tvBookTiming = findViewById(R.id.tvBookTiming)
        tvBookSeatsAvailable = findViewById(R.id.tvBookSeatsAvailable)
        tvBookFarePerPassenger = findViewById(R.id.tvBookFarePerPassenger)

        btnAddPassenger = findViewById(R.id.btnAddPassenger)
        containerPassengers = findViewById(R.id.containerPassengers)

        tvFareCalculationFormula = findViewById(R.id.tvFareCalculationFormula)
        tvTotalFare = findViewById(R.id.tvTotalFare)
        tvBottomTotalFare = findViewById(R.id.tvBottomTotalFare)
        btnProceedToPayment = findViewById(R.id.btnProceedToPayment)
    }

    private fun loadTrainDetails() {
        train = dbHelper.getTrainById(trainId)
        if (train == null) {
            Toast.makeText(this, "Train not found in database", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val currentTrain = train!!
        tvBookTrainName.text = currentTrain.trainName
        tvBookTrainNumber.text = "#${currentTrain.trainNumber}"
        tvBookRoute.text = "${currentTrain.source} ➔ ${currentTrain.destination}"
        tvBookJourneyDate.text = journeyDate
        tvBookTiming.text = "Dep: ${currentTrain.departureTime} | Arr: ${currentTrain.arrivalTime}"
        tvBookSeatsAvailable.text = "Available: ${currentTrain.availableSeats} Seats"
        tvBookFarePerPassenger.text = "₹${currentTrain.fare.toInt()}"

        if (currentTrain.availableSeats <= 0) {
            Toast.makeText(this, "Sorry, no seats available for this train", Toast.LENGTH_LONG).show()
            btnProceedToPayment.isEnabled = false
            btnAddPassenger.isEnabled = false
        }
    }

    private fun setupListeners() {
        btnHeaderBack.setOnClickListener {
            finish()
        }

        btnAddPassenger.setOnClickListener {
            val currentCount = containerPassengers.childCount
            val available = train?.availableSeats ?: 0

            if (currentCount >= MAX_PASSENGERS) {
                Toast.makeText(this, "Maximum $MAX_PASSENGERS passengers allowed per booking", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentCount >= available) {
                Toast.makeText(this, "Cannot add more passengers than available seats ($available)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addPassengerRow()
        }

        btnProceedToPayment.setOnClickListener {
            proceedToPayment()
        }
    }

    private fun addPassengerRow() {
        val inflater = LayoutInflater.from(this)
        val rowView = inflater.inflate(R.layout.item_passenger_input, containerPassengers, false)

        val tvNumber: TextView = rowView.findViewById(R.id.tvPassengerNumber)
        val btnRemove: ImageButton = rowView.findViewById(R.id.btnRemovePassenger)

        val index = containerPassengers.childCount + 1
        tvNumber.text = "Passenger $index"

        btnRemove.setOnClickListener {
            if (containerPassengers.childCount > 1) {
                containerPassengers.removeView(rowView)
                updatePassengerIndexes()
                updateFareCalculation()
            }
        }

        containerPassengers.addView(rowView)
        updatePassengerIndexes()
        updateFareCalculation()
    }

    private fun updatePassengerIndexes() {
        val count = containerPassengers.childCount
        for (i in 0 until count) {
            val child = containerPassengers.getChildAt(i)
            val tvNumber: TextView = child.findViewById(R.id.tvPassengerNumber)
            val btnRemove: ImageButton = child.findViewById(R.id.btnRemovePassenger)

            tvNumber.text = "Passenger ${i + 1}"
            // Only show remove button if there are more than 1 passengers
            btnRemove.visibility = if (count > 1) View.VISIBLE else View.GONE
        }
    }

    private fun updateFareCalculation() {
        val passengerCount = containerPassengers.childCount
        val farePerPerson = train?.fare ?: 0.0
        val total = farePerPerson * passengerCount

        tvFareCalculationFormula.text = "₹${farePerPerson.toInt()} × $passengerCount Passenger(s)"
        tvTotalFare.text = "₹${String.format("%.2f", total)}"
        tvBottomTotalFare.text = "₹${String.format("%.2f", total)}"
    }

    private fun proceedToPayment() {
        val currentTrain = train ?: return
        val passengerCount = containerPassengers.childCount

        // 1. Recheck available seats in SQLite
        val availableSeats = dbHelper.getAvailableSeats(trainId)
        if (availableSeats < passengerCount) {
            Toast.makeText(this, "Not enough seats available for the selected train.", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Validate all passenger rows
        val passengerList = ArrayList<Passenger>()

        for (i in 0 until passengerCount) {
            val rowView = containerPassengers.getChildAt(i)
            val etName: TextInputEditText = rowView.findViewById(R.id.etPassengerName)
            val etAge: TextInputEditText = rowView.findViewById(R.id.etPassengerAge)
            val rgGender: RadioGroup = rowView.findViewById(R.id.rgPassengerGender)

            val name = etName.text.toString().trim()
            val ageStr = etAge.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Please enter passenger name"
                etName.requestFocus()
                return
            }

            if (ageStr.isEmpty()) {
                etAge.error = "Please enter age"
                etAge.requestFocus()
                return
            }

            val age = ageStr.toIntOrNull()
            if (age == null || age <= 0 || age > 120) {
                etAge.error = "Enter a valid age (1 - 120)"
                etAge.requestFocus()
                return
            }

            val selectedGenderId = rgGender.checkedRadioButtonId
            val gender = when (selectedGenderId) {
                R.id.rbFemale -> "Female"
                R.id.rbOther -> "Other"
                else -> "Male"
            }

            passengerList.add(
                Passenger(
                    passengerName = name,
                    age = age,
                    gender = gender
                )
            )
        }

        val totalFare = currentTrain.fare * passengerCount

        // 3. Show Booking Confirmation Dialog before navigating to payment
        showBookingConfirmationDialog(currentTrain, passengerList, totalFare)
    }

    private fun showBookingConfirmationDialog(
        currentTrain: Train,
        passengerList: ArrayList<Passenger>,
        totalFare: Double
    ) {
        val passengerSummary = StringBuilder()
        passengerList.forEachIndexed { idx, p ->
            passengerSummary.append("${idx + 1}. ${p.passengerName} (${p.age} yrs, ${p.gender})\n")
        }

        val message = """
            🚆 Train: ${currentTrain.trainName} (#${currentTrain.trainNumber})
            📍 Route: ${currentTrain.source} ➔ ${currentTrain.destination}
            📅 Journey Date: $journeyDate
            ⏰ Departure: ${currentTrain.departureTime}
            
            👥 Passenger List (${passengerList.size}):
            ${passengerSummary.toString().trim()}
            
            💰 Total Amount: ₹${String.format("%.2f", totalFare)}
            
            Are you sure you want to confirm these passenger details and proceed to payment?
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Booking Details 🎫")
            .setMessage(message)
            .setPositiveButton("Confirm & Pay") { _, _ ->
                val intent = Intent(this, PaymentActivity::class.java).apply {
                    putExtra("EXTRA_TRAIN", currentTrain)
                    putExtra("EXTRA_JOURNEY_DATE", journeyDate)
                    putExtra("EXTRA_PASSENGER_LIST", passengerList)
                    putExtra("EXTRA_TOTAL_FARE", totalFare)
                }
                startActivity(intent)
            }
            .setNegativeButton("Review Again", null)
            .show()
    }
}
