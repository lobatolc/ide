package br.com.ide.domain.model

data class MissionPersonalMetrics(

    /*
     * Cada encontro salvo representa uma pessoa/encontro
     * registrado pelo missionário durante a missão.
     */
    val encounterCount: Int = 0,

    /*
     * Ações simples realizadas.
     */
    val visitCount: Int = 0,
    val prayerCount: Int = 0,
    val customActivityCount: Int = 0,

    /*
     * Estudos bíblicos.
     *
     * offeredBibleStudyCount inclui tanto OFFERED quanto ACCEPTED,
     * pois o estudo aceito necessariamente foi oferecido.
     */
    val offeredBibleStudyCount: Int = 0,
    val acceptedBibleStudyCount: Int = 0,

    /*
     * Soma das quantidades de todos os materiais entregues.
     */
    val deliveredMaterialCount: Int = 0,

    /*
     * Quantidade de pesquisas concluídas.
     */
    val completedSurveyCount: Int = 0,

    /*
     * Distância percorrida pelo participante durante a missão.
     */
    val distanceMeters: Double = 0.0,

    /*
     * Tempo total de participação na missão.
     */
    val participationSeconds: Long = 0L
)
