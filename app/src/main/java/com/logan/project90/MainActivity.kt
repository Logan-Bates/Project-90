package com.logan.project90

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.logan.project90.navigation.AppNavGraph
import com.logan.project90.ui.theme.Project90Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as Project90Application).appContainer
        setContent {
            Project90Theme {
                AppNavGraph(appContainer = container)
            }
        }
    }
}
