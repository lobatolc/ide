package br.com.ide.presentation.feature.login

import androidx.annotation.StringRes
import br.com.ide.R
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException

@StringRes
fun Throwable.toLoginErrorRes(): Int {
    return when (this) {

        is FirebaseNetworkException ->
            R.string.error_network

        is FirebaseAuthException -> {
            when (errorCode) {

                "ERROR_TOO_MANY_REQUESTS" ->
                    R.string.error_too_many_requests

                "ERROR_INVALID_CREDENTIAL",
                "ERROR_WRONG_PASSWORD",
                "ERROR_USER_NOT_FOUND" ->
                    R.string.error_invalid_credentials

                else ->
                    R.string.error_google_login
            }
        }

        else ->
            R.string.error_google_login
    }
}