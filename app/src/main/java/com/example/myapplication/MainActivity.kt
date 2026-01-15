package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.myapplication.navigation.MovieNavigation
import com.example.myapplication.ui.theme.ProjTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}


@Composable
fun MyApp() {
    ProjTheme {
        MovieNavigation()
    }
}