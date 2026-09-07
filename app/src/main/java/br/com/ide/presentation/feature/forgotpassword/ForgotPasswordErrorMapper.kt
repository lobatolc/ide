package br.com.ide.presentation.feature.forgotpassword

import androidx.annotation.StringRes
import br.com.ide.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

@StringRes
fun mapForgotPasswordError(
    exception: Throwable
): Int {
    return when (exception) {

        is FirebaseNetworkException ->
            R.string.error_network

        is FirebaseAuthException -> {
            when (exception.errorCode) {

                "ERROR_TOO_MANY_REQUESTS" ->
                    R.string.error_too_many_requests

                "ERROR_NETWORK_REQUEST_FAILED" ->
                    R.string.error_network

                else ->
                    R.string.forgot_password_error
            }
        }

        else ->
            R.string.forgot_password_error
    }
}