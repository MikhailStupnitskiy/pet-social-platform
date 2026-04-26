package com.example.petsocial

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.petsocial.feature.auth.AuthRoute
import com.example.petsocial.ui.theme.PetSocialPlatformTheme
import com.example.petsocial.feature.profile.ProfileRoute
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.petsocial.feature.pets.PetsRoute
import com.example.petsocial.feature.matching.MatchingRoute

import dagger.hilt.android.AndroidEntryPoint

private enum class MainTab {
    Profile,
    Pets,
    Matching
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PetSocialPlatformTheme {
                val sessionViewModel: SessionViewModel = hiltViewModel()
                val sessionState by sessionViewModel.uiState.collectAsState()

                when (sessionState) {
                    SessionUiState.Loading,
                    SessionUiState.LoggingOut -> {
                        LoadingScreen()
                    }

                    SessionUiState.AuthRequired -> {
                        AuthRoute(
                            onAuthSuccess = {
                                sessionViewModel.onAuthSuccess()
                            }
                        )
                    }

                    SessionUiState.Authorized -> {
                        val mainTab = remember {
                            mutableStateOf(MainTab.Profile)
                        }

                        when (mainTab.value) {
                            MainTab.Profile -> {
                                ProfileRoute(
                                    onPetsClick = {
                                        mainTab.value = MainTab.Pets
                                    },
                                    onMatchingClick = {
                                        mainTab.value = MainTab.Matching
                                    },
                                    onLogoutClick = {
                                        sessionViewModel.logout()
                                    }
                                )
                            }

                            MainTab.Pets -> {
                                PetsRoute(
                                    onBackClick = {
                                        mainTab.value = MainTab.Profile
                                    }
                                )
                            }

                            MainTab.Matching -> {
                                MatchingRoute(
                                    onBackClick = {
                                        mainTab.value = MainTab.Profile
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@androidx.compose.runtime.Composable
private fun MainScreen(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Main screen")

        Button(
            onClick = onLogoutClick
        ) {
            Text("Выйти")
        }
    }
}