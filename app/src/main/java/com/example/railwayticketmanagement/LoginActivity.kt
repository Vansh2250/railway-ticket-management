package com.example.railwayticketmanagement

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.User
import com.example.railwayticketmanagement.utils.InsetsUtil
import com.example.railwayticketmanagement.utils.SessionManager
import com.example.railwayticketmanagement.utils.SupabaseAuthHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var btnHeaderBack: ImageButton
    private lateinit var btnConfigureSupabase: TextView

    // Mode Switcher Tabs
    private lateinit var tabPasswordLogin: Button
    private lateinit var tabOtpLogin: Button
    private lateinit var layoutPasswordLogin: LinearLayout
    private lateinit var layoutOtpLogin: LinearLayout

    // Password Mode Views
    private lateinit var etLoginEmail: TextInputEditText
    private lateinit var etLoginPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvForgotPassword: TextView
    private lateinit var btnAutoFillDemo: Button

    // OTP Mode Views
    private lateinit var layoutOtpStepEmail: LinearLayout
    private lateinit var layoutOtpStepVerify: LinearLayout
    private lateinit var etOtpEmail: TextInputEditText
    private lateinit var btnSendOtpLogin: MaterialButton
    private lateinit var tvOtpSentNotice: TextView
    private lateinit var etLoginOtpCode: TextInputEditText
    private lateinit var btnOpenMailAppLogin: MaterialButton
    private lateinit var btnVerifyOtpLogin: MaterialButton
    private lateinit var tvResendOtpLogin: TextView
    private lateinit var btnChangeOtpEmail: TextView

    private lateinit var tvGoToRegister: TextView

    private var isOtpMode = false
    private var pendingOtpEmail = ""
    private var resendTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val headerView: View? = findViewById(R.id.headerLayout)
        InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        initViews()
        setupListeners()
        updateSupabaseBadge()

        val prefillEmail = intent.getStringExtra("PREFILL_EMAIL")
        if (!prefillEmail.isNullOrBlank()) {
            etLoginEmail.setText(prefillEmail)
            etOtpEmail.setText(prefillEmail)
            etLoginPassword.requestFocus()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE

        val tvTitle: TextView = findViewById(R.id.tvHeaderTitle)
        tvTitle.text = "Account Login"

        btnConfigureSupabase = findViewById(R.id.btnConfigureSupabase)

        tabPasswordLogin = findViewById(R.id.tabPasswordLogin)
        tabOtpLogin = findViewById(R.id.tabOtpLogin)
        layoutPasswordLogin = findViewById(R.id.layoutPasswordLogin)
        layoutOtpLogin = findViewById(R.id.layoutOtpLogin)

        // Password Mode
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        btnAutoFillDemo = findViewById(R.id.btnAutoFillDemo)

        // OTP Mode
        layoutOtpStepEmail = findViewById(R.id.layoutOtpStepEmail)
        layoutOtpStepVerify = findViewById(R.id.layoutOtpStepVerify)
        etOtpEmail = findViewById(R.id.etOtpEmail)
        btnSendOtpLogin = findViewById(R.id.btnSendOtpLogin)
        tvOtpSentNotice = findViewById(R.id.tvOtpSentNotice)
        etLoginOtpCode = findViewById(R.id.etLoginOtpCode)
        btnOpenMailAppLogin = findViewById(R.id.btnOpenMailAppLogin)
        btnVerifyOtpLogin = findViewById(R.id.btnVerifyOtpLogin)
        tvResendOtpLogin = findViewById(R.id.tvResendOtpLogin)
        btnChangeOtpEmail = findViewById(R.id.btnChangeOtpEmail)

        tvGoToRegister = findViewById(R.id.tvGoToRegister)
    }

    private fun updateSupabaseBadge() {
        if (SupabaseAuthHelper.isConfigured(this)) {
            btnConfigureSupabase.text = "🟢 Supabase Connected (Tap to change keys)"
            btnConfigureSupabase.setTextColor(resources.getColor(R.color.accent_green, theme))
        } else {
            btnConfigureSupabase.text = "⚙️ Configure Supabase API Keys"
            btnConfigureSupabase.setTextColor(resources.getColor(R.color.primary, theme))
        }
    }

    private fun setupListeners() {
        btnHeaderBack.setOnClickListener { finish() }

        btnConfigureSupabase.setOnClickListener {
            SupabaseAuthHelper.showConfigDialog(this) {
                updateSupabaseBadge()
            }
        }

        tabPasswordLogin.setOnClickListener { switchToPasswordMode() }
        tabOtpLogin.setOnClickListener { switchToOtpMode() }

        btnLogin.setOnClickListener { handlePasswordLogin() }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        btnAutoFillDemo.setOnClickListener {
            if (isOtpMode) {
                etOtpEmail.setText("rahul@example.com")
            } else {
                etLoginEmail.setText("rahul@example.com")
                etLoginPassword.setText("password123")
            }
        }

        btnSendOtpLogin.setOnClickListener { handleSendOtp() }

        btnOpenMailAppLogin.setOnClickListener {
            SupabaseAuthHelper.openEmailApp(this)
        }

        btnVerifyOtpLogin.setOnClickListener { handleVerifyOtp() }

        btnChangeOtpEmail.setOnClickListener {
            layoutOtpStepVerify.visibility = View.GONE
            layoutOtpStepEmail.visibility = View.VISIBLE
            resendTimer?.cancel()
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun switchToPasswordMode() {
        isOtpMode = false
        tabPasswordLogin.setBackgroundResource(R.drawable.bg_tab_selected)
        tabPasswordLogin.setTextColor(resources.getColor(R.color.white, theme))

        tabOtpLogin.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabOtpLogin.setTextColor(resources.getColor(R.color.text_secondary, theme))

        layoutPasswordLogin.visibility = View.VISIBLE
        layoutOtpLogin.visibility = View.GONE
    }

    private fun switchToOtpMode() {
        isOtpMode = true
        tabOtpLogin.setBackgroundResource(R.drawable.bg_tab_selected)
        tabOtpLogin.setTextColor(resources.getColor(R.color.white, theme))

        tabPasswordLogin.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        tabPasswordLogin.setTextColor(resources.getColor(R.color.text_secondary, theme))

        layoutPasswordLogin.visibility = View.GONE
        layoutOtpLogin.visibility = View.VISIBLE

        val currentEmail = etLoginEmail.text.toString().trim()
        if (currentEmail.isNotEmpty() && etOtpEmail.text.isNullOrEmpty()) {
            etOtpEmail.setText(currentEmail)
        }
    }

    private fun handlePasswordLogin() {
        val email = etLoginEmail.text.toString().trim()
        val password = etLoginPassword.text.toString()

        if (email.isEmpty()) {
            etLoginEmail.error = "Please enter your email"
            etLoginEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etLoginEmail.error = "Please enter a valid email address"
            etLoginEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etLoginPassword.error = "Please enter your password"
            etLoginPassword.requestFocus()
            return
        }

        val user = dbHelper.checkLogin(email, password)
        if (user != null) {
            session.createLoginSession(user.id, user.name, user.email, user.mobile)
            Toast.makeText(this, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, SearchTrainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSendOtp() {
        val email = etOtpEmail.text.toString().trim()

        if (email.isEmpty()) {
            etOtpEmail.error = "Please enter your email"
            etOtpEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etOtpEmail.error = "Please enter a valid email address"
            etOtpEmail.requestFocus()
            return
        }

        pendingOtpEmail = email

        val progress = ProgressDialog(this).apply {
            setMessage("Sending OTP from Supabase...")
            setCancelable(false)
            show()
        }

        btnSendOtpLogin.isEnabled = false

        SupabaseAuthHelper.sendOtp(this, email, false) { success, message, _ ->
            progress.dismiss()
            btnSendOtpLogin.isEnabled = true

            if (success) {
                layoutOtpStepEmail.visibility = View.GONE
                layoutOtpStepVerify.visibility = View.VISIBLE
                tvOtpSentNotice.text = "Enter 6-digit OTP code sent to: $email"

                AlertDialog.Builder(this)
                    .setTitle("Check Your Gmail App 📨")
                    .setMessage("A 6-digit verification code has been dispatched from your Supabase server to $email.\n\nPlease open your Gmail app (and check the Spam/Junk folder if needed) to copy the code.")
                    .setPositiveButton("Open Gmail App") { _, _ ->
                        SupabaseAuthHelper.openEmailApp(this)
                    }
                    .setNegativeButton("OK", null)
                    .show()

                startResendTimer()
            } else {
                if (!SupabaseAuthHelper.isConfigured(this)) {
                    AlertDialog.Builder(this)
                        .setTitle("Supabase Credentials Required ⚙️")
                        .setMessage("To send real OTP emails from your Supabase project account to your Gmail, please configure your Supabase Project URL and Anon Key.")
                        .setPositiveButton("Configure Now") { _, _ ->
                            SupabaseAuthHelper.showConfigDialog(this) {
                                updateSupabaseBadge()
                            }
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
        val otpCode = etLoginOtpCode.text.toString().trim()
        if (otpCode.length != 6) {
            etLoginOtpCode.error = "Please enter the 6-digit OTP code"
            etLoginOtpCode.requestFocus()
            return
        }

        val progress = ProgressDialog(this).apply {
            setMessage("Verifying OTP with Supabase...")
            setCancelable(false)
            show()
        }

        btnVerifyOtpLogin.isEnabled = false

        SupabaseAuthHelper.verifyOtp(this, pendingOtpEmail, otpCode) { success, message, _ ->
            progress.dismiss()
            btnVerifyOtpLogin.isEnabled = true

            if (success) {
                resendTimer?.cancel()
                Toast.makeText(this, "OTP Verified Successfully!", Toast.LENGTH_SHORT).show()

                // Check if user exists in local database
                var user = dbHelper.getUserByEmail(pendingOtpEmail)
                if (user == null) {
                    // Auto-register verified user
                    val defaultName = pendingOtpEmail.substringBefore("@")
                        .replaceFirstChar { it.uppercase() }
                    val newUser = User(
                        name = defaultName,
                        email = pendingOtpEmail,
                        mobile = "9876543210",
                        password = "otp_verified"
                    )
                    val newId = dbHelper.insertUser(newUser)
                    user = User(newId, defaultName, pendingOtpEmail, "9876543210")
                }

                session.createLoginSession(user.id, user.name, user.email, user.mobile)
                Toast.makeText(this, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, SearchTrainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startResendTimer() {
        resendTimer?.cancel()
        tvResendOtpLogin.isEnabled = false

        resendTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendOtpLogin.text = "Resend OTP in ${seconds}s"
            }

            override fun onFinish() {
                tvResendOtpLogin.text = "Resend OTP"
                tvResendOtpLogin.isEnabled = true
                tvResendOtpLogin.setOnClickListener {
                    handleSendOtp()
                }
            }
        }.start()
    }
}
