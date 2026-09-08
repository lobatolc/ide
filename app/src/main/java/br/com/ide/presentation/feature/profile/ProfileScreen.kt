package br.com.ide.presentation.feature.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.ide.R
import br.com.ide.presentation.components.BottomNavigationItem
import br.com.ide.presentation.components.IdeBottomNavigation
import br.com.ide.presentation.components.IdePrimaryButton
import br.com.ide.presentation.components.IdeScreenSubtitle
import br.com.ide.presentation.components.IdeScreenTitle
import br.com.ide.presentation.feature.register.toStringRes as sabbathClassToStringRes
import br.com.ide.presentation.mapper.toStringRes as userRoleToStringRes
import br.com.ide.presentation.model.AppLanguage
import br.com.ide.presentation.model.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    appLanguage: AppLanguage,
    onLanguageChanged: (AppLanguage) -> Unit,
    appTheme: AppTheme,
    onThemeChanged: (AppTheme) -> Unit,
    onHomeClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by
    viewModel.uiState.collectAsStateWithLifecycle()

    var showLanguageSheet by remember {
        mutableStateOf(false)
    }

    var showThemeSheet by remember {
        mutableStateOf(false)
    }

    val languageSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    val themeSheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    ProfileContent(
        uiState = uiState,
        appLanguage = appLanguage,
        appTheme = appTheme,
        onHomeClick = onHomeClick,
        onMetricsClick = onMetricsClick,
        onEditProfileClick = onEditProfileClick,
        onLanguageClick = {
            showLanguageSheet = true
        },
        onThemeClick = {
            showThemeSheet = true
        },
        onNotificationsClick = onNotificationsClick,
        onLogout = onLogout
    )

    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showLanguageSheet = false
            },
            sheetState = languageSheetState
        ) {
            LanguageBottomSheet(
                selectedLanguage = appLanguage,
                onLanguageSelected = { language ->
                    onLanguageChanged(language)
                    showLanguageSheet = false
                }
            )
        }
    }

    if (showThemeSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showThemeSheet = false
            },
            sheetState = themeSheetState
        ) {
            ThemeBottomSheet(
                selectedTheme = appTheme,
                onThemeSelected = { theme ->
                    onThemeChanged(theme)
                    showThemeSheet = false
                }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    appLanguage: AppLanguage,
    appTheme: AppTheme,
    onHomeClick: () -> Unit,
    onMetricsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onThemeClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        containerColor =
            MaterialTheme.colorScheme.background,

        bottomBar = {
            IdeBottomNavigation(
                selectedItem =
                    BottomNavigationItem.PROFILE,

                onItemSelected = { item ->
                    when (item) {

                        BottomNavigationItem.HOME -> {
                            onHomeClick()
                        }

                        BottomNavigationItem.METRICS -> {
                            onMetricsClick()
                        }

                        BottomNavigationItem.PROFILE -> {
                            Unit
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment =
                    Alignment.Center
            ) {
                CircularProgressIndicator(
                    color =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }

            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 20.dp,
                    end = 12.dp,
                    top = 20.dp,
                    bottom = 24.dp
                ),
            verticalArrangement =
                Arrangement.Top
        ) {

            IdeScreenTitle(
                text = stringResource(
                    R.string.profile_title
                )
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            IdeScreenSubtitle(
                text = stringResource(
                    R.string
                        .profile_subtitle_overview
                )
            )

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            ProfileHeaderCard(
                firstName =
                    uiState.firstName,
                lastName =
                    uiState.lastName,
                email =
                    uiState.email,
                onEditClick =
                    onEditProfileClick
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            ProfileSection(
                title = stringResource(
                    R.string.profile_information
                )
            ) {

                ProfileOptionRow(
                    icon =
                        Icons.Outlined.Groups,

                    title = stringResource(
                        R.string
                            .register_sabbath_school_class
                    ),

                    subtitle =
                        uiState
                            .sabbathSchoolClass
                            ?.let {
                                stringResource(
                                    it.sabbathClassToStringRes()
                                )
                            }
                            .orEmpty(),

                    showChevron = false
                )

                ProfileDivider()

                ProfileOptionRow(
                    icon =
                        Icons.Outlined.Person,

                    title = stringResource(
                        R.string.profile_permission
                    ),

                    subtitle = stringResource(
                        uiState.role
                            .userRoleToStringRes()
                    ),

                    showChevron = false
                )
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            ProfileSection(
                title = stringResource(
                    R.string.profile_settings
                )
            ) {

                ProfileOptionRow(
                    icon =
                        Icons.Outlined.Language,

                    title = stringResource(
                        R.string.profile_language
                    ),

                    subtitle =
                        when (appLanguage) {

                            AppLanguage.PORTUGUESE ->
                                "Português (Brasil)"

                            AppLanguage.ENGLISH ->
                                "English"

                            AppLanguage.SPANISH ->
                                "Español"
                        },

                    onClick =
                        onLanguageClick
                )

                ProfileDivider()

                ProfileOptionRow(
                    icon =
                        Icons.Outlined
                            .SettingsBrightness,

                    title = stringResource(
                        R.string.profile_theme
                    ),

                    subtitle =
                        when (appTheme) {

                            AppTheme.SYSTEM ->
                                stringResource(
                                    R.string
                                        .profile_theme_system
                                )

                            AppTheme.LIGHT ->
                                stringResource(
                                    R.string
                                        .profile_theme_light
                                )

                            AppTheme.DARK ->
                                stringResource(
                                    R.string
                                        .profile_theme_dark
                                )
                        },

                    onClick =
                        onThemeClick
                )

                ProfileDivider()

                ProfileOptionRow(
                    icon =
                        Icons.Outlined.Notifications,

                    title = stringResource(
                        R.string
                            .profile_notifications
                    ),

                    subtitle = stringResource(
                        R.string
                            .profile_notifications_enabled
                    ),

                    onClick =
                        onNotificationsClick
                )
            }

            uiState.errorMessage?.let { errorMessage ->

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                Text(
                    text = stringResource(
                        errorMessage
                    ),
                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium
                )
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            IdePrimaryButton(
                text = stringResource(
                    R.string.profile_logout
                ),
                onClick = onLogout,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    firstName: String,
    lastName: String,
    email: String,
    onEditClick: () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            MaterialTheme.shapes.large,
        color =
            MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {

        Row(
            modifier =
                Modifier.padding(16.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .surfaceVariant
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Person,
                        contentDescription = null,
                        modifier =
                            Modifier.size(34.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.width(14.dp)
            )

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {

                Text(
                    text =
                        "$firstName $lastName"
                            .trim(),
                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurface
                )

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text = email,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }

            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clickable {
                        onEditClick()
                    },
                shape =
                    CircleShape,
                color =
                    MaterialTheme
                        .colorScheme
                        .primaryContainer
            ) {

                Box(
                    contentAlignment =
                        Alignment.Center
                ) {

                    Icon(
                        imageVector =
                            Icons.Outlined.Edit,

                        contentDescription =
                            stringResource(
                                R.string.profile_edit
                            ),

                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),
        shape =
            MaterialTheme.shapes.large,
        color =
            MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {

        Column(
            modifier =
                Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                )
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .titleMedium,
                fontWeight =
                    FontWeight.Bold,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            Spacer(
                modifier =
                    Modifier.height(10.dp)
            )

            content()
        }
    }
}

@Composable
private fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        onClick()
                    }
                } else {
                    Modifier
                }
            )
            .padding(
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Surface(
            modifier =
                Modifier.size(42.dp),
            shape =
                CircleShape,
            color =
                MaterialTheme
                    .colorScheme
                    .primaryContainer
        ) {

            Box(
                contentAlignment =
                    Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint =
                        MaterialTheme
                            .colorScheme
                            .primary
                )
            }
        }

        Spacer(
            modifier =
                Modifier.width(12.dp)
        )

        Column(
            modifier =
                Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface
            )

            if (subtitle.isNotBlank()) {

                Spacer(
                    modifier =
                        Modifier.height(2.dp)
                )

                Text(
                    text = subtitle,
                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant
                )
            }
        }

        if (
            showChevron &&
            onClick != null
        ) {

            Icon(
                imageVector =
                    Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileDivider() {

    HorizontalDivider(
        color =
            MaterialTheme
                .colorScheme
                .outline
                .copy(
                    alpha = 0.25f
                )
    )
}

@Composable
private fun LanguageBottomSheet(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            )
    ) {

        Text(
            text = stringResource(
                R.string.profile_select_language
            ),
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        LanguageOption(
            label = "Português (Brasil)",
            selected =
                selectedLanguage ==
                        AppLanguage.PORTUGUESE,
            onClick = {
                onLanguageSelected(
                    AppLanguage.PORTUGUESE
                )
            }
        )

        ProfileDivider()

        LanguageOption(
            label = "English",
            selected =
                selectedLanguage ==
                        AppLanguage.ENGLISH,
            onClick = {
                onLanguageSelected(
                    AppLanguage.ENGLISH
                )
            }
        )

        ProfileDivider()

        LanguageOption(
            label = "Español",
            selected =
                selectedLanguage ==
                        AppLanguage.SPANISH,
            onClick = {
                onLanguageSelected(
                    AppLanguage.SPANISH
                )
            }
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 14.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier =
                Modifier.weight(1f),
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        if (selected) {

            Icon(
                imageVector =
                    Icons.Outlined.Check,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}

@Composable
private fun ThemeBottomSheet(
    selectedTheme: AppTheme,
    onThemeSelected: (AppTheme) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            )
    ) {

        Text(
            text = stringResource(
                R.string.profile_select_theme
            ),
            style =
                MaterialTheme
                    .typography
                    .titleLarge,
            fontWeight =
                FontWeight.Bold,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        ThemeOption(
            label = stringResource(
                R.string.profile_theme_system
            ),
            selected =
                selectedTheme ==
                        AppTheme.SYSTEM,
            onClick = {
                onThemeSelected(
                    AppTheme.SYSTEM
                )
            }
        )

        ProfileDivider()

        ThemeOption(
            label = stringResource(
                R.string.profile_theme_light
            ),
            selected =
                selectedTheme ==
                        AppTheme.LIGHT,
            onClick = {
                onThemeSelected(
                    AppTheme.LIGHT
                )
            }
        )

        ProfileDivider()

        ThemeOption(
            label = stringResource(
                R.string.profile_theme_dark
            ),
            selected =
                selectedTheme ==
                        AppTheme.DARK,
            onClick = {
                onThemeSelected(
                    AppTheme.DARK
                )
            }
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(
                vertical = 14.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Text(
            text = label,
            modifier =
                Modifier.weight(1f),
            style =
                MaterialTheme
                    .typography
                    .bodyLarge,
            color =
                MaterialTheme
                    .colorScheme
                    .onSurface
        )

        if (selected) {

            Icon(
                imageVector =
                    Icons.Outlined.Check,
                contentDescription = null,
                tint =
                    MaterialTheme
                        .colorScheme
                        .primary
            )
        }
    }
}