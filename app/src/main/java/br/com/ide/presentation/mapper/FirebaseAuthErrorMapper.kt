package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

@StringRes
fun mapFirebaseAuthError(
    exception: Throwable
): Int {

    return when (exception) {

        is FirebaseAuthInvalidCredentialsException,
        is FirebaseAuthInvalidUserException -> {
            R.string.error_invalid_credentials
        }

        is FirebaseNetworkException -> {
            R.string.error_network
        }

        is FirebaseAuthException -> {

            when (exception.errorCode) {

                "ERROR_TOO_MANY_REQUESTS" -> {
                    R.string.error_too_many_requests
                }

                "ERROR_NETWORK_REQUEST_FAILED" -> {
                    R.string.error_network
                }

                else -> {
                    R.string.error_login_generic
                }
            }
        }

        else -> {
            R.string.error_unexpected
        }
    }
}