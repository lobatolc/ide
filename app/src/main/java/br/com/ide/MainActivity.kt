package br.com.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.com.ide.presentation.theme.IdeTheme
import br.com.ide.presentation.feature.home.HomeScreen
import androidx.navigation.compose.rememberNavController
import br.com.ide.presentation.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IdeTheme {

                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController
                )
            }
        }
    }
}

