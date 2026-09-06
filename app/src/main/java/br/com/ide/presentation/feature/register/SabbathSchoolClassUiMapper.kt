package br.com.ide.presentation.feature.register

import br.com.ide.R
import br.com.ide.domain.model.SabbathSchoolClass

fun SabbathSchoolClass.toStringRes(): Int {
    return when (this) {
        SabbathSchoolClass.JUVENIS ->
            R.string.sabbath_school_juveniles

        SabbathSchoolClass.JOVENS ->
            R.string.sabbath_school_youth

        SabbathSchoolClass.ADULTOS ->
            R.string.sabbath_school_adults
    }
}