package br.com.ide.presentation.feature.createmission

import javax.inject.Inject

class NavigationEventHandler @Inject constructor() {

    fun handle(
        event: CreateMissionEvent.Navigation,
        onPreviousStep: () -> Unit,
        onNextStep: () -> Unit
    ) {
        when (event) {

            CreateMissionEvent.PreviousStep -> {
                onPreviousStep()
            }

            CreateMissionEvent.Next -> {
                onNextStep()
            }
        }
    }
}