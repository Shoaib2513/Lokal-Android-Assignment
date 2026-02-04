package com.lokal.assignment.data

import timber.log.Timber
import kotlin.random.Random
import android.util.Log

class OtpManager {

    companion object {
        private const val OTP_LENGTH = 6
        private const val OTP_EXPIRY_MILLIS = 60_000L
        private const val MAX_ATTEMPTS = 3
    }

    private data class OtpData(
        val otp: String,
        val createdAt: Long,
        var attemptsLeft: Int
    )

    private val otpStore: MutableMap<String, OtpData> = mutableMapOf()

    enum class OtpResult {
        SUCCESS,
        INVALID,
        EXPIRED,
        ATTEMPTS_EXCEEDED
    }

    fun generateOtp(email: String) {
        val otp = generateSixDigitOtp()

        Log.d("OTP_TEST", "OTP for $email = $otp")

        otpStore[email] = OtpData(
            otp = otp,
            createdAt = System.currentTimeMillis(),
            attemptsLeft = MAX_ATTEMPTS
        )
    }


    fun validateOtp(email: String, enteredOtp: String): OtpResult {
        val data = otpStore[email] ?: return OtpResult.EXPIRED

        if (System.currentTimeMillis() - data.createdAt > OTP_EXPIRY_MILLIS) {
            otpStore.remove(email)
            return OtpResult.EXPIRED
        }

        if (data.attemptsLeft <= 0) {
            otpStore.remove(email)
            return OtpResult.ATTEMPTS_EXCEEDED
        }

        return if (data.otp == enteredOtp) {
            otpStore.remove(email)
            OtpResult.SUCCESS
        } else {
            data.attemptsLeft--
            if (data.attemptsLeft <= 0) {
                otpStore.remove(email)
                OtpResult.ATTEMPTS_EXCEEDED
            } else {
                OtpResult.INVALID
            }
        }
    }

    private fun generateSixDigitOtp(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}
