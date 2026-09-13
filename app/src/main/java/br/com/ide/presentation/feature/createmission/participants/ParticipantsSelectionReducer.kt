package br.com.ide.presentation.feature.createmission.participants

import br.com.ide.domain.model.UserRole
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class ParticipantsSelectionReducer @Inject constructor() {

    fun toggleChurch(
        state: CreateMissionUiState,
        churchId: String
    ): CreateMissionUiState {

        if (
            state.creatorRole ==
            UserRole.LEADER
        ) {
            return state
        }

        val church =
            state.churches
                .firstOrNull {
                    it.id ==
                            churchId
                }
                ?: return state

        if (
            state.creatorRole ==
            UserRole.PASTOR &&
            church.districtId !=
            state.creatorDistrictId
        ) {
            return state
        }

        val updatedChurches =
            state.selectedChurchIds
                .toMutableSet()

        if (
            churchId in
            updatedChurches
        ) {
            updatedChurches.remove(
                churchId
            )
        } else {
            updatedChurches.add(
                churchId
            )
        }

        var updatedDistricts =
            state.selectedDistrictIds

        if (
            state.creatorRole ==
            UserRole.ADMIN
        ) {

            val districtChurchIds =
                state.churches
                    .filter {
                        it.districtId ==
                                church.districtId
                    }
                    .map {
                        it.id
                    }
                    .toSet()

            updatedDistricts =
                state.selectedDistrictIds
                    .toMutableSet()
                    .apply {

                        if (
                            districtChurchIds
                                .isNotEmpty() &&
                            updatedChurches
                                .containsAll(
                                    districtChurchIds
                                )
                        ) {
                            add(
                                church.districtId
                            )
                        } else {
                            remove(
                                church.districtId
                            )
                        }
                    }
        }

        return state.copy(
            selectedDistrictIds =
                updatedDistricts,
            selectedChurchIds =
                updatedChurches,
            participantsError =
                null
        )
    }

    fun toggleDistrict(
        state: CreateMissionUiState,
        districtId: String
    ): CreateMissionUiState {

        if (
            state.creatorRole !=
            UserRole.ADMIN
        ) {
            return state
        }

        val churchIds =
            state.churches
                .filter {
                    it.districtId ==
                            districtId
                }
                .map {
                    it.id
                }
                .toSet()

        if (
            churchIds.isEmpty()
        ) {
            return state
        }

        val updatedDistricts =
            state.selectedDistrictIds
                .toMutableSet()

        val updatedChurches =
            state.selectedChurchIds
                .toMutableSet()

        val allSelected =
            updatedChurches
                .containsAll(
                    churchIds
                )

        if (
            allSelected
        ) {

            updatedDistricts.remove(
                districtId
            )

            updatedChurches.removeAll(
                churchIds
            )

        } else {

            updatedDistricts.add(
                districtId
            )

            updatedChurches.addAll(
                churchIds
            )
        }

        return state.copy(
            selectedDistrictIds =
                updatedDistricts,
            selectedChurchIds =
                updatedChurches,
            participantsError =
                null
        )
    }

    fun selectAllChurches(
        state: CreateMissionUiState
    ): CreateMissionUiState {

        if (
            state.creatorRole ==
            UserRole.LEADER
        ) {
            return state
        }

        val selectableChurches =
            when (
                state.creatorRole
            ) {

                UserRole.PASTOR -> {
                    state.churches
                        .filter {
                            it.districtId ==
                                    state.creatorDistrictId
                        }
                }

                UserRole.ADMIN -> {
                    state.churches
                }

                else -> {
                    emptyList()
                }
            }

        val allChurchIds =
            selectableChurches
                .map {
                    it.id
                }
                .toSet()

        if (
            allChurchIds.isEmpty()
        ) {
            return state
        }

        val shouldSelectAll =
            !state.selectedChurchIds
                .containsAll(
                    allChurchIds
                )

        val selectedChurchIds =
            if (
                shouldSelectAll
            ) {
                allChurchIds
            } else {
                emptySet()
            }

        val selectedDistrictIds =
            when (
                state.creatorRole
            ) {

                UserRole.PASTOR -> {

                    state.creatorDistrictId
                        ?.let {
                            setOf(it)
                        }
                        ?: emptySet()
                }

                UserRole.ADMIN -> {

                    if (
                        shouldSelectAll
                    ) {

                        state.districts
                            .map {
                                it.id
                            }
                            .filter { districtId ->

                                state.churches
                                    .any {
                                        it.districtId ==
                                                districtId
                                    }
                            }
                            .toSet()

                    } else {
                        emptySet()
                    }
                }

                else -> {
                    state.selectedDistrictIds
                }
            }

        return state.copy(
            selectedDistrictIds =
                selectedDistrictIds,
            selectedChurchIds =
                selectedChurchIds,
            participantsError =
                null
        )
    }
}