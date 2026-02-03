package com.lokal.assignment.viewmodel

enum class Screen {
    LOGIN,
    OTP,
    SESSION
}

data class AuthState(
    val email: String = "",
    val otp: String = "",
    val screen: Screen = Screen.LOGIN,

    val isLoading: Boolean = false,
    val isVerifying: Boolean = false,

    val error: String? = null,

    val otpRemainingSeconds: Int = 0,

    val sessionStartTime: Long = 0L,
    val sessionDuration: Long = 0L
)
