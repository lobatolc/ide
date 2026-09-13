package br.com.ide.presentation.feature.createmission.materials

import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class MaterialsReducer @Inject constructor() {

    fun toggleMaterial(
        state: CreateMissionUiState,
        material: MissionMaterialType
    ): CreateMissionUiState {

        val updatedMaterials =
            state.selectedMaterials
                .toMutableSet()

        if (
            material in updatedMaterials
        ) {
            updatedMaterials.remove(
                material
            )
        } else {
            updatedMaterials.add(
                material
            )
        }

        val customMaterialName =
            if (
                MissionMaterialType.OTHER in
                updatedMaterials
            ) {
                state.customMaterialName
            } else {
                ""
            }

        return state.copy(
            selectedMaterials =
                updatedMaterials,

            customMaterialName =
                customMaterialName,

            materialsError =
                null,

            customMaterialNameError =
                null
        )
    }

    fun updateCustomMaterialName(
        state: CreateMissionUiState,
        value: String
    ): CreateMissionUiState {

        return state.copy(
            customMaterialName =
                value,

            customMaterialNameError =
                null,

            materialsError =
                null
        )
    }
}