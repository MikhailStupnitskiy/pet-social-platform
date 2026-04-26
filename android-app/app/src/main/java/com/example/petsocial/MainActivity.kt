package com.example.petsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.petsocial.feature.auth.AuthRoute
import com.example.petsocial.ui.theme.PetSocialPlatformTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PetSocialPlatformTheme {
                val isLoggedIn = remember {
                    mutableStateOf(false)
                }

                if (isLoggedIn.value) {
                    Text("Main screen")
                } else {
                    AuthRoute(
                        onAuthSuccess = {
                            isLoggedIn.value = true
                        }
                    )
                }
            }
        }
    }
}