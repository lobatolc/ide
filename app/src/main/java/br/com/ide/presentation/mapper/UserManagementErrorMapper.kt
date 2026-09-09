package br.com.ide.presentation.mapper

import androidx.annotation.StringRes
import br.com.ide.R
import br.com.ide.domain.error.UserManagementError

@StringRes
fun Throwable.toUserManagementErrorRes(): Int {
    return when (this) {

        UserManagementError.CannotManageUser ->
            R.string.user_management_error_cannot_manage_user

        UserManagementError.CannotAssignRole ->
            R.string.user_management_error_cannot_assign_role

        UserManagementError.DistrictRequired ->
            R.string.user_management_error_district_required

        UserManagementError.ChurchRequired ->
            R.string.user_management_error_church_required

        UserManagementError.PastorWithoutDistrict ->
            R.string.user_management_error_pastor_without_district

        UserManagementError.CannotManageOtherDistrict ->
            R.string.user_management_error_other_district

        UserManagementError.CannotMoveToOtherDistrict ->
            R.string.user_management_error_move_district

        UserManagementError.InvalidRoleForPastor ->
            R.string.user_management_error_invalid_role_pastor

        UserManagementError.AdminRoleNotAssignable ->
            R.string.user_management_error_admin_role

        UserManagementError.ChurchDoesNotBelongToDistrict ->
            R.string.user_management_error_church_wrong_district

        UserManagementError.UnableToValidateChurch ->
            R.string.user_management_error_validate_church

        else ->
            R.string.user_management_error_generic
    }
}