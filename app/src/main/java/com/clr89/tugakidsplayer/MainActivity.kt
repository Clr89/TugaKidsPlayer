package com.clr89.tugakidsplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.clr89.tugakidsplayer.ui.catalog.CatalogScreen
import com.clr89.tugakidsplayer.ui.theme.TugaKidsPlayerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TugaKidsPlayerTheme {
                CatalogScreen()
            }
        }
    }
}
