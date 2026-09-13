package br.com.ide.presentation.feature.createmission.materials

import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionSubScreenReducer
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import javax.inject.Inject

class MaterialsEventHandler @Inject constructor(
    private val materialsReducer:
    MaterialsReducer,
    private val subScreenReducer:
    CreateMissionSubScreenReducer
) {

    fun handle(
        state: CreateMissionUiState,
        event: CreateMissionEvent.Materials
    ): CreateMissionUiState {

        return when (event) {

            CreateMissionEvent.OpenMaterials -> {

                subScreenReducer
                    .openMaterials(
                        state
                    )
            }

            CreateMissionEvent.CloseMaterials -> {

                subScreenReducer
                    .close(
                        state
                    )
            }

            is CreateMissionEvent.MaterialToggled -> {

                materialsReducer
                    .toggleMaterial(
                        state = state,
                        material = event.material
                    )
            }

            is CreateMissionEvent.CustomMaterialNameChanged -> {

                materialsReducer
                    .updateCustomMaterialName(
                        state = state,
                        value = event.value
                    )
            }

            CreateMissionEvent.SaveMaterials -> {

                /*
                 * Salvar envolve validação.
                 * O ViewModel continua responsável
                 * por esse fluxo por enquanto.
                 */
                state
            }
        }
    }
}