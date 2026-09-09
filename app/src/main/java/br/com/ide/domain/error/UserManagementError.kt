package br.com.ide.domain.error

sealed class UserManagementError(
    message: String
) : Exception(message) {

    data object CannotManageUser :
        UserManagementError(
            "CANNOT_MANAGE_USER"
        )

    data object CannotAssignRole :
        UserManagementError(
            "CANNOT_ASSIGN_ROLE"
        )

    data object DistrictRequired :
        UserManagementError(
            "DISTRICT_REQUIRED"
        )

    data object ChurchRequired :
        UserManagementError(
            "CHURCH_REQUIRED"
        )

    data object PastorWithoutDistrict :
        UserManagementError(
            "PASTOR_WITHOUT_DISTRICT"
        )

    data object CannotManageOtherDistrict :
        UserManagementError(
            "CANNOT_MANAGE_OTHER_DISTRICT"
        )

    data object CannotMoveToOtherDistrict :
        UserManagementError(
            "CANNOT_MOVE_TO_OTHER_DISTRICT"
        )

    data object InvalidRoleForPastor :
        UserManagementError(
            "INVALID_ROLE_FOR_PASTOR"
        )

    data object AdminRoleNotAssignable :
        UserManagementError(
            "ADMIN_ROLE_NOT_ASSIGNABLE"
        )

    data object ChurchDoesNotBelongToDistrict :
        UserManagementError(
            "CHURCH_DOES_NOT_BELONG_TO_DISTRICT"
        )

    data object UnableToValidateChurch :
        UserManagementError(
            "UNABLE_TO_VALIDATE_CHURCH"
        )
}