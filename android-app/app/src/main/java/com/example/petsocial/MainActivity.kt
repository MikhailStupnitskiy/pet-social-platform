package com.example.petsocial

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.lifecycle.lifecycleScope
import com.example.petsocial.core.auth.auth.AuthApi
import com.example.petsocial.core.auth.model.LoginRequest
import com.example.petsocial.core.datastore.auth.TokenStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authApi: AuthApi

    @Inject
    lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            try {
                val loginResponse = authApi.login(
                    LoginRequest(
                        email = "test@example.com",
                        password = "123456"
                    )
                )

                tokenStorage.saveToken(loginResponse.token)

                Log.d("API", "Token saved: ${loginResponse.token}")

                val meResponse = authApi.me()

                Log.d("API", "Me: $meResponse")
            } catch (e: Exception) {
                Log.e("API", "Error", e)
            }
        }

        setContent {
            Text("Check logs")
        }
    }
}