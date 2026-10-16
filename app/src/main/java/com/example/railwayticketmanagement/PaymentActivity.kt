package com.example.railwayticketmanagement

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.Booking
import com.example.railwayticketmanagement.models.Passenger
import com.example.railwayticketmanagement.models.Train
import com.example.railwayticketmanagement.utils.InsetsUtil
import com.example.railwayticketmanagement.utils.SessionManager
import com.google.android.material.button.MaterialButton

class PaymentActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private var train: Train? = null
    private var journeyDate: String = ""
    private var passengers: ArrayList<Passenger> = arrayListOf()
    private var totalFare: Double = 0.0

    private lateinit var tvPaymentTrainName: TextView
    private lateinit var tvPaymentJourneyDate: TextView
    private lateinit var tvPaymentPassengerCount: TextView
    private lateinit var tvPaymentTotalFare: TextView
    private lateinit var rgPaymentMethod: RadioGroup
    private lateinit var btnPayNow: MaterialButton
    private lateinit var btnHeaderBack: ImageButton

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        val headerView: View? = findViewById(R.id.headerLayout)
        InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Session expired. Please log in.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        extractIntentData()

        if (train == null || passengers.isEmpty()) {
            Toast.makeText(this, "Invalid booking information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        populateDetails()
        setupListeners()
    }

    @Suppress("DEPRECATION", "UNCHECKED_CAST")
    private fun extractIntentData() {
        train = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_TRAIN", Train::class.java)
        } else {
            intent.getSerializableExtra("EXTRA_TRAIN") as? Train
        }

        journeyDate = intent.getStringExtra("EXTRA_JOURNEY_DATE") ?: ""

        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_PASSENGER_LIST", ArrayList::class.java) as? ArrayList<Passenger>
        } else {
            intent.getSerializableExtra("EXTRA_PASSENGER_LIST") as? ArrayList<Passenger>
        }
        passengers = list ?: arrayListOf()

        totalFare = intent.getDoubleExtra("EXTRA_TOTAL_FARE", 0.0)
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE

        val tvTitle: TextView = findViewById(R.id.tvHeaderTitle)
        tvTitle.text = "Payment Gateway"

        tvPaymentTrainName = findViewById(R.id.tvPaymentTrainName)
        tvPaymentJourneyDate = findViewById(R.id.tvPaymentJourneyDate)
        tvPaymentPassengerCount = findViewById(R.id.tvPaymentPassengerCount)
        tvPaymentTotalFare = findViewById(R.id.tvPaymentTotalFare)
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod)
        btnPayNow = findViewById(R.id.btnPayNow)
    }

    private fun populateDetails() {
        val currentTrain = train!!
        tvPaymentTrainName.text = "${currentTrain.trainName} (#${currentTrain.trainNumber})"
        tvPaymentJourneyDate.text = journeyDate
        tvPaymentPassengerCount.text = "${passengers.size} Passenger(s)"
        tvPaymentTotalFare.text = "₹${String.format("%.2f", totalFare)}"
        btnPayNow.text = "Proceed to Pay ₹${String.format("%.2f", totalFare)}"
    }

    private fun setupListeners() {
        btnHeaderBack.setOnClickListener {
            finish()
        }

        btnPayNow.setOnClickListener {
            dispatchSelectedPaymentModule()
        }
    }

    /**
     * Opens the appropriate option-wise payment module based on user selection.
     */
    private fun dispatchSelectedPaymentModule() {
        when (rgPaymentMethod.checkedRadioButtonId) {
            R.id.rbUPI -> openUpiPaymentModal()
            R.id.rbCard -> openCardPaymentModal()
            R.id.rbNetBanking -> openNetBankingModal()
            R.id.rbWallet -> openWalletPaymentModal()
            R.id.rbCash -> openCashCounterModal()
            else -> openUpiPaymentModal()
        }
    }

    // =========================================================================
    // 1. UPI PAYMENT MODULE (Google Pay, PhonePe, Paytm, BHIM, UPI ID / QR)
    // =========================================================================
    private fun openUpiPaymentModal() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 25, 50, 15)
        }

        val tvAmount = TextView(this).apply {
            text = "Total Payable: ₹${String.format("%.2f", totalFare)}"
            textSize = 17f
            setTextColor(resources.getColor(R.color.accent_green, theme))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 15)
        }
        layout.addView(tvAmount)

        val tvInstruction = TextView(this).apply {
            text = "Select a simulated UPI App or enter your UPI VPA ID:"
            textSize = 13f
            setTextColor(resources.getColor(R.color.text_secondary, theme))
            setPadding(0, 0, 0, 10)
        }
        layout.addView(tvInstruction)

        // Radio group for UPI Apps
        val upiRadioGroup = RadioGroup(this)
        val rbGPay = RadioButton(this).apply { text = "Google Pay (GPay)"; isChecked = true; id = View.generateViewId() }
        val rbPhonePe = RadioButton(this).apply { text = "PhonePe UPI"; id = View.generateViewId() }
        val rbPaytm = RadioButton(this).apply { text = "Paytm UPI"; id = View.generateViewId() }
        val rbBhim = RadioButton(this).apply { text = "BHIM UPI / Other VPA"; id = View.generateViewId() }

        upiRadioGroup.addView(rbGPay)
        upiRadioGroup.addView(rbPhonePe)
        upiRadioGroup.addView(rbPaytm)
        upiRadioGroup.addView(rbBhim)
        layout.addView(upiRadioGroup)

        // Custom UPI ID input
        val etUpiId = EditText(this).apply {
            val phone = session.getUserMobile().filter { it.isDigit() }.ifBlank { "9876543210" }
            hint = "e.g. $phone@upi"
            setText("$phone@oksbi")
            setSingleLine()
            setPadding(20, 20, 20, 20)
            setBackgroundResource(android.R.drawable.edit_text)
        }
        layout.addView(TextView(this).apply {
            text = "UPI Virtual Payment Address (VPA):"
            textSize = 12f
            setTextColor(resources.getColor(R.color.primary_dark, theme))
            setPadding(0, 15, 0, 5)
        })
        layout.addView(etUpiId)

        AlertDialog.Builder(this)
            .setTitle("📱 UPI Payment Simulation")
            .setView(layout)
            .setPositiveButton("Authorize & Pay") { _, _ ->
                val selectedApp = when (upiRadioGroup.checkedRadioButtonId) {
                    rbGPay.id -> "Google Pay"
                    rbPhonePe.id -> "PhonePe"
                    rbPaytm.id -> "Paytm"
                    else -> "BHIM UPI"
                }
                val upiId = etUpiId.text.toString().trim().ifBlank { "passenger@upi" }
                simulatePaymentProcessing("UPI ($selectedApp: $upiId)")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // 2. DEBIT / CREDIT CARD MODULE (Card Entry + Simulated 3D-Secure Bank OTP)
    // =========================================================================
    private fun openCardPaymentModal() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 25, 50, 15)
        }

        val tvAmount = TextView(this).apply {
            text = "Payable Amount: ₹${String.format("%.2f", totalFare)}"
            textSize = 17f
            setTextColor(resources.getColor(R.color.accent_green, theme))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 15)
        }
        layout.addView(tvAmount)

        val etCardNumber = EditText(this).apply {
            hint = "4532 8901 2345 6789 (16 digits)"
            filters = arrayOf(InputFilter.LengthFilter(19))
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val etCardHolder = EditText(this).apply {
            hint = "Cardholder Name"
            setText(session.getUserName().ifBlank { "Rahul Sharma" })
            setSingleLine()
        }

        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 10, 0, 10)
        }

        val etExpiry = EditText(this).apply {
            hint = "MM/YY"
            filters = arrayOf(InputFilter.LengthFilter(5))
            setSingleLine()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val etCvv = EditText(this).apply {
            hint = "CVV (3 digits)"
            filters = arrayOf(InputFilter.LengthFilter(3))
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        rowLayout.addView(etExpiry)
        rowLayout.addView(etCvv)

        val btnAutoFill = Button(this).apply {
            text = "⚡ Quick Demo Card Auto-Fill"
            setBackgroundColor(resources.getColor(R.color.primary, theme))
            setTextColor(resources.getColor(R.color.white, theme))
            textSize = 12f
            setOnClickListener {
                etCardNumber.setText("4532 9812 6743 1290")
                etExpiry.setText("08/29")
                etCvv.setText("789")
            }
        }

        layout.addView(TextView(this).apply { text = "Card Number:"; textSize = 12f; setTextColor(resources.getColor(R.color.primary_dark, theme)) })
        layout.addView(etCardNumber)
        layout.addView(TextView(this).apply { text = "Name on Card:"; textSize = 12f; setTextColor(resources.getColor(R.color.primary_dark, theme)) })
        layout.addView(etCardHolder)
        layout.addView(TextView(this).apply { text = "Expiry Date & CVV:"; textSize = 12f; setTextColor(resources.getColor(R.color.primary_dark, theme)) })
        layout.addView(rowLayout)
        layout.addView(btnAutoFill)

        AlertDialog.Builder(this)
            .setTitle("💳 Card Payment Simulation")
            .setView(layout)
            .setPositiveButton("Proceed to Bank OTP") { _, _ ->
                val cardRaw = etCardNumber.text.toString().trim()
                val last4 = if (cardRaw.length >= 4) cardRaw.takeLast(4) else "1290"
                showBankOtpVerificationDialog("Card (Ending in $last4)")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Simulated Bank 3D-Secure OTP Dialog for Card Payment.
     */
    private fun showBankOtpVerificationDialog(methodPrefix: String) {
        val demoOtp = (100000..999999).random().toString()
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 25, 50, 15)
        }

        val tvInfo = TextView(this).apply {
            text = "A simulated 3D-Secure Bank OTP has been generated for ₹${String.format("%.2f", totalFare)}.\n\nYour Bank OTP is:\n👉  $demoOtp  👈"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary_dark, theme))
            setPadding(0, 0, 0, 15)
        }
        layout.addView(tvInfo)

        val etOtp = EditText(this).apply {
            hint = "Enter 6-digit Bank OTP"
            filters = arrayOf(InputFilter.LengthFilter(6))
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(demoOtp) // pre-filled for convenience in viva
        }
        layout.addView(etOtp)

        AlertDialog.Builder(this)
            .setTitle("🛡️ Bank 3D-Secure Authentication")
            .setView(layout)
            .setPositiveButton("Authenticate & Pay") { _, _ ->
                val entered = etOtp.text.toString().trim()
                if (entered == demoOtp || entered.length == 6) {
                    simulatePaymentProcessing("$methodPrefix - Auth Code: $demoOtp")
                } else {
                    Toast.makeText(this, "Invalid Bank OTP", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // 3. NET BANKING MODULE (Bank Selection + Simulated Bank Portal Auth)
    // =========================================================================
    private fun openNetBankingModal() {
        val banks = arrayOf(
            "State Bank of India (SBI)",
            "HDFC Bank",
            "ICICI Bank",
            "Axis Bank",
            "Punjab National Bank (PNB)",
            "Bank of Baroda"
        )
        var selectedBankIndex = 0

        AlertDialog.Builder(this)
            .setTitle("🏛️ Net Banking Simulation")
            .setSingleChoiceItems(banks, selectedBankIndex) { _, which ->
                selectedBankIndex = which
            }
            .setPositiveButton("Proceed to Bank Login") { _, _ ->
                val chosenBank = banks[selectedBankIndex]
                showBankPortalLoginDialog(chosenBank)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showBankPortalLoginDialog(bankName: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 25, 50, 15)
        }

        val tvInfo = TextView(this).apply {
            text = "Welcome to $bankName Online Banking\nAmount to Debit: ₹${String.format("%.2f", totalFare)}"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary_dark, theme))
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 15)
        }
        layout.addView(tvInfo)

        val etUser = EditText(this).apply {
            hint = "Net Banking User ID"
            setText("railway_user_${session.getUserId()}")
            setSingleLine()
        }
        val etPass = EditText(this).apply {
            hint = "Net Banking Password"
            setText("demo1234")
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine()
        }

        layout.addView(TextView(this).apply { text = "Customer ID:"; textSize = 12f })
        layout.addView(etUser)
        layout.addView(TextView(this).apply { text = "Password:"; textSize = 12f })
        layout.addView(etPass)

        AlertDialog.Builder(this)
            .setTitle("$bankName Portal")
            .setView(layout)
            .setPositiveButton("Authorize Payment") { _, _ ->
                simulatePaymentProcessing("Net Banking ($bankName)")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // 4. RAILWAY E-WALLET MODULE (Balance Deduction Simulation)
    // =========================================================================
    private fun openWalletPaymentModal() {
        val demoBalance = 5000.0
        val remaining = demoBalance - totalFare

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 25, 50, 15)
        }

        val tvBalance = TextView(this).apply {
            text = "👛 Railway e-Wallet Balance: ₹${String.format("%.2f", demoBalance)}\n" +
                    "Ticket Amount: ₹${String.format("%.2f", totalFare)}\n" +
                    "Remaining Balance: ₹${String.format("%.2f", remaining)}"
            textSize = 14f
            setTextColor(resources.getColor(R.color.primary_dark, theme))
            setLineSpacing(6f, 1f)
            setPadding(0, 0, 0, 15)
        }
        layout.addView(tvBalance)

        AlertDialog.Builder(this)
            .setTitle("👛 Railway e-Wallet Instant Debit")
            .setView(layout)
            .setPositiveButton("Pay Instantly from Wallet") { _, _ ->
                simulatePaymentProcessing("Railway e-Wallet")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // =========================================================================
    // 5. RAILWAY COUNTER CASH BOOKING MODULE
    // =========================================================================
    private fun openCashCounterModal() {
        val voucherNumber = (10000000..99999999).random().toString()

        AlertDialog.Builder(this)
            .setTitle("💵 Railway PRS Station Counter")
            .setMessage(
                "A Cash Reservation Voucher (#$voucherNumber) will be issued.\n\n" +
                "Amount Payable: ₹${String.format("%.2f", totalFare)}\n\n" +
                "Your seat reservation will be confirmed and held. You can present this voucher at any Computerized Railway Reservation Counter to pay in cash."
            )
            .setPositiveButton("Confirm Cash Booking") { _, _ ->
                simulatePaymentProcessing("PRS Counter Cash (Voucher #$voucherNumber)")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Shows a professional 1.2-second simulated authorization spinner,
     * then commits the transaction to SQLite.
     */
    @Suppress("DEPRECATION")
    private fun simulatePaymentProcessing(paymentMethodDescription: String) {
        val progressDialog = ProgressDialog(this).apply {
            setTitle("Processing Transaction")
            setMessage("Securing transaction with Indian Railways Payment Gateway...\nAuthenticating payment...")
            setCancelable(false)
            show()
        }

        mainHandler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                progressDialog.dismiss()
                finalizeBooking(paymentMethodDescription)
            }
        }, 1200)
    }

    /**
     * Finalizes booking in local SQLite database inside a safe transaction,
     * deducts available train seats, and opens BookingConfirmationActivity.
     */
    private fun finalizeBooking(paymentMethod: String) {
        val currentTrain = train ?: return
        val currentUserId = session.getUserId()

        if (currentUserId == -1L) {
            Toast.makeText(this, "User session not found. Please log in again.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val booking = Booking(
            userId = currentUserId,
            trainId = currentTrain.trainId,
            journeyDate = journeyDate,
            passengerCount = passengers.size,
            totalFare = totalFare,
            bookingStatus = "CONFIRMED",
            bookingDate = ""
        )

        val bookingId = dbHelper.createBooking(booking, passengers, paymentMethod)

        if (bookingId > 0) {
            Toast.makeText(this, "Payment Approved! Ticket Booked Successfully.", Toast.LENGTH_SHORT).show()

            val confirmationIntent = Intent(this, BookingConfirmationActivity::class.java).apply {
                putExtra("EXTRA_BOOKING_ID", bookingId)
            }
            startActivity(confirmationIntent)
            finish()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Booking Failed")
                .setMessage("Not enough seats available on ${currentTrain.trainName}. Please select another train or fewer passengers.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}
