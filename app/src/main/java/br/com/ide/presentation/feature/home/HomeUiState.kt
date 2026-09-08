package br.com.ide.presentation.feature.home

import androidx.annotation.StringRes
import br.com.ide.domain.model.Mission
import br.com.ide.domain.model.MissionStatus
import br.com.ide.domain.model.UserRole

data class HomeUiState(
    val isLoading: Boolean = false,

    val userName: String = "",

    val missions: List<Mission> = emptyList(),

    val searchQuery: String = "",

    val selectedStatus: MissionStatus? = null,

    @StringRes
    val errorMessage: Int? = null,

    val userRole: UserRole = UserRole.MISSIONARY
)