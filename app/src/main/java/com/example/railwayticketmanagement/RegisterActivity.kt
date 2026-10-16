package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.User
import com.example.railwayticketmanagement.utils.DummyOtpHelper
import com.example.railwayticketmanagement.utils.InsetsUtil
import com.example.railwayticketmanagement.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var etRegName: TextInputEditText
    private lateinit var etRegEmail: TextInputEditText
    private lateinit var etRegMobile: TextInputEditText
    private lateinit var etRegPassword: TextInputEditText
    private lateinit var etRegConfirmPassword: TextInputEditText
    private lateinit var btnRegister: MaterialButton
    private lateinit var tvGoToLogin: TextView
    private lateinit var btnHeaderBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val headerView: View? = findViewById(R.id.headerLayout)
        InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE

        val tvTitle: TextView = findViewById(R.id.tvHeaderTitle)
        tvTitle.text = "User Registration"

        etRegName = findViewById(R.id.etRegName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegMobile = findViewById(R.id.etRegMobile)
        etRegPassword = findViewById(R.id.etRegPassword)
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)
    }

    private fun setupListeners() {
        btnHeaderBack.setOnClickListener { finish() }

        btnRegister.setOnClickListener {
            handleRegister()
        }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun handleRegister() {
        val name = etRegName.text.toString().trim()
        val email = etRegEmail.text.toString().trim()
        val mobile = etRegMobile.text.toString().trim()
        val password = etRegPassword.text.toString()
        val confirmPassword = etRegConfirmPassword.text.toString()

        if (name.isEmpty()) {
            etRegName.error = "Please enter your name"
            etRegName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            etRegEmail.error = "Please enter your email"
            etRegEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegEmail.error = "Please enter a valid email address"
            etRegEmail.requestFocus()
            return
        }

        if (mobile.isEmpty()) {
            etRegMobile.error = "Please enter your mobile number"
            etRegMobile.requestFocus()
            return
        }

        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            etRegMobile.error = "Mobile number must be exactly 10 digits"
            etRegMobile.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etRegPassword.error = "Please enter a password"
            etRegPassword.requestFocus()
            return
        }

        if (password.length < 4) {
            etRegPassword.error = "Password must be at least 4 characters"
            etRegPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            etRegConfirmPassword.error = "Passwords do not match"
            etRegConfirmPassword.requestFocus()
            return
        }

        if (dbHelper.checkEmailExists(email)) {
            etRegEmail.error = "Email is already registered. Please login."
            etRegEmail.requestFocus()
            return
        }

        // Show choice: Verify via OTP or Direct Register
        AlertDialog.Builder(this)
            .setTitle("Email Verification")
            .setMessage("Would you like to verify your email ($email) via 6-digit OTP?")
            .setPositiveButton("Verify via OTP 📨") { _, _ ->
                sendRegistrationOtp(name, email, mobile, password)
            }
            .setNeutralButton("Direct Register") { _, _ ->
                executeRegistration(name, email, mobile, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendRegistrationOtp(name: String, email: String, mobile: String, password: String) {
        showOtpVerificationDialog(name, email, mobile, password)
    }

    private fun showOtpVerificationDialog(
        name: String,
        email: String,
        mobile: String,
        password: String
    ) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_otp_verify, null)
        val tvNotice = dialogView.findViewById<TextView>(R.id.tvOtpDialogNotice)
        val etOtp = dialogView.findViewById<EditText>(R.id.etOtpDialogCode)
        val btnAutoFill = dialogView.findViewById<MaterialButton>(R.id.btnOtpDialogAutoFill)

        tvNotice.text = "A 6-digit OTP verification code has been dispatched for $email. Please enter it below:"

        var generatedCode = ""
        DummyOtpHelper.sendOtp(this, email, onAutoFill = { code ->
            etOtp.setText(code)
        }) { _, _, code ->
            generatedCode = code
            etOtp.setText(code)
        }

        btnAutoFill.setOnClickListener {
            val codeToUse = if (generatedCode.isNotEmpty()) generatedCode else DummyOtpHelper.getLastGeneratedOtp(email) ?: "123456"
            etOtp.setText(codeToUse)
            Toast.makeText(this, "OTP $codeToUse auto-filled!", Toast.LENGTH_SHORT).show()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Verify Email Address")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Verify & Register", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val verifyButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            verifyButton.setOnClickListener {
                val enteredOtp = etOtp.text.toString().trim()
                if (enteredOtp.length != 6) {
                    etOtp.error = "Please enter 6 digits"
                    return@setOnClickListener
                }

                DummyOtpHelper.verifyOtp(email, enteredOtp) { verified, verifyMsg ->
                    if (verified) {
                        dialog.dismiss()
                        Toast.makeText(this, "Email Verified Successfully!", Toast.LENGTH_SHORT).show()
                        executeRegistration(name, email, mobile, password)
                    } else {
                        Toast.makeText(this, verifyMsg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun executeRegistration(name: String, email: String, mobile: String, password: String) {
        val newUser = User(
            name = name,
            email = email,
            mobile = mobile,
            password = password
        )

        val insertedId = dbHelper.insertUser(newUser)
        if (insertedId > 0) {
            Toast.makeText(this, "Registration Successful! Auto-logging in...", Toast.LENGTH_SHORT).show()
            session.createLoginSession(insertedId, name, email, mobile)

            val intent = Intent(this, SearchTrainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to register user. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
