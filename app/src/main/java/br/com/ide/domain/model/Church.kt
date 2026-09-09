package br.com.ide.domain.model

data class Church(
    val id: String,
    val name: String,
    val districtId: String,
    val active: Boolean = true,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)