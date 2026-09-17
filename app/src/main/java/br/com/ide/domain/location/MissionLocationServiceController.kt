package br.com.ide.domain.location

interface MissionLocationServiceController {

    fun start(
        missionId: String,
        userId: String
    )

    fun stop()
}
