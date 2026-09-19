package br.com.ide.domain.model

data class MissionGeneralMetrics(

    /*
     * Estrutura da missão.
     */
    val participantCount: Int = 0,
    val groupCount: Int = 0,

    /*
     * Pessoas/encontros registrados por todos os participantes.
     */
    val encounterCount: Int = 0,

    /*
     * Ações realizadas durante a missão.
     */
    val visitCount: Int = 0,
    val prayerCount: Int = 0,
    val customActivityCount: Int = 0,

    /*
     * Estudos bíblicos.
     *
     * offeredBibleStudyCount inclui tanto OFFERED quanto ACCEPTED,
     * pois todo estudo aceito necessariamente foi oferecido.
     */
    val offeredBibleStudyCount: Int = 0,
    val acceptedBibleStudyCount: Int = 0,

    /*
     * Soma das quantidades de todos os materiais entregues
     * por todos os participantes.
     */
    val deliveredMaterialCount: Int = 0,

    /*
     * Quantidade total de pesquisas concluídas.
     */
    val completedSurveyCount: Int = 0,

    /*
     * Soma das distâncias percorridas pelos participantes
     * cujos trajetos estão disponíveis.
     */
    val distanceMeters: Double = 0.0,

    /*
     * Soma do tempo de participação de todos os participantes.
     */
    val participationSeconds: Long = 0L
)
