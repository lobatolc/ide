package br.com.ide.domain.model

data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val sabbathSchoolClass: SabbathSchoolClass,
    val role: UserRole
)