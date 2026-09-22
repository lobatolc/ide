package br.com.ide.presentation.feature.metrics

sealed interface MetricsEvent {
    data object Retry : MetricsEvent
    data object OpenSelector : MetricsEvent
    data object DismissSelector : MetricsEvent

    data class SearchChanged(
        val query: String
    ) : MetricsEvent

    data class PeriodChanged(
        val period: MetricsPeriodFilter
    ) : MetricsEvent

    data class MissionToggled(
        val missionId: String
    ) : MetricsEvent
    data object ApplySelection : MetricsEvent
}
