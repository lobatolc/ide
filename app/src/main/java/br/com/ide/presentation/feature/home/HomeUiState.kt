package br.com.ide.presentation.feature.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val activeMissionName: String? = null,
    val errorMessage: String? = null
)