package com.lokal.assignment.analytics

import timber.log.Timber

object AnalyticsLogger {

    fun otpGenerated(email: String) {
        Timber.d("OTP generated | email=$email")
    }

    fun otpSuccess(email: String) {
        Timber.d("OTP validation success | email=$email")
    }

    fun otpFailure(email: String) {
        Timber.d("OTP validation failure | email=$email")
    }

    fun logout(email: String) {
        Timber.d("User logged out | email=$email")
    }
}
