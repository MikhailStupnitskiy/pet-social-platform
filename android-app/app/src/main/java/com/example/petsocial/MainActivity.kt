package com.example.petsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.petsocial.navigation.PetSocialAppRoot
import com.example.petsocial.ui.theme.PetSocialPlatformTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PetSocialPlatformTheme {
                PetSocialAppRoot()
            }
        }
    }
}