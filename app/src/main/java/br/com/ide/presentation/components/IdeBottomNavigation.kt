package br.com.ide.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import br.com.ide.R

enum class BottomNavigationItem(
    val labelRes: Int,
    val icon: ImageVector
) {
    HOME(
        labelRes = R.string.bottom_nav_home,
        icon = Icons.Outlined.Home
    ),

    METRICS(
        labelRes = R.string.bottom_nav_metrics,
        icon = Icons.Outlined.BarChart
    ),

    PROFILE(
        labelRes = R.string.bottom_nav_profile,
        icon = Icons.Outlined.Person
    )
}

@Composable
fun IdeBottomNavigation(
    selectedItem: BottomNavigationItem,
    onItemSelected: (BottomNavigationItem) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        BottomNavigationItem.entries.forEach { item ->

            val selected = selectedItem == item

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onItemSelected(item)
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(
                            item.labelRes
                        )
                    )
                },
                label = {
                    Text(
                        text = stringResource(
                            item.labelRes
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor =
                        MaterialTheme.colorScheme.primary,

                    selectedTextColor =
                        MaterialTheme.colorScheme.primary,

                    indicatorColor =
                        MaterialTheme.colorScheme.primaryContainer,

                    unselectedIconColor =
                        MaterialTheme.colorScheme.onSurfaceVariant,

                    unselectedTextColor =
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}