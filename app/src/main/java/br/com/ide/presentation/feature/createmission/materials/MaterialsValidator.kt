package br.com.ide.presentation.feature.createmission.materials

import br.com.ide.R
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

data class MaterialsValidationResult(
    val materialsError: Int? = null,
    val customMaterialNameError: Int? = null
) {

    val isValid: Boolean
        get() =
            materialsError == null &&
                    customMaterialNameError == null
}

class MaterialsValidator @Inject constructor() {

    fun validate(
        state: CreateMissionUiState
    ): MaterialsValidationResult {

        val materialsError =
            if (
                state.selectedMaterials
                    .isEmpty()
            ) {
                R.string
                    .create_mission_materials_required
            } else {
                null
            }

        val customMaterialNameError =
            if (
                MissionMaterialType.OTHER in
                state.selectedMaterials &&
                state.customMaterialName
                    .isBlank()
            ) {
                R.string
                    .create_mission_custom_material_required
            } else {
                null
            }

        return MaterialsValidationResult(
            materialsError =
                materialsError,
            customMaterialNameError =
                customMaterialNameError
        )
    }
}