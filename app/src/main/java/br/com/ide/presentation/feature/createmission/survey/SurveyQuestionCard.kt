package br.com.ide.presentation.feature.createmission.survey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.ide.R
import br.com.ide.domain.model.SurveyQuestionDraft
import br.com.ide.domain.model.SurveyQuestionType
import br.com.ide.presentation.components.IdeDropdownField
import br.com.ide.presentation.components.IdeTextField
import br.com.ide.presentation.feature.createmission.CreateMissionEvent
import br.com.ide.presentation.mapper.toStringRes

@Composable
fun SurveyQuestionCard(
    number: Int,
    question: SurveyQuestionDraft,
    canRemove: Boolean,
    onEvent: (CreateMissionEvent) -> Unit
) {
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
                    MaterialTheme.colorScheme.outline
            )
    ) {

        Column(
            modifier =
                Modifier.padding(16.dp)
        ) {

            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text =
                        "${stringResource(R.string.create_mission_survey_question)} $number",
                    style =
                        MaterialTheme.typography.titleMedium,
                    fontWeight =
                        FontWeight.SemiBold,
                    modifier =
                        Modifier.weight(1f)
                )

                if (canRemove) {

                    IconButton(
                        onClick = {
                            onEvent(
                                CreateMissionEvent
                                    .RemoveSurveyQuestion(
                                        question.id
                                    )
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Outlined.Delete,
                            contentDescription =
                                stringResource(
                                    R.string.create_mission_remove_question
                                )
                        )
                    }
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            IdeTextField(
                value =
                    question.question,
                onValueChange = {
                    onEvent(
                        CreateMissionEvent
                            .SurveyQuestionChanged(
                                questionId =
                                    question.id,
                                value =
                                    it
                            )
                    )
                },
                label =
                    stringResource(
                        R.string.create_mission_survey_question
                    ),
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            IdeDropdownField(
                selectedValue =
                    question.type,
                options =
                    SurveyQuestionType.entries,
                label =
                    stringResource(
                        R.string.create_mission_survey_question_type
                    ),
                optionText = {
                    stringResource(
                        it.toStringRes()
                    )
                },
                onOptionSelected = {
                    onEvent(
                        CreateMissionEvent
                            .SurveyQuestionTypeChanged(
                                questionId =
                                    question.id,
                                type =
                                    it
                            )
                    )
                },
                modifier =
                    Modifier.fillMaxWidth()
            )

            if (
                question.type ==
                SurveyQuestionType.SINGLE_CHOICE
            ) {

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                question.options
                    .forEachIndexed {
                            optionIndex,
                            option ->

                        Row(
                            modifier =
                                Modifier.fillMaxWidth(),
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            IdeTextField(
                                value =
                                    option,
                                onValueChange = {
                                    onEvent(
                                        CreateMissionEvent
                                            .SurveyOptionChanged(
                                                questionId =
                                                    question.id,
                                                optionIndex =
                                                    optionIndex,
                                                value =
                                                    it
                                            )
                                    )
                                },
                                label =
                                    "${stringResource(R.string.create_mission_survey_option)} ${optionIndex + 1}",
                                modifier =
                                    Modifier.weight(1f)
                            )

                            if (
                                question.options.size > 2
                            ) {

                                Spacer(
                                    modifier =
                                        Modifier.width(4.dp)
                                )

                                IconButton(
                                    onClick = {
                                        onEvent(
                                            CreateMissionEvent
                                                .RemoveSurveyOption(
                                                    questionId =
                                                        question.id,
                                                    optionIndex =
                                                        optionIndex
                                                )
                                        )
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.Delete,
                                        contentDescription =
                                            null
                                    )
                                }
                            }
                        }

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )
                    }

                TextButton(
                    onClick = {
                        onEvent(
                            CreateMissionEvent
                                .AddSurveyOption(
                                    question.id
                                )
                        )
                    }
                ) {
                    Text(
                        text =
                            "+ " +
                                    stringResource(
                                        R.string.create_mission_add_option
                                    )
                    )
                }
            }
        }
    }
}