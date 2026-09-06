package br.com.ide.presentation.mapper

import br.com.ide.R
import br.com.ide.domain.model.UserRole

fun UserRole.toStringRes(): Int {
    return when (this) {
        UserRole.MISSIONARY -> R.string.user_role_missionary
        UserRole.LEADER -> R.string.user_role_leader
        UserRole.PASTOR -> R.string.user_role_pastor
        UserRole.ADMIN -> R.string.user_role_admin
    }
}