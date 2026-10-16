package com.example.railwayticketmanagement.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import com.example.railwayticketmanagement.R
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * DummyOtpHelper provides a self-contained, realistic dummy OTP module for
 * user authentication (Login, Register, and Forgot Password).
 *
 * Features:
 * 1. Generates 6-digit verification codes valid for 5 minutes.
 * 2. Emits an Android system status-bar notification with the code.
 * 3. Shows an interactive simulated SMS/Email popup with "Auto-Fill" and "Copy" actions.
 * 4. Master demo bypass code "123456" for 100% reliable viva and project presentations.
 * 5. Completely offline & self-contained — no external API/server keys or dependencies required.
 */
object DummyOtpHelper {

    private const val CHANNEL_ID = "railway_dummy_otp_channel"
    private const val CHANNEL_NAME = "Railway OTP Service"
    private const val OTP_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes

    // Stores email/phone -> Pair(6-digit-otp, expiry-time)
    private val otpStore = ConcurrentHashMap<String, Pair<String, Long>>()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Generate a new 6-digit OTP code and store it with 5-minute validity.
     */
    fun generateOtp(target: String): String {
        val cleanTarget = target.trim().lowercase()
        val code = Random.nextInt(100000, 999999).toString()
        val expiry = System.currentTimeMillis() + OTP_EXPIRY_MS
        otpStore[cleanTarget] = Pair(code, expiry)
        return code
    }

    /**
     * Retrieve the active OTP code for target, if valid.
     */
    fun getLastGeneratedOtp(target: String): String? {
        val cleanTarget = target.trim().lowercase()
        val record = otpStore[cleanTarget] ?: return null
        return if (System.currentTimeMillis() <= record.second) record.first else null
    }

    /**
     * Send dummy OTP for an email address or mobile number.
     * Generates a 6-digit code, triggers a system notification, and displays
     * the simulated SMS/Email popup on screen with 1-tap Auto-Fill.
     */
    fun sendOtp(
        activity: Activity,
        target: String,
        onAutoFill: ((otpCode: String) -> Unit)? = null,
        callback: (success: Boolean, message: String, generatedOtp: String) -> Unit
    ) {
        val cleanTarget = target.trim().lowercase()
        val otpCode = generateOtp(cleanTarget)

        // Show local Android notification
        showSystemNotification(activity, cleanTarget, otpCode)

        // Show interactive simulated SMS/Email popup dialog
        showOtpSimulatedDialog(activity, cleanTarget, otpCode, onAutoFill)

        mainHandler.post {
            callback(true, "OTP code sent successfully to $cleanTarget", otpCode)
        }
    }

    /**
     * Verify OTP entered by the user.
     * Returns true if:
     * 1. The code matches the generated code for this email.
     * 2. Or the code is the universal presentation bypass code "123456".
     */
    fun verifyOtp(
        target: String,
        enteredOtp: String,
        callback: (success: Boolean, message: String) -> Unit
    ) {
        val cleanTarget = target.trim().lowercase()
        val cleanOtp = enteredOtp.trim()

        if (cleanOtp.length != 6) {
            callback(false, "Please enter a valid 6-digit OTP code.")
            return
        }

        // Master bypass code for effortless viva demonstrations
        if (cleanOtp == "123456") {
            otpStore.remove(cleanTarget)
            callback(true, "OTP verified successfully!")
            return
        }

        val record = otpStore[cleanTarget]
        if (record == null) {
            // Check any active valid code
            val matchingEntry = otpStore.entries.firstOrNull {
                it.value.first == cleanOtp && System.currentTimeMillis() <= it.value.second
            }
            if (matchingEntry != null) {
                otpStore.remove(matchingEntry.key)
                callback(true, "OTP verified successfully!")
                return
            }
            callback(false, "No active OTP found. Please request a new OTP code.")
            return
        }

        val (savedOtp, expiry) = record
        if (System.currentTimeMillis() > expiry) {
            otpStore.remove(cleanTarget)
            callback(false, "OTP has expired. Please request a new one.")
            return
        }

        if (savedOtp == cleanOtp) {
            otpStore.remove(cleanTarget)
            callback(true, "OTP verified successfully!")
        } else {
            callback(false, "Invalid OTP code. Please check the code and try again.")
        }
    }

    /**
     * Display a realistic Simulated SMS/Email Notification Dialog
     */
    fun showOtpSimulatedDialog(
        activity: Activity,
        target: String,
        otpCode: String,
        onAutoFill: ((otpCode: String) -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) return

        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_dummy_otp_notification, null)
        val tvOtpTarget = dialogView.findViewById<TextView>(R.id.tvSimulatedOtpTarget)
        val tvOtpCode = dialogView.findViewById<TextView>(R.id.tvSimulatedOtpCode)
        val btnAutoFill = dialogView.findViewById<Button>(R.id.btnSimulatedOtpAutoFill)
        val btnCopy = dialogView.findViewById<Button>(R.id.btnSimulatedOtpCopy)
        val btnDismiss = dialogView.findViewById<Button>(R.id.btnSimulatedOtpDismiss)

        tvOtpTarget.text = "Recipient: $target"
        tvOtpCode.text = otpCode

        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnAutoFill.setOnClickListener {
            onAutoFill?.invoke(otpCode)
            Toast.makeText(activity, "OTP $otpCode auto-filled!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnCopy.setOnClickListener {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Railway OTP", otpCode)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(activity, "OTP $otpCode copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        btnDismiss.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Show status bar notification with the OTP code
     */
    private fun showSystemNotification(context: Context, target: String, otpCode: String) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Simulated Railway OTP notifications for presentation"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check_circle)
                .setContentTitle("Railway OTP Alert: $otpCode")
                .setContentText("Your verification code is $otpCode (valid for 5 mins)")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("IRCTC Verification: Your one-time verification code is $otpCode for $target. Valid for 5 minutes. Do not share with anyone.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(1001, builder.build())
        } catch (_: Exception) {}
    }
}
