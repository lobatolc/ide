package br.com.ide.domain.model

enum class UserRole(
    val level: Int
) {
    MISSIONARY(1),
    LEADER(2),
    PASTOR(3),
    ADMIN(4);

    fun hasAtLeast(requiredRole: UserRole): Boolean {
        return level >= requiredRole.level
    }
}