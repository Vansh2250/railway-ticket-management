package com.example.railwayticketmanagement

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.railwayticketmanagement.models.User
import com.example.railwayticketmanagement.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var session: SessionManager

    private lateinit var etProfileName: TextInputEditText
    private lateinit var etProfileEmail: TextInputEditText
    private lateinit var etProfileMobile: TextInputEditText
    private lateinit var etProfilePassword: TextInputEditText
    private lateinit var btnUpdateProfile: MaterialButton
    private lateinit var btnProfileLogout: MaterialButton
    private lateinit var btnHeaderBack: ImageButton

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val headerView: View? = findViewById(R.id.headerLayout)
        com.example.railwayticketmanagement.utils.InsetsUtil.applyStatusBarPadding(headerView)

        dbHelper = DatabaseHelper(this)
        session = SessionManager(this)

        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Please log in to view your profile", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        initViews()
        loadUserProfile()
        setupListeners()
    }

    private fun initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack)
        btnHeaderBack.visibility = View.VISIBLE

        val tvTitle: TextView = findViewById(R.id.tvHeaderTitle)
        tvTitle.text = "My Profile"

        etProfileName = findViewById(R.id.etProfileName)
        etProfileEmail = findViewById(R.id.etProfileEmail)
        etProfileMobile = findViewById(R.id.etProfileMobile)
        etProfilePassword = findViewById(R.id.etProfilePassword)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnProfileLogout = findViewById(R.id.btnProfileLogout)
    }

    private fun loadUserProfile() {
        val userId = session.getUserId()
        currentUser = dbHelper.getUserById(userId)

        if (currentUser != null) {
            val user = currentUser!!
            etProfileName.setText(user.name)
            etProfileEmail.setText(user.email)
            etProfileMobile.setText(user.mobile)
        } else {
            // Fallback to session details
            etProfileName.setText(session.getUserName())
            etProfileEmail.setText(session.getUserEmail())
            etProfileMobile.setText(session.getUserMobile())
        }
    }

    private fun setupListeners() {
        btnHeaderBack.setOnClickListener {
            finish()
        }

        btnUpdateProfile.setOnClickListener {
            handleProfileUpdate()
        }

        btnProfileLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Are you sure you want to log out from your account?")
            .setPositiveButton("Yes, Logout") { _, _ ->
                session.logout()
                Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SearchTrainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun handleProfileUpdate() {
        val name = etProfileName.text.toString().trim()
        val mobile = etProfileMobile.text.toString().trim()
        val newPassword = etProfilePassword.text.toString()

        if (name.isEmpty()) {
            etProfileName.error = "Please enter your name"
            etProfileName.requestFocus()
            return
        }

        if (mobile.isEmpty()) {
            etProfileMobile.error = "Please enter your mobile number"
            etProfileMobile.requestFocus()
            return
        }

        if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
            etProfileMobile.error = "Mobile number must be exactly 10 digits"
            etProfileMobile.requestFocus()
            return
        }

        if (newPassword.isNotEmpty() && newPassword.length < 4) {
            etProfilePassword.error = "Password must be at least 4 characters"
            etProfilePassword.requestFocus()
            return
        }

        val userId = session.getUserId()
        val passwordToSave = if (newPassword.isNotBlank()) newPassword else null
        val updated = dbHelper.updateUser(userId, name, mobile, passwordToSave)

        if (updated) {
            session.updateProfile(name, mobile)
            Toast.makeText(this, "Profile Updated Successfully.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
}
