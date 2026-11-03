package com.vedatv.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vedatv.app.ui.VedaTvApp
import com.vedatv.app.ui.theme.VedaTvTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VedaTvTheme {
                VedaTvApp()
            }
        }
    }
}
