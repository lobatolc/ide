package br.com.ide.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Login

@Serializable
data object Register

@Serializable
data object CompleteRegistration

@Serializable
data object ForgotPassword

@Serializable
data object Home

@Serializable
data object Mission

@Serializable
data object Profile

@Serializable
data object EditProfile

@Serializable
data object UserManagement

@Serializable
data class UserManagementDetails(
    val userId: String
)