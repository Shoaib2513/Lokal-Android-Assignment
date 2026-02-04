package com.lokal.assignment.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lokal.assignment.analytics.AnalyticsLogger
import com.lokal.assignment.data.OtpManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val otpManager: OtpManager = OtpManager()
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    private var otpTimerJob: Job? = null
    private var sessionTimerJob: Job? = null


    fun onEmailChanged(email: String) {
        _state.update {
            it.copy(email = email, error = null)
        }
    }

    fun sendOtp() {
        val email = state.value.email.trim()
        if (email.isEmpty()) return

        otpManager.generateOtp(email)
        AnalyticsLogger.otpGenerated(email)

        startOtpTimer()

        _state.update {
            it.copy(
                screen = Screen.OTP,
                otp = "",
                error = null
            )
        }
    }

    fun onOtpChanged(otp: String) {
        if (otp.length <= 6) {
            _state.update {
                it.copy(otp = otp, error = null)
            }
        }
    }
    fun goBackToLogin() {
        _state.update {
            it.copy(
                screen = Screen.LOGIN,
                otp = "",
                otpRemainingSeconds = 0,
                error = null
            )
        }
    }

    fun verifyOtp() {
        val currentState = state.value
        val email = currentState.email
        val otp = currentState.otp

        _state.update { it.copy(isVerifying = true) }

        val result = otpManager.validateOtp(email, otp)

        when (result) {
            OtpManager.OtpResult.SUCCESS -> {
                AnalyticsLogger.otpSuccess(email)
                startSession()
            }

            OtpManager.OtpResult.EXPIRED -> {
                AnalyticsLogger.otpFailure(email)
                _state.update {
                    it.copy(
                        isVerifying = false,
                        error = "OTP expired. Please resend."
                    )
                }
            }

            OtpManager.OtpResult.INVALID -> {
                AnalyticsLogger.otpFailure(email)
                _state.update {
                    it.copy(
                        isVerifying = false,
                        error = "Invalid OTP. Try again."
                    )
                }
            }

            OtpManager.OtpResult.ATTEMPTS_EXCEEDED -> {
                AnalyticsLogger.otpFailure(email)

                otpTimerJob?.cancel()

                _state.update {
                    AuthState(
                        screen = Screen.LOGIN,
                        error = "Maximum OTP attempts exceeded. Please login again."
                    )
                }
            }


        }
    }

    fun resendOtp() {
        sendOtp()
    }

    private fun startOtpTimer() {
        otpTimerJob?.cancel()

        otpTimerJob = viewModelScope.launch {
            for (sec in 60 downTo 0) {
                _state.update {
                    it.copy(otpRemainingSeconds = sec)
                }
                delay(1000)
            }
        }
    }

    private fun startSession() {
        otpTimerJob?.cancel()

        val startTime = System.currentTimeMillis()

        _state.update {
            it.copy(
                screen = Screen.SESSION,
                isVerifying = false,
                sessionStartTime = startTime,
                sessionDuration = 0L
            )
        }

        sessionTimerJob?.cancel()
        sessionTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update {
                    it.copy(
                        sessionDuration = System.currentTimeMillis() - startTime
                    )
                }
            }
        }
    }

    fun logout() {
        sessionTimerJob?.cancel()
        AnalyticsLogger.logout(state.value.email)

        _state.value = AuthState()
    }
}
