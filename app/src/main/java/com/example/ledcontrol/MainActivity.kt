package com.example.ledcontrol

import android.bluetooth.BluetoothSocket
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.asLiveData
import com.example.ledcontrol.ui.theme.LedControlTheme
import java.io.IOException
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    private var lastGesture: String? = null
    private val gestureToCommandMap = mapOf(
        "Thumb_Up" to "turn on all lights",
        "Thumb_Down" to "turn off all lights",
        "Open_Palm" to "set all blue",
        "Closed_Fist" to "set all red",
        "Pointing_Up" to "set all yellow",
        "Victory" to "set all green",
        "ILoveYou" to "set all purple",
    )
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: LedViewModel by viewModels()
        val results = viewModel.gestureResults.asLiveData()
        enableEdgeToEdge()
        setContent {
            LedControlTheme {
                results.observeForever { result ->
                    val gesture = result?.results?.firstOrNull()?.gestures()?.firstOrNull()?.get(0)?.categoryName()
                    Log.d(GestureRecognizerHelper.TAG, "Detected gesture: $gesture")

                    if (gesture != null && gesture != lastGesture) {
                        lastGesture = gesture
                        val command = gestureToCommandMap[gesture]

                        if (command != null) {
                            viewModel.translateCommand(command).let { success ->
                                if (success) {
                                    sendBluetoothData(viewModel.bluetoothSocket.value, viewModel.ledStates)
                                    Toast.makeText(this, "$command detected", Toast.LENGTH_SHORT).show()

                                }
                            }
                        } else {
                            Log.d(GestureRecognizerHelper.TAG, "No command mapped for gesture: $gesture")
                        }
                    } else if (gesture == lastGesture) {
                        Log.d(GestureRecognizerHelper.TAG, "Ignoring repeated gesture: $gesture")
                    }
                }
//                if (viewModel.gestureResults != null) {
//                    Log.d(GestureRecognizerHelper.TAG, "onCreate: gesture results= ${
//                        viewModel.gestureResults.collectAsState().value?.results?.firstOrNull()?.gestures()?.firstOrNull()?.get(0)?.categoryName()}")
//                }
                LedNavigation(viewModel){
                        sendBluetoothData(viewModel.bluetoothSocket.value, viewModel.ledStates) // Replace 'null' with a valid socket when available
                }

            }
        }
    }

    @OptIn(ExperimentalUnsignedTypes::class)
    fun sendBluetoothData(socket: BluetoothSocket?, ledStates: List<LedState>) {
        val packet = makePacket(ledStates[0], ledStates[1], ledStates[2])
        val outputStream: OutputStream? = socket?.outputStream
        packet.forEach { step ->

            val byteToSend = step.packet
            try {
                byteToSend.createBytes()
                outputStream!!.write(
                    byteArrayOf(
                        76.toByte(),
                        (byteToSend.ledNumber + 48u).toByte()
                    )
                )
                outputStream.write(byteToSend.color.asByteArray()
                    .onEach {
                        (it.toInt() and 0xFF)
                    })


                Log.d("Send Data: ", byteToSend.createBytes().toString())
            } catch (e: IOException) {

                Log.e("Send Error", "Unable to send message", e)
            }
//                delay(40)
        }

    }
}

