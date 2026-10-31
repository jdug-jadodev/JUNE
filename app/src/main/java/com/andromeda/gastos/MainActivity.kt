package com.andromeda.gastos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.andromeda.gastos.infrastructure.ui.navigation.AndromedaNavHost
import com.andromeda.gastos.infrastructure.ui.theme.AndromedaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as AndromedaApp).container

        setContent {
            AndromedaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AndromedaNavHost(container)
                }
            }
        }
    }
}
