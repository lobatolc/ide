package br.com.ide.presentation.feature.metrics

import br.com.ide.domain.model.BibleStudyStatus
import br.com.ide.domain.model.EncounterAgeGroup
import br.com.ide.domain.model.MissionActivityType
import br.com.ide.domain.model.MissionMaterialType
import br.com.ide.domain.model.MissionParticipantStatus
import br.com.ide.domain.model.SurveyQuestionType
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.round

object MetricsExcelExporter {

    private val dateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private val dateFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun create(
        snapshot: MetricsExportSnapshot,
        language: String
    ): ByteArray {
        val labels = Labels.forLanguage(language)

        val sheets = listOf(
            Sheet(labels.summarySheet, buildSummaryRows(snapshot, labels)),
            Sheet(labels.missionsSheet, buildMissionRows(snapshot, labels)),
            Sheet(labels.participantsSheet, buildParticipantRows(snapshot, labels)),
            Sheet(labels.encountersSheet, buildEncounterRows(snapshot, labels)),
            Sheet(labels.materialsSheet, buildMaterialRows(snapshot, labels)),
            Sheet(labels.surveySheet, buildSurveyRows(snapshot, labels))
        )

        return buildWorkbook(sheets)
    }

    // =========================================================
    // Resumo
    // =========================================================

    private fun buildSummaryRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val s = snapshot.summary

