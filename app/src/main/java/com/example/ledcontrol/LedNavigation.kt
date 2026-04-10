package com.example.ledcontrol

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LedNavigation(viewModel: LedViewModel, sendBluetoothData: (List<LedState>) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = DeviceScreen){
        composable<DeviceScreen>{
            DeviceScreen(viewModel= viewModel){
                navController.navigate(LedScreen)
            }
        }
        composable<LedScreen> {
            LedControlScreen(viewModel=viewModel, onNavigateToCameraScreen = {
                navController.navigate(CameraScreen)
            }, sendBluetoothData = {
                viewModel.ledStates
                sendBluetoothData(viewModel.ledStates)
            })
        }
        composable<CameraScreen> {

            CameraScreen( ledViewModel = viewModel)
        }

    }
}