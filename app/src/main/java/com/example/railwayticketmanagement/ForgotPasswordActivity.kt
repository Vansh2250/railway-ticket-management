package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.utils.InsetsUtil
import com.example.railwayticketmanagement.utils.SupabaseAuthHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    private var headerView: View? = null
    private lateinit var btnHeaderBack: ImageButton
    private lateinit var btnConfigureSupabase: TextView

    // Steps containers
    private lateinit var layoutStepEmail: LinearLayout
    private lateinit var layoutStepOtp: LinearLayout
    private lateinit var layoutStepNewPassword: LinearLayout

    // Step 1: Email
    private lateinit var etForgotEmail: TextInputEditText
    private lateinit var btnSendOtp: MaterialButton

    // Step 2: OTP
    private lateinit var tvOtpInstruction: TextView
    private lateinit var etForgotOtp: TextInputEditText
    private lateinit var btnOpenMailApp: MaterialButton
    private lateinit var btnVerifyOtp: MaterialButton
    private lateinit var tvResendOtp: TextView

    // Step 3: New Password
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var etConfirmNewPassword: TextInputEditText
    private lateinit var btnResetPassword: MaterialButton

    private lateinit var tvBackToLogin: TextView

    private var targetEmail: String = ""
    private var supabaseAccessToken: String? = null
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        dbHelper = DatabaseHelper(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        headerView = findViewById(R.id.headerLayout)
        InsetsUtil.applyStatusBarPadding(headerView)

        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE
        btnHeaderBack.setOnClickListener { finish() }

        val tvTitle: TextView = findViewById(R.id.tvHeaderTitle)
        tvTitle.text = "Password Recovery"

        layoutStepEmail = findViewById(R.id.layoutStepEmail)
        layoutStepOtp = findViewById(R.id.layoutStepOtp)
        layoutStepNewPassword = findViewById(R.id.layoutStepNewPassword)

        etForgotEmail = findViewById(R.id.etForgotEmail)
        btnSendOtp = findViewById(R.id.btnSendOtp)

        tvOtpInstruction = findViewById(R.id.tvOtpInstruction)
        etForgotOtp = findViewById(R.id.etForgotOtp)
        btnOpenMailApp = findViewById(R.id.btnOpenMailApp)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)
        tvResendOtp = findViewById(R.id.tvResendOtp)

        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnResetPassword = findViewById(R.id.btnResetPassword)

        btnConfigureSupabase = findViewById(R.id.btnConfigureSupabase)
        updateSupabaseButtonBadge()

        tvBackToLogin = findViewById(R.id.tvBackToLogin)
    }

    private fun updateSupabaseButtonBadge() {
        if (SupabaseAuthHelper.isConfigured(this)) {
            btnConfigureSupabase.text = "🟢 Supabase Connected (Tap to change keys)"
            btnConfigureSupabase.setTextColor(resources.getColor(R.color.accent_green, theme))
        } else {
            btnConfigureSupabase.text = "⚙️ Configure Supabase API Keys (For real email OTP)"
            btnConfigureSupabase.setTextColor(resources.getColor(R.color.primary, theme))
        }
    }

    private fun showSupabaseConfigDialog() {
        SupabaseAuthHelper.showConfigDialog(this) {
            updateSupabaseButtonBadge()
        }
    }

    private fun setupListeners() {
        btnOpenMailApp.setOnClickListener {
            SupabaseAuthHelper.openEmailApp(this)
        }

        btnConfigureSupabase.setOnClickListener {
            showSupabaseConfigDialog()
        }

        btnSendOtp.setOnClickListener {
            handleSendOtp()
        }

        btnVerifyOtp.setOnClickListener {
            handleVerifyOtp()
        }

        tvResendOtp.setOnClickListener {
            handleResendOtp()
        }

        btnResetPassword.setOnClickListener {
            handleResetPassword()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun handleSendOtp() {
        val email = etForgotEmail.text.toString().trim()

        if (email.isEmpty()) {
            etForgotEmail.error = "Please enter your registered email"
            etForgotEmail.requestFocus()
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etForgotEmail.error = "Please enter a valid email address"
            etForgotEmail.requestFocus()
            return
        }

        // Verify if account exists in RailwayDB
        val exists = dbHelper.checkEmailExists(email)
        if (!exists) {
            AlertDialog.Builder(this)
                .setTitle("Account Not Found")
                .setMessage("No account was found with the email address '$email'. Please verify your email or register a new account.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        targetEmail = email
        btnSendOtp.isEnabled = false
        btnSendOtp.text = "Sending OTP..."

        SupabaseAuthHelper.sendOtp(this, email) { success, message, generatedOtp ->
            btnSendOtp.isEnabled = true
            btnSendOtp.text = "Send Supabase OTP"

            if (success) {
                layoutStepEmail.visibility = View.GONE
                layoutStepOtp.visibility = View.VISIBLE
                layoutStepNewPassword.visibility = View.GONE

                tvOtpInstruction.text = "Enter the 6-digit OTP sent to:\n$email"
                startResendTimer()

                AlertDialog.Builder(this)
                    .setTitle("Check Your Gmail App 📨")
                    .setMessage("A 6-digit verification code has been dispatched from your Supabase server to $email.\n\nPlease open your Gmail app (and check the Spam/Junk folder if needed) to copy the code.")
                    .setPositiveButton("Open Gmail App") { _, _ ->
                        SupabaseAuthHelper.openEmailApp(this)
                    }
                    .setNegativeButton("OK", null)
                    .show()
            } else {
                if (!SupabaseAuthHelper.isConfigured(this)) {
                    AlertDialog.Builder(this)
                        .setTitle("Supabase Credentials Required ⚙️")
                        .setMessage("To send real OTP emails from your Supabase project account to your Gmail, please configure your Supabase Project URL and Anon Key.")
                        .setPositiveButton("Configure Now") { _, _ ->
                            showSupabaseConfigDialog()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Supabase Notice")
                        .setMessage(message)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
    }

    private fun handleVerifyOtp() {
        val otp = etForgotOtp.text.toString().trim()

        if (otp.isEmpty() || otp.length != 6) {
            etForgotOtp.error = "Please enter a valid 6-digit OTP code"
            etForgotOtp.requestFocus()
            return
        }

        btnVerifyOtp.isEnabled = false
        btnVerifyOtp.text = "Verifying..."

        SupabaseAuthHelper.verifyOtp(this, targetEmail, otp) { success, message, accessToken ->
            btnVerifyOtp.isEnabled = true
            btnVerifyOtp.text = "Verify OTP Code"

            if (success) {
                resendTimer?.cancel()
                supabaseAccessToken = accessToken

                layoutStepOtp.visibility = View.GONE
                layoutStepNewPassword.visibility = View.VISIBLE

                Toast.makeText(this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Verification Failed")
                    .setMessage(message)
                    .setPositiveButton("Try Again", null)
                    .show()
            }
        }
    }

    private fun handleResendOtp() {
        SupabaseAuthHelper.sendOtp(this, targetEmail) { success, message, generatedOtp ->
            if (success) {
                startResendTimer()
                if (generatedOtp != null) {
                    AlertDialog.Builder(this)
                        .setTitle("New Supabase OTP")
                        .setMessage("Your new 6-digit Verification Code is:\n\n👉  $generatedOtp  👈")
                        .setPositiveButton("Auto-fill") { _, _ ->
                            etForgotOtp.setText(generatedOtp)
                        }
                        .setNegativeButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleResetPassword() {
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmNewPassword.text.toString().trim()

        if (newPassword.isEmpty()) {
            etNewPassword.error = "Please enter a new password"
            etNewPassword.requestFocus()
            return
        }

        if (newPassword.length < 4) {
            etNewPassword.error = "Password must be at least 4 characters"
            etNewPassword.requestFocus()
            return
        }

        if (confirmPassword.isEmpty()) {
            etConfirmNewPassword.error = "Please confirm your new password"
            etConfirmNewPassword.requestFocus()
            return
        }

        if (newPassword != confirmPassword) {
            etConfirmNewPassword.error = "Passwords do not match"
            etConfirmNewPassword.requestFocus()
            return
        }

        // 1. Update in local SQLite DB
        val updatedLocal = dbHelper.updatePasswordByEmail(targetEmail, newPassword)

        if (updatedLocal) {
            // 2. Synchronize with Supabase Auth
            SupabaseAuthHelper.updatePasswordOnSupabase(supabaseAccessToken, newPassword) { _, _ -> }

            // 3. Show Password Reset Confirmation Dialog
            AlertDialog.Builder(this)
                .setTitle("Password Reset Confirmed! ✅")
                .setMessage("Your account password for $targetEmail has been successfully changed.\n\nYou can now log in using your new password.")
                .setCancelable(false)
                .setPositiveButton("Proceed to Login") { _, _ ->
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("PREFILL_EMAIL", targetEmail)
                    }
                    startActivity(intent)
                    finish()
                }
                .show()
        } else {
            Toast.makeText(this, "Failed to update password. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startResendTimer() {
        tvResendOtp.isEnabled = false
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvResendOtp.text = "Resend OTP in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                tvResendOtp.isEnabled = true
                tvResendOtp.text = "Didn't get OTP? Resend OTP"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}