        return listOf(
            listOf(Cell.Text(labels.metric), Cell.Text(labels.value)),
            listOf(Cell.Text(labels.missions), Cell.Number(s.missionCount.toDouble())),
            listOf(Cell.Text(labels.uniqueMissionaries), Cell.Number(s.uniqueMissionaryCount.toDouble())),
            listOf(Cell.Text(labels.participations), Cell.Number(s.participationCount.toDouble())),
            listOf(Cell.Text(labels.groups), Cell.Number(s.groupCount.toDouble())),
            listOf(Cell.Text(labels.encounters), Cell.Number(s.encounterCount.toDouble())),
            listOf(Cell.Text(labels.visits), Cell.Number(s.visitCount.toDouble())),
            listOf(Cell.Text(labels.prayers), Cell.Number(s.prayerCount.toDouble())),
            listOf(Cell.Text(labels.otherActivities), Cell.Number(s.customActivityCount.toDouble())),
            listOf(Cell.Text(labels.offeredBibleStudies), Cell.Number(s.offeredBibleStudyCount.toDouble())),
            listOf(Cell.Text(labels.acceptedBibleStudies), Cell.Number(s.acceptedBibleStudyCount.toDouble())),
            listOf(Cell.Text(labels.materials), Cell.Number(s.deliveredMaterialCount.toDouble())),
            listOf(Cell.Text(labels.surveys), Cell.Number(s.completedSurveyCount.toDouble())),
            listOf(
                Cell.Text(labels.distanceKilometers),
                Cell.Number(roundToTwoDecimals(s.distanceMeters / 1000.0))
            ),
            listOf(
                Cell.Text(labels.missionaryTime),
                Cell.Text(formatDuration(s.participationSeconds))
            )
        )
    }

    // =========================================================
    // Missões
    // =========================================================

    private fun buildMissionRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>(
            listOf(
                labels.mission,
                labels.scheduledAt,
                labels.startedAt,
                labels.endedAt,
                labels.participatingChurches,
                labels.operationalGroups,
                labels.description
            ).map(Cell::Text)
        )

        snapshot.missions.forEach { data ->
            val mission = data.mission

            val operationalGroups =
                mission.groups.size +
                        if (data.participants.any { it.groupId == null }) {
                            1
                        } else {
                            0
                        }

            rows += listOf(
                Cell.Text(mission.name),
                Cell.Text(formatDateTime(mission.scheduledAt)),
                Cell.Text(formatDateTime(mission.startedAt)),
                Cell.Text(formatDateTime(mission.endedAt)),
                Cell.Number(mission.participatingChurchIds.distinct().size.toDouble()),
                Cell.Number(operationalGroups.toDouble()),
                Cell.Text(mission.description)
            )
        }

        return rows
    }

    // =========================================================
    // Participantes
    // =========================================================

    private fun buildParticipantRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>(
            listOf(
                labels.mission,
                labels.missionDate,
                labels.missionary,
                labels.group,
                labels.support,
                labels.status,
                labels.joinedAt,
                labels.endedAt,
                labels.endedByMission
            ).map(Cell::Text)
        )

        snapshot.missions.forEach { data ->
            val mission = data.mission

            data.participants.forEach { participant ->
                val groupName =
                    participant.groupId
                        ?.let { groupId ->
                            mission.groups
                                .firstOrNull { it.id == groupId }
                                ?.name
                        }
                        ?: labels.generalGroup

                rows += listOf(
                    Cell.Text(mission.name),
                    Cell.Text(formatDate(mission.endedAt ?: mission.scheduledAt)),
                    Cell.Text(snapshot.userNames[participant.userId].orEmpty()),
                    Cell.Text(groupName),
                    Cell.Text(labels.booleanLabel(participant.isSupport)),
                    Cell.Text(labels.participantStatusLabel(participant.status)),
                    Cell.Text(formatDateTime(participant.joinedAt)),
                    Cell.Text(formatDateTime(participant.endedAt)),
                    Cell.Text(labels.booleanLabel(participant.endedByMission))
                )
            }
        }

        return rows
    }

    // =========================================================
    // Encontros
    // =========================================================

    private fun buildEncounterRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>(
            listOf(
                labels.mission,
                labels.missionDate,
                labels.missionary,
                labels.group,
                labels.person,
                labels.ageGroup,
                labels.phone,
                labels.address,
                labels.atHome,
                labels.mapsLocation,
                labels.visit,
                labels.prayer,
                labels.otherActivity,
                labels.bibleStudy,
                labels.surveyCompleted,
                labels.followUp,
                labels.materialQuantity,
                labels.notes
            ).map(Cell::Text)
        )

        snapshot.missions.forEach { data ->
            val mission = data.mission

            data.encounters.forEach { encounter ->
                val groupName =
                    encounter.groupId
                        ?.let { groupId ->
                            mission.groups
                                .firstOrNull { it.id == groupId }
                                ?.name
                        }
                        ?: labels.generalGroup

                rows += listOf(
                    Cell.Text(mission.name),
                    Cell.Text(formatDate(mission.endedAt ?: mission.scheduledAt)),
                    Cell.Text(snapshot.userNames[encounter.registeredByUserId].orEmpty()),
                    Cell.Text(groupName),
                    Cell.Text(encounter.personName.orEmpty()),
                    Cell.Text(
                        encounter.ageGroup
                            ?.let(labels::ageGroupLabel)
                            .orEmpty()
                    ),
                    Cell.Text(formatBrazilianPhone(encounter.phoneDigits.orEmpty())),
                    Cell.Text(encounter.address.orEmpty()),
                    Cell.Text(labels.booleanLabel(encounter.isAtPersonHome)),
                    buildMapsCell(
                        latitude = encounter.latitude,
                        longitude = encounter.longitude,
                        labels = labels
                    ),
                    Cell.Text(
                        labels.booleanLabel(
                            MissionActivityType.VISIT in encounter.performedActivities
                        )
                    ),
                    Cell.Text(
                        labels.booleanLabel(
                            MissionActivityType.PRAYER in encounter.performedActivities
                        )
                    ),
                    Cell.Text(
                        labels.booleanLabel(
                            MissionActivityType.OTHER in encounter.performedActivities
                        )
                    ),
                    Cell.Text(labels.bibleStudyStatusLabel(encounter.bibleStudyStatus)),
                    Cell.Text(
                        labels.booleanLabel(
                            MissionActivityType.OPINION_SURVEY in encounter.performedActivities
                        )
                    ),
                    Cell.Text(labels.booleanLabel(encounter.acceptedFollowUp)),
                    Cell.Number(
                        encounter.materials
                            .sumOf { it.quantity.coerceAtLeast(0) }
                            .toDouble()
                    ),
                    Cell.Text(encounter.notes.orEmpty())
                )
            }
        }

        return rows
    }

    // =========================================================
    // Materiais
    // =========================================================

    private fun buildMaterialRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>(
            listOf(
                labels.mission,
                labels.person,
                labels.missionary,
                labels.material,
                labels.quantity
            ).map(Cell::Text)
        )

        snapshot.missions.forEach { data ->
            data.encounters.forEach { encounter ->
                encounter.materials.forEach { material ->
                    rows += listOf(
                        Cell.Text(data.mission.name),
                        Cell.Text(encounter.personName.orEmpty()),
                        Cell.Text(snapshot.userNames[encounter.registeredByUserId].orEmpty()),
                        Cell.Text(
                            labels.materialLabel(
                                type = material.type,
                                customName = material.customName
                            )
                        ),
                        Cell.Number(material.quantity.coerceAtLeast(0).toDouble())
                    )
                }
            }
        }

        return rows
    }

    // =========================================================
    // Pesquisa
    // =========================================================

    private fun buildSurveyRows(
        snapshot: MetricsExportSnapshot,
        labels: Labels
    ): List<List<Cell>> {
        val rows = mutableListOf<List<Cell>>(
            listOf(
                labels.mission,
                labels.person,
                labels.missionary,
                labels.question,
                labels.questionType,
                labels.answer
            ).map(Cell::Text)
        )

        snapshot.missions.forEach { data ->
            data.encounters.forEach { encounter ->
                encounter.surveyAnswers.forEach { answer ->
                    rows += listOf(
                        Cell.Text(data.mission.name),
                        Cell.Text(encounter.personName.orEmpty()),
                        Cell.Text(snapshot.userNames[encounter.registeredByUserId].orEmpty()),
                        Cell.Text(answer.question),
                        Cell.Text(labels.questionTypeLabel(answer.type)),
                        Cell.Text(answer.answer)
                    )
                }
            }
        }

        return rows
    }

    // =========================================================
    // Formatações amigáveis
    // =========================================================

    private fun buildMapsCell(
        latitude: Double?,
        longitude: Double?,
        labels: Labels
    ): Cell {
        if (latitude == null || longitude == null) {
            return Cell.Text("")
        }

        val url =
            "https://www.google.com/maps?q=$latitude,$longitude"

        val formula =
            "HYPERLINK(\"$url\",\"${escapeFormulaText(labels.openMaps)}\")"

        return Cell.Formula(
            formula = formula,
            displayValue = labels.openMaps
        )
    }

    private fun formatBrazilianPhone(
        phone: String
    ): String {
        var digits =
            phone.filter(Char::isDigit)

        if (
            digits.startsWith("55") &&
            digits.length in 12..13
        ) {
            digits = digits.drop(2)
        }

        return when (digits.length) {
            11 ->
                "(${digits.substring(0, 2)}) ${digits.substring(2, 3)} ${digits.substring(3, 7)}-${digits.substring(7, 11)}"

            10 ->
                "(${digits.substring(0, 2)}) ${digits.substring(2, 6)}-${digits.substring(6, 10)}"

            else ->
                phone
        }
    }

    private fun formatDuration(
        totalSeconds: Long
    ): String {
        val safeSeconds =
            totalSeconds.coerceAtLeast(0L)

        val hours =
            safeSeconds / 3600L

        val minutes =
            (safeSeconds % 3600L) / 60L

        val seconds =
            safeSeconds % 60L

        return "${hours}h ${minutes}min ${seconds}s"
    }

    private fun roundToTwoDecimals(
        value: Double
    ): Double =
        round(value * 100.0) / 100.0

    private fun escapeFormulaText(
        value: String
    ): String =
        value.replace("\"", "\"\"")

    // =========================================================
    // XLSX
    // =========================================================

    private fun buildWorkbook(
        sheets: List<Sheet>
    ): ByteArray {
        val output = ByteArrayOutputStream()

        ZipOutputStream(output).use { zip ->
            zip.writeEntry("[Content_Types].xml", contentTypesXml(sheets.size))
            zip.writeEntry("_rels/.rels", rootRelationshipsXml())
            zip.writeEntry("xl/workbook.xml", workbookXml(sheets))
            zip.writeEntry("xl/_rels/workbook.xml.rels", workbookRelationshipsXml(sheets.size))
            zip.writeEntry("xl/styles.xml", stylesXml())

            sheets.forEachIndexed { index, sheet ->
                zip.writeEntry(
                    "xl/worksheets/sheet${index + 1}.xml",
                    worksheetXml(sheet.rows)
                )
            }
        }

        return output.toByteArray()
    }

    private fun ZipOutputStream.writeEntry(
        path: String,
        content: String
    ) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun contentTypesXml(
        sheetCount: Int
    ): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
        append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
        append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
        append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
        append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")

        for (i in 1..sheetCount) {
            append("<Override PartName=\"/xl/worksheets/sheet$i.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
        }

        append("</Types>")
    }

    private fun rootRelationshipsXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""

    private fun workbookXml(
        sheets: List<Sheet>
    ): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets>")

        sheets.forEachIndexed { index, sheet ->
            append("<sheet name=\"")
            append(xmlEscapeAttribute(sheet.name.take(31)))
            append("\" sheetId=\"")
            append(index + 1)
            append("\" r:id=\"rId")
            append(index + 1)
            append("\"/>")
        }

        append("</sheets>")
        append("<calcPr calcId=\"191029\" fullCalcOnLoad=\"1\" forceFullCalc=\"1\"/>")
        append("</workbook>")
    }

    private fun workbookRelationshipsXml(
        sheetCount: Int
    ): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")

        for (i in 1..sheetCount) {
            append("<Relationship Id=\"rId$i\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$i.xml\"/>")
        }

        append("<Relationship Id=\"rId${sheetCount + 1}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>")
        append("</Relationships>")
    }

    private fun stylesXml(): String =
        """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FF008C89"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="2"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/></cellXfs></styleSheet>"""

    private fun worksheetXml(
        rows: List<List<Cell>>
    ): String = buildString {
        val maxColumns =
            rows.maxOfOrNull { it.size }
                ?: 0

        val widths =
            (0 until maxColumns).map { columnIndex ->
                val maxLength =
                    rows
                        .asSequence()
                        .mapNotNull { row -> row.getOrNull(columnIndex) }
                        .map { cell ->
                            when (cell) {
                                is Cell.Text -> cell.value.length
                                is Cell.Number -> cell.value.toString().length
                                is Cell.Formula -> cell.displayValue.length
                            }
                        }
                        .maxOrNull()
                        ?: 0

                (maxLength + 2).coerceIn(10, 40)
            }

        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        append("<sheetViews><sheetView workbookViewId=\"0\"><pane ySplit=\"1\" topLeftCell=\"A2\" activePane=\"bottomLeft\" state=\"frozen\"/></sheetView></sheetViews>")

        if (widths.isNotEmpty()) {
            append("<cols>")

            widths.forEachIndexed { index, width ->
                val column =
                    index + 1

                append("<col min=\"$column\" max=\"$column\" width=\"$width\" customWidth=\"1\"/>")
            }

            append("</cols>")
        }

        append("<sheetData>")

        rows.forEachIndexed { rowIndex, row ->
            val excelRow =
                rowIndex + 1

            val height =
                if (rowIndex == 0) {
                    " ht=\"24\" customHeight=\"1\""
                } else {
                    ""
                }

            append("<row r=\"$excelRow\"$height>")

            row.forEachIndexed { columnIndex, cell ->
                val ref =
                    "${columnName(columnIndex + 1)}$excelRow"

                val style =
                    if (rowIndex == 0) {
                        " s=\"1\""
                    } else {
                        ""
                    }

                when (cell) {
                    is Cell.Text -> {
                        append("<c r=\"$ref\" t=\"inlineStr\"$style><is><t xml:space=\"preserve\">")
                        append(xmlEscapeText(cell.value))
                        append("</t></is></c>")
                    }

                    is Cell.Number -> {
                        append("<c r=\"$ref\"$style><v>${cell.value}</v></c>")
                    }

                    is Cell.Formula -> {
                        append("<c r=\"$ref\" t=\"str\"$style><f>")
                        append(xmlEscapeText(cell.formula))
                        append("</f><v>")
                        append(xmlEscapeText(cell.displayValue))
                        append("</v></c>")
                    }
                }
            }

            append("</row>")
        }

        append("</sheetData>")

        if (rows.isNotEmpty() && maxColumns > 0) {
            append("<autoFilter ref=\"A1:${columnName(maxColumns)}${rows.size}\"/>")
        }

        append("</worksheet>")
    }

    private fun columnName(
        index: Int
    ): String {
        var value = index
        val result = StringBuilder()

        while (value > 0) {
            val remainder =
                (value - 1) % 26

            result.append(
                ('A'.code + remainder).toChar()
            )

            value =
                (value - 1) / 26
        }

        return result.reverse().toString()
    }

    private fun formatDateTime(
        value: LocalDateTime?
    ): String =
        value?.format(dateTimeFormatter).orEmpty()

    private fun formatDate(
        value: LocalDateTime?
    ): String =
        value?.format(dateFormatter).orEmpty()

    private fun xmlEscapeText(
        value: String
    ): String =
        sanitizeXml(value)
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private fun xmlEscapeAttribute(
        value: String
    ): String =
        xmlEscapeText(value)
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

    private fun sanitizeXml(
        value: String
    ): String =
        value.filter { char ->
            char == '\t' ||
                    char == '\n' ||
                    char == '\r' ||
                    char.code >= 0x20
        }

    private data class Sheet(
        val name: String,
        val rows: List<List<Cell>>
    )

    private sealed interface Cell {
        data class Text(
            val value: String
        ) : Cell

        data class Number(
            val value: Double
        ) : Cell

        data class Formula(
            val formula: String,
            val displayValue: String
        ) : Cell
    }

    private data class Labels(
        val summarySheet: String,
        val missionsSheet: String,
        val participantsSheet: String,
        val encountersSheet: String,
        val materialsSheet: String,
        val surveySheet: String,

        val metric: String,
        val value: String,
        val missions: String,
        val uniqueMissionaries: String,
        val participations: String,
        val groups: String,
        val encounters: String,
        val visits: String,
        val prayers: String,
        val otherActivities: String,
        val offeredBibleStudies: String,
        val acceptedBibleStudies: String,
        val materials: String,
        val surveys: String,
        val distanceKilometers: String,
        val missionaryTime: String,

        val mission: String,
        val missionDate: String,
        val scheduledAt: String,
        val startedAt: String,
        val endedAt: String,
        val status: String,
        val participatingChurches: String,
        val operationalGroups: String,
        val description: String,

        val missionary: String,
        val group: String,
        val generalGroup: String,
        val support: String,
        val joinedAt: String,
        val endedByMission: String,

        val person: String,
        val ageGroup: String,
        val phone: String,
        val address: String,
        val atHome: String,
        val mapsLocation: String,
        val openMaps: String,
        val visit: String,
        val prayer: String,
        val otherActivity: String,
        val bibleStudy: String,
        val surveyCompleted: String,
        val followUp: String,
        val materialQuantity: String,
        val notes: String,

        val material: String,
        val quantity: String,

        val question: String,
        val questionType: String,
        val answer: String,

        val yes: String,
        val no: String,

        val participantActive: String,
        val participantNeedsSupport: String,
        val participantFinished: String,

        val ageChild: String,
        val ageTeenager: String,
        val ageYouth: String,
        val ageAdult: String,
        val ageElderly: String,

        val bibleStudyNotOffered: String,
        val bibleStudyOffered: String,
        val bibleStudyAccepted: String,

        val materialBook: String,
        val materialMagazine: String,
        val materialLeaflet: String,
        val materialOther: String,

        val questionText: String,
        val questionSingleChoice: String
    ) {

        fun booleanLabel(
            value: Boolean
        ): String =
            if (value) yes else no

        fun participantStatusLabel(
            status: MissionParticipantStatus
        ): String =
            when (status) {
                MissionParticipantStatus.ACTIVE -> participantActive
                MissionParticipantStatus.NEEDS_SUPPORT -> participantNeedsSupport
                MissionParticipantStatus.FINISHED -> participantFinished
            }

        fun ageGroupLabel(
            ageGroup: EncounterAgeGroup
        ): String =
            when (ageGroup) {
                EncounterAgeGroup.CHILD -> ageChild
                EncounterAgeGroup.TEENAGER -> ageTeenager
                EncounterAgeGroup.YOUTH -> ageYouth
                EncounterAgeGroup.ADULT -> ageAdult
                EncounterAgeGroup.ELDERLY -> ageElderly
            }

        fun bibleStudyStatusLabel(
            status: BibleStudyStatus
        ): String =
            when (status) {
                BibleStudyStatus.NOT_OFFERED -> bibleStudyNotOffered
                BibleStudyStatus.OFFERED -> bibleStudyOffered
                BibleStudyStatus.ACCEPTED -> bibleStudyAccepted
            }

        fun materialLabel(
            type: MissionMaterialType,
            customName: String?
        ): String =
            when (type) {
                MissionMaterialType.BOOK -> materialBook
                MissionMaterialType.MAGAZINE -> materialMagazine
                MissionMaterialType.LEAFLET -> materialLeaflet
                MissionMaterialType.OTHER ->
                    customName
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: materialOther
            }

        fun questionTypeLabel(
            type: SurveyQuestionType
        ): String =
            when (type) {
                SurveyQuestionType.TEXT -> questionText
                SurveyQuestionType.SINGLE_CHOICE -> questionSingleChoice
            }

        companion object {
            fun forLanguage(
                language: String
            ): Labels =
                when (language.lowercase()) {
                    "en" -> english()
                    "es" -> spanish()
                    else -> portuguese()
                }

            private fun portuguese() = Labels(
                summarySheet = "Resumo",
                missionsSheet = "Missões",
                participantsSheet = "Participantes",
                encountersSheet = "Encontros",
                materialsSheet = "Materiais",
                surveySheet = "Pesquisa",

                metric = "Métrica",
                value = "Valor",
                missions = "Missões",
                uniqueMissionaries = "Missionários únicos",
                participations = "Participações",
                groups = "Grupos",
                encounters = "Encontros",
                visits = "Visitas",
                prayers = "Orações",
                otherActivities = "Outras atividades",
                offeredBibleStudies = "Estudos oferecidos",
                acceptedBibleStudies = "Estudos aceitos",
                materials = "Materiais entregues",
                surveys = "Pesquisas concluídas",
                distanceKilometers = "Distância (km)",
                missionaryTime = "Tempo missionário",

                mission = "Missão",
                missionDate = "Data da missão",
                scheduledAt = "Agendada em",
                startedAt = "Iniciada em",
                endedAt = "Encerrada em",
                status = "Status",
                participatingChurches = "Igrejas participantes",
                operationalGroups = "Grupos operacionais",
                description = "Descrição",

                missionary = "Missionário",
                group = "Grupo",
                generalGroup = "Grupo Geral",
                support = "Apoio",
                joinedAt = "Entrada",
                endedByMission = "Encerrado pela missão",

                person = "Pessoa",
                ageGroup = "Faixa etária",
                phone = "Telefone",
                address = "Endereço",
                atHome = "Na casa da pessoa",
                mapsLocation = "Localização (Maps)",
                openMaps = "Abrir no Maps",
                visit = "Visita",
                prayer = "Oração",
                otherActivity = "Outra atividade",
                bibleStudy = "Estudo bíblico",
                surveyCompleted = "Pesquisa concluída",
                followUp = "Aceitou acompanhamento",
                materialQuantity = "Quantidade de materiais",
                notes = "Observações",

                material = "Material",
                quantity = "Quantidade",

                question = "Pergunta",
                questionType = "Tipo da pergunta",
                answer = "Resposta",

                yes = "Sim",
                no = "Não",

                participantActive = "Ativo",
                participantNeedsSupport = "Precisa de apoio",
                participantFinished = "Finalizado",

                ageChild = "Criança",
                ageTeenager = "Adolescente",
                ageYouth = "Jovem",
                ageAdult = "Adulto",
                ageElderly = "Idoso",

                bibleStudyNotOffered = "Não oferecido",
                bibleStudyOffered = "Oferecido",
                bibleStudyAccepted = "Aceito",

                materialBook = "Livro",
                materialMagazine = "Revista",
                materialLeaflet = "Panfleto",
                materialOther = "Outro",

                questionText = "Texto",
                questionSingleChoice = "Escolha única"
            )

            private fun english() = Labels(
                summarySheet = "Summary",
                missionsSheet = "Missions",
                participantsSheet = "Participants",
                encountersSheet = "Encounters",
                materialsSheet = "Materials",
                surveySheet = "Survey",

                metric = "Metric",
                value = "Value",
                missions = "Missions",
                uniqueMissionaries = "Unique missionaries",
                participations = "Participations",
                groups = "Groups",
                encounters = "Encounters",
                visits = "Visits",
                prayers = "Prayers",
                otherActivities = "Other activities",
                offeredBibleStudies = "Bible studies offered",
                acceptedBibleStudies = "Bible studies accepted",
                materials = "Materials delivered",
                surveys = "Surveys completed",
                distanceKilometers = "Distance (km)",
                missionaryTime = "Missionary time",

                mission = "Mission",
                missionDate = "Mission date",
                scheduledAt = "Scheduled at",
                startedAt = "Started at",
                endedAt = "Ended at",
                status = "Status",
                participatingChurches = "Participating churches",
                operationalGroups = "Operational groups",
                description = "Description",

                missionary = "Missionary",
                group = "Group",
                generalGroup = "General Group",
                support = "Support",
                joinedAt = "Joined at",
                endedByMission = "Ended by mission",

                person = "Person",
                ageGroup = "Age group",
                phone = "Phone",
                address = "Address",
                atHome = "At person's home",
                mapsLocation = "Location (Maps)",
                openMaps = "Open in Maps",
                visit = "Visit",
                prayer = "Prayer",
                otherActivity = "Other activity",
                bibleStudy = "Bible study",
                surveyCompleted = "Survey completed",
                followUp = "Accepted follow-up",
                materialQuantity = "Material quantity",
                notes = "Notes",

                material = "Material",
                quantity = "Quantity",

                question = "Question",
                questionType = "Question type",
                answer = "Answer",

                yes = "Yes",
                no = "No",

                participantActive = "Active",
                participantNeedsSupport = "Needs support",
                participantFinished = "Finished",

                ageChild = "Child",
                ageTeenager = "Teenager",
                ageYouth = "Youth",
                ageAdult = "Adult",
                ageElderly = "Elderly",

                bibleStudyNotOffered = "Not offered",
                bibleStudyOffered = "Offered",
                bibleStudyAccepted = "Accepted",

                materialBook = "Book",
                materialMagazine = "Magazine",
                materialLeaflet = "Leaflet",
                materialOther = "Other",

                questionText = "Text",
                questionSingleChoice = "Single choice"
            )

            private fun spanish() = Labels(
                summarySheet = "Resumen",
                missionsSheet = "Misiones",
                participantsSheet = "Participantes",
                encountersSheet = "Encuentros",
                materialsSheet = "Materiales",
                surveySheet = "Encuesta",

                metric = "Métrica",
                value = "Valor",
                missions = "Misiones",
                uniqueMissionaries = "Misioneros únicos",
                participations = "Participaciones",
                groups = "Grupos",
                encounters = "Encuentros",
                visits = "Visitas",
                prayers = "Oraciones",
                otherActivities = "Otras actividades",
                offeredBibleStudies = "Estudios ofrecidos",
                acceptedBibleStudies = "Estudios aceptados",
                materials = "Materiales entregados",
                surveys = "Encuestas completadas",
                distanceKilometers = "Distancia (km)",
                missionaryTime = "Tiempo misionero",

                mission = "Misión",
                missionDate = "Fecha de la misión",
                scheduledAt = "Programada en",
                startedAt = "Iniciada en",
                endedAt = "Finalizada en",
                status = "Estado",
                participatingChurches = "Iglesias participantes",
                operationalGroups = "Grupos operativos",
                description = "Descripción",

                missionary = "Misionero",
                group = "Grupo",
                generalGroup = "Grupo General",
                support = "Apoyo",
                joinedAt = "Entrada",
                endedByMission = "Finalizado por la misión",

                person = "Persona",
                ageGroup = "Grupo de edad",
                phone = "Teléfono",
                address = "Dirección",
                atHome = "En casa de la persona",
                mapsLocation = "Ubicación (Maps)",
                openMaps = "Abrir en Maps",
                visit = "Visita",
                prayer = "Oración",
                otherActivity = "Otra actividad",
                bibleStudy = "Estudio bíblico",
                surveyCompleted = "Encuesta completada",
                followUp = "Aceptó seguimiento",
                materialQuantity = "Cantidad de materiales",
                notes = "Observaciones",

                material = "Material",
                quantity = "Cantidad",

                question = "Pregunta",
                questionType = "Tipo de pregunta",
                answer = "Respuesta",

                yes = "Sí",
                no = "No",

                participantActive = "Activo",
                participantNeedsSupport = "Necesita apoyo",
                participantFinished = "Finalizado",

                ageChild = "Niño",
                ageTeenager = "Adolescente",
                ageYouth = "Joven",
                ageAdult = "Adulto",
                ageElderly = "Adulto mayor",

                bibleStudyNotOffered = "No ofrecido",
                bibleStudyOffered = "Ofrecido",
                bibleStudyAccepted = "Aceptado",

                materialBook = "Libro",
                materialMagazine = "Revista",
                materialLeaflet = "Folleto",
                materialOther = "Otro",

                questionText = "Texto",
                questionSingleChoice = "Opción única"
            )
        }
    }
}
