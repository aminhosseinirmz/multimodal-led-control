package com.example.ledcontrol

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
//socket is a local state variable to hold the connection.
//When a BluetoothSocket is successfully created (socket != null), it gets sent to the LedViewModel.
//BluetoothConnectionButton() handles scanning and connecting to a device.
//After a successful connection, it triggers onNavigateToLedScreen() to go to the next screen.
@Composable
fun DeviceScreen(viewModel: LedViewModel , onNavigateToLedScreen: () -> Unit) {
    var socket by remember { mutableStateOf<BluetoothSocket?>(null) }

    LaunchedEffect(socket) {
        if (socket != null) {
            viewModel.updateBluetoothSocket(socket)
        }
    }
    Column {
        Text("Select your device", modifier = Modifier
            .padding(top = 32.dp, start = 16.dp),
            style = MaterialTheme.typography.headlineLarge)
        BluetoothConnectionButton(onConnectionResult = { newSocket ->
            socket = newSocket
            viewModel.updateBluetoothSocket(newSocket)
            onNavigateToLedScreen()
        })
    }
}

/**
 * Composable function to display a list of paired Bluetooth devices and connect to a selected device.
 *
 * @param onConnectionResult Callback function to handle the connection result (BluetoothSocket or null).
 */
@Composable
fun BluetoothConnectionButton(onConnectionResult: (BluetoothSocket?) -> Unit) {
    val context = LocalContext.current
    val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // State to hold the list of paired devices.
    var pairedDevices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }

    // State to track if Bluetooth permission is granted.
    var hasBluetoothPermission by remember { mutableStateOf(false) }

    // State to hold the selected device.
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    var socket by remember { mutableStateOf<BluetoothSocket?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Determine the required permissions based on the Android version.
    val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }

    // Check if all required permissions are granted.
    fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request permissions if not granted
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasBluetoothPermission = permissions.all { it.value }
    }

    // Check permissions and request if needed
    hasBluetoothPermission = checkPermissions()
    LaunchedEffect(Unit) {
        if (!hasBluetoothPermission) {
            launcher.launch(requiredPermissions)
        }
    }

    // Fetch paired devices when the composable is first launched or when permission changes.
    LaunchedEffect(hasBluetoothPermission) {
        if (hasBluetoothPermission) {
            pairedDevices = bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } else {
            pairedDevices = emptyList()
        }
    }

    Column(modifier = Modifier.padding(32.dp)) {
        // Display buttons for each paired device.
        pairedDevices.forEach { device ->
            Button(onClick = {
                selectedDevice = device
                coroutineScope.launch {
                    // Connect to the selected device and handle the result.
                    socket = connectToBluetoothDevice(device, context)

                    onConnectionResult(socket)
                }
            }) {
                Text("Connect to ${device.name}")
            }
        }
    }
}

/**
 * Function to establish a Bluetooth connection with a given device (typically an Arduino).
 *
 * @param device The Bluetooth device to connect to.
 * @param context The application context.
 * @return The BluetoothSocket if the connection is successful, null otherwise.
 */
suspend fun connectToBluetoothDevice(
    device: BluetoothDevice,
    context: Context
): BluetoothSocket? = withContext(Dispatchers.IO) {
    var socket: BluetoothSocket? = null
    try {
        // Check for Bluetooth permission before attempting to connect.
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            )
        }
        if (requiredPermissions.all {
                ActivityCompat.checkSelfPermission(
                    context,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }
        ) {
            val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SPP UUID
            socket = device.createRfcommSocketToServiceRecord(device.uuids[0].uuid)
            Log.d("BluetoothConnection", "Attempting to connect to ${device.name}...")
            socket.connect()
            Log.d("BluetoothConnection", "Connected to ${device.name}")
        } else {
            Log.e("BluetoothConnection", "Bluetooth permission not granted")
        }
    } catch (e: IOException) {
        Log.e("BluetoothConnection", "Error connecting to device ${device.name}", e)
        if (e.message?.contains("read failed") == true) {
            Log.e(
                "BluetoothConnection",
                "Possible causes: Device not ready, incorrect UUID, connection refused, interference, pairing issues."
            )
        }
        try {
            socket?.close()
        } catch (closeException: IOException) {
            Log.e("BluetoothConnection", "Error closing socket", closeException)
        }
        return@withContext null
    } catch (e: SecurityException) {
        Log.e(
            "BluetoothConnection",
            "Security exception connecting to device ${device.name}",
            e
        )
        return@withContext null
    }
    return@withContext socket
}