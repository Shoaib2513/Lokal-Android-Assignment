package com.lokal.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import com.lokal.assignment.ui.screen.LoginScreen
import com.lokal.assignment.ui.screen.OtpScreen
import com.lokal.assignment.ui.screen.SessionScreen
import com.lokal.assignment.ui.theme.LokalAndroidAssignmentTheme
import com.lokal.assignment.viewmodel.AuthViewModel
import com.lokal.assignment.viewmodel.Screen
import androidx.activity.compose.BackHandler


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LokalAndroidAssignmentTheme {

                val viewModel: AuthViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                when (state.screen) {

                    Screen.LOGIN -> {
                        LoginScreen(
                            email = state.email,
                            isLoading = state.isLoading,
                            errorMessage = state.error,
                            onEmailChange = viewModel::onEmailChanged,
                            onSendOtpClick = viewModel::sendOtp
                        )

                    }

                    Screen.OTP -> {

                        BackHandler {
                            viewModel.goBackToLogin()
                        }

                        OtpScreen(
                            email = state.email,
                            otp = state.otp,
                            remainingTime = state.otpRemainingSeconds,
                            isVerifying = state.isVerifying,
                            errorMessage = state.error,
                            onOtpChange = viewModel::onOtpChanged,
                            onVerifyClick = viewModel::verifyOtp,
                            onResendClick = viewModel::resendOtp
                        )
                    }


                    Screen.SESSION -> {
                        SessionScreen(
                            sessionStartTimeMillis = state.sessionStartTime,
                            sessionDurationMillis = state.sessionDuration,
                            onLogoutClick = viewModel::logout
                        )
                    }
                }
            }
        }
    }
}
