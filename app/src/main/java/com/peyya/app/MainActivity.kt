package com.peyya.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.peyya.app.ui.PeyyaApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val forceMock = intent?.getBooleanExtra(EXTRA_FORCE_MOCK, false) == true
        setContent {
            PeyyaApp(forceMock = forceMock)
        }
    }

    companion object {
        const val EXTRA_FORCE_MOCK = "force_mock"
    }
}
