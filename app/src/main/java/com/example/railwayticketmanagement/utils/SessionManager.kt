package com.example.railwayticketmanagement.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "RailwayAppSession"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_MOBILE = "userMobile"

        private const val KEY_PENDING_TRAIN_ID = "pendingTrainId"
        private const val KEY_PENDING_JOURNEY_DATE = "pendingJourneyDate"
    }

    fun setLoginSession(userId: Long, name: String, email: String, mobile: String) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putLong(KEY_USER_ID, userId)
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_EMAIL, email)
        editor.putString(KEY_USER_MOBILE, mobile)
        editor.apply()
    }

    fun createLoginSession(userId: Long, name: String, email: String, mobile: String) {
        setLoginSession(userId, name, email, mobile)
    }

    fun updateProfile(name: String, mobile: String) {
        val editor = prefs.edit()
        editor.putString(KEY_USER_NAME, name)
        editor.putString(KEY_USER_MOBILE, mobile)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUserId(): Long {
        return prefs.getLong(KEY_USER_ID, -1L)
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    fun getUserEmail(): String {
        return prefs.getString(KEY_USER_EMAIL, "") ?: ""
    }

    fun getUserMobile(): String {
        return prefs.getString(KEY_USER_MOBILE, "") ?: ""
    }

    fun savePendingBooking(trainId: Long, journeyDate: String) {
        val editor = prefs.edit()
        editor.putLong(KEY_PENDING_TRAIN_ID, trainId)
        editor.putString(KEY_PENDING_JOURNEY_DATE, journeyDate)
        editor.apply()
    }

    fun hasPendingBooking(): Boolean {
        return prefs.getLong(KEY_PENDING_TRAIN_ID, -1L) != -1L
    }

    fun getPendingTrainId(): Long {
        return prefs.getLong(KEY_PENDING_TRAIN_ID, -1L)
    }

    fun getPendingJourneyDate(): String {
        return prefs.getString(KEY_PENDING_JOURNEY_DATE, "") ?: ""
    }

    fun clearPendingBooking() {
        val editor = prefs.edit()
        editor.remove(KEY_PENDING_TRAIN_ID)
        editor.remove(KEY_PENDING_JOURNEY_DATE)
        editor.apply()
    }

    fun logout() {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_NAME)
        editor.remove(KEY_USER_EMAIL)
        editor.remove(KEY_USER_MOBILE)
        editor.remove(KEY_PENDING_TRAIN_ID)
        editor.remove(KEY_PENDING_JOURNEY_DATE)
        editor.apply()
    }
}
