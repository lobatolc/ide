package br.com.ide.presentation.feature.createmission.materials

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.feature.createmission.CreateMissionUiState
import br.com.ide.presentation.feature.createmission.components.CreateMissionHeader
import br.com.ide.presentation.feature.createmission.components.MissionFieldError
import br.com.ide.presentation.feature.createmission.components.MissionScreenContainer
import br.com.ide.presentation.feature.createmission.components.SelectionRow
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun CreateMissionMaterialsScreen(
    uiState: CreateMissionUiState,
    onEvent: (CreateMissionEvent) -> Unit
) {
    MissionScreenContainer {

        CreateMissionHeader(
            title =
                stringResource(
                    R.string.create_mission_configure_materials
                ),
            backContentDescription =
                stringResource(
                    R.string.create_mission_back
                ),
            onBackClick = {
                onEvent(
                    CreateMissionEvent.CloseMaterials
                )
            }
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        IdeScreenSubtitle(
            text =
                stringResource(
                    R.string.create_mission_materials_subtitle
                )
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(16.dp),
            color =
                MaterialTheme.colorScheme.surface,
            border =
                BorderStroke(
                    width = 1.dp,
                    color =
                        if (
                            uiState.materialsError != null
                        ) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                )
        ) {

            Column {

                MissionMaterialType.entries
                    .forEachIndexed {
                            index,
                            material ->

                        SelectionRow(
                            title =
                                stringResource(
                                    material.toStringRes()
                                ),
                            checked =
                                material in
                                        uiState.selectedMaterials,
                            onCheckedChange = {
                                onEvent(
                                    CreateMissionEvent
                                        .MaterialToggled(
                                            material
                                        )
                                )
                            }
                        )

                        if (
                            index <
                            MissionMaterialType.entries.lastIndex
                        ) {
                            HorizontalDivider(
                                modifier =
                                    Modifier.padding(
                                        start = 16.dp
                                    )
                            )
                        }
                    }
            }
        }

        uiState.materialsError
            ?.let { errorRes ->

                Spacer(
                    modifier =
                        Modifier.height(6.dp)
                )

                MissionFieldError(
                    errorRes =
                        errorRes
                )
            }

        if (
            MissionMaterialType.OTHER in
            uiState.selectedMaterials
        ) {

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeTextField(
                value =
                    uiState.customMaterialName,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .CustomMaterialNameChanged(
                                it
                            )
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_custom_material
                    ),
                errorRes =
                    uiState.customMaterialNameError,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        Spacer(
            modifier =
                Modifier.height(28.dp)
        )

        IdePrimaryButton(
            text =
                stringResource(
                    R.string.create_mission_save_materials
                ),
            onClick = {
                onEvent(
                    CreateMissionEvent.SaveMaterials
                )
            },
            modifier =
                Modifier.fillMaxWidth()
        )
    }
}