package com.example.ledcontrol

import android.bluetooth.BluetoothSocket
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color.argb
import android.os.Build
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import io.mhssn.colorpicker.ColorPickerDialog
import io.mhssn.colorpicker.ColorPickerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

// ViewModel to hold LED states
class LedViewModel : ViewModel() {

    private val _gestureResults = MutableStateFlow<GestureRecognizerHelper.ResultBundle?>(null)
    val gestureResults: StateFlow<GestureRecognizerHelper.ResultBundle?> =
        _gestureResults.asStateFlow()

    fun updateGestureResults(resultBundle: GestureRecognizerHelper.ResultBundle) {
        _gestureResults.value = resultBundle
    }

    // StateFlow to hold the Bluetooth socket
    private val _bluetoothSocket = MutableStateFlow<BluetoothSocket?>(null)
    val bluetoothSocket: StateFlow<BluetoothSocket?> = _bluetoothSocket.asStateFlow()

    // Function to update the Bluetooth socket
    fun updateBluetoothSocket(socket: BluetoothSocket?) {
        _bluetoothSocket.value = socket
    }

    var ledStates by mutableStateOf(
        listOf(
            LedState(1), // Corrected initial values
            LedState(2),
            LedState(3)
        )
    )
        private set

    fun updateLedState(ledNumber: Int, color: Color) {
        ledStates = ledStates.map { ledState ->
            if (ledState.ledNumber == ledNumber) {
                ledState.copy(
                    alpha = color.alpha.toInt(),
                    colorR = (color.red*255).toInt(),
                    colorG = (color.green*255).toInt(),
                    colorB = (color.blue*255).toInt()
                )
            } else {
                ledState
            }
        }
    }

    fun translateCommand(voiceCommand: String):Boolean {
        when {

            voiceCommand.lowercase().contains("turn on all lights") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 255
                    it.colorG = 255
                    it.colorB = 255
                }
                return true
            }
            voiceCommand.lowercase().contains("set all blue") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 0
                    it.colorG = 0
                    it.colorB = 255
                }
                return true
            }
            voiceCommand.lowercase().contains("set all red") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 255
                    it.colorG = 0
                    it.colorB = 0
                }
                return true
            }
            voiceCommand.lowercase().contains("set all yellow") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 255
                    it.colorG = 255
                    it.colorB = 0
                }
                return true
            }
            voiceCommand.lowercase().contains("set all green") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 0
                    it.colorG = 255
                    it.colorB = 0
                }
                return true
            }
            voiceCommand.lowercase().contains("set all purple") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 160
                    it.colorG = 32
                    it.colorB = 240
                }
                return true
            }
            voiceCommand.lowercase().contains("set all pink") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 255
                    it.colorG = 192
                    it.colorB = 203
                }
                return true
            }
            voiceCommand.lowercase().contains("set all orange") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 255
                    it.colorG = 165
                    it.colorB = 0
                }
                return true
            }
            voiceCommand.lowercase().contains("set all cyan") -> {
                ledStates.forEach {
                    it.alpha = 255
                    it.colorR = 0
                    it.colorG = 255
                    it.colorB = 255
                }
                return true
            }

            voiceCommand.lowercase().contains("turn off all lights") -> {
                ledStates.forEach {
                    it.alpha = 0
                    it.colorR = 0
                    it.colorG = 0
                    it.colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("turn on light one") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("turn on light two") -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("turn on light three") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("turn off light one") -> {
                ledStates[0].apply {
                    alpha = 0
                    colorR = 0
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("turn off light two") -> {
                ledStates[1].apply {
                    alpha = 0
                    colorR = 0
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("turn off light three") -> {
                ledStates[2].apply {
                    alpha = 0
                    colorR = 0
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light one red") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light one blue") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 0
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light one green") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light one yellow") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light one pink") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 122
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light one white") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light two red") || voiceCommand.lowercase()
                .contains("light to red") -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light two blue") || voiceCommand.lowercase()
                .contains("light to blue")
                -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 0
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light two green") || voiceCommand.lowercase()
                .contains("light to green") -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light two yellow") || voiceCommand.lowercase()
                .contains("light to yellow") -> {
                ledStates[0].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light two pink") || voiceCommand.lowercase()
                .contains("light to pink") -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 122
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light two white") || voiceCommand.lowercase()
                .contains("light to white") -> {
                ledStates[1].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light three red") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 0
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light three blue") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 0
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light three green") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 0
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light three yellow") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 0
                }
                return true

            }

            voiceCommand.lowercase().contains("light three pink") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 122
                    colorB = 255
                }
                return true

            }

            voiceCommand.lowercase().contains("light three white") -> {
                ledStates[2].apply {
                    alpha = 255
                    colorR = 255
                    colorG = 255
                    colorB = 255
                }
                return true

            }

            else -> {
                return false
            }
        }
    }


}


// Data class to represent LED states
data class LedState(
    var ledNumber: Int = 0,
    var alpha: Int = 0,
    var colorR: Int = 0,
    var colorG: Int = 0,
    var colorB: Int = 0,

    )

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LedControlScreen(
    onNavigateToCameraScreen: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: LedViewModel,
    sendBluetoothData: (List<LedState>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) } // State to control dialog visibility

    var selectedLed by remember { mutableStateOf(0) } // Initial color
    var selectedColor1 by remember { mutableStateOf(Color(argb(0,0,0,0))) } // Initial color
    var selectedColor2 by remember { mutableStateOf(Color(argb(0,0,0,0))) } // Initial color
    var selectedColor3 by remember { mutableStateOf(Color(argb(0,0,0,0))) } // Initial color
    val gestureResult by viewModel.gestureResults.collectAsState()
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(32.dp)
            .fillMaxSize()
    ) {
        Text("Smart Home Panel", modifier = Modifier
            .padding(top = 32.dp, start = 16.dp),
            style = MaterialTheme.typography.headlineLarge)
        LedButtons(viewModel.ledStates) { ledNumber,alpha, red, green, blue ->
            viewModel.updateLedState(ledNumber, Color(alpha = alpha, red = red, green = green, blue = blue))
            sendBluetoothData(viewModel.ledStates)
        }
        viewModel.ledStates.forEach { led ->
            LedControl(led){alpha, red, green, blue ->
                selectedLed= led.ledNumber
                when (selectedLed) {
                    1->selectedColor1= Color(alpha, red, green, blue)
                    2->selectedColor2= Color(alpha, red, green, blue)
                    3->selectedColor3= Color(alpha, red, green, blue)
                }
                viewModel.updateLedState(led.ledNumber, Color(alpha = alpha, red = red, green = green, blue = blue))
                sendBluetoothData(viewModel.ledStates)
            }
        }
        Row(modifier = Modifier.
        padding(top = 16.dp)
            .fillMaxWidth().align(Alignment.Start)
        ) {
            VoiceControlButton { voiceCommand ->
                Log.d(TAG, "voice command: $voiceCommand")
                viewModel.translateCommand(voiceCommand).let {
                    if (it) {
                        sendBluetoothData(viewModel.ledStates)
                    }
                }
                //            sendBluetoothData(viewModel.ledStates)
            }
            IconButton(onClick = onNavigateToCameraScreen) {
                Icon(modifier = Modifier.width(100.dp).height(100.dp), painter = painterResource(
                    R.drawable.camera_svgrepo_com
                ), contentDescription = "Camera")
            }
        }
        // Button to trigger the dialog
        Button(modifier = Modifier.padding(top=64.dp), onClick = { showDialog = true }) {
            Text("Guide")
        }
        // Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false }, // Close when tapped outside
                title = { Text("Commands") },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 200.dp) // Limits max height
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(stringResource(R.string.guide))
                    }
                },
                confirmButton = {
                    Button(onClick = { showDialog = false }) {
                        Text("Ok")
                    }
                }
            )
        }
        if (gestureResult != null) {
            gestureResult?.results?.firstOrNull()?.gestures()?.firstOrNull()?.get(0)
                ?.categoryName()?.let { command ->

                sendBluetoothData(viewModel.ledStates)
            }
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LedButtons(leds: List<LedState>, onColorChange: (Int,Int, Int, Int, Int) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedLed by remember { mutableStateOf(0) } // Initial led
    Row(modifier = Modifier.padding(top = 32.dp)) {
        // Display the color picker dialog
        if (showDialog) {
            ColorPickerDialog(
//                controller = controller,
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(),
                onPickedColor = { color: Color ->
                    when (selectedLed) {
                        1->{
                            leds[0].alpha = (color.alpha*255).toInt()
                            leds[0].colorR = (color.red*255).toInt()
                            leds[0].colorG = (color.green*255).toInt()
                            leds[0].colorB = (color.blue*255).toInt()
                        }
                        2->{
                            leds[1].alpha = (color.alpha*255).toInt()
                            leds[1].colorR = (color.red*255).toInt()
                            leds[1].colorG = (color.green*255).toInt()
                            leds[1].colorB = (color.blue*255).toInt()
                        }
                        3->{
                            leds[2].alpha = (color.alpha*255).toInt()
                            leds[2].colorR = (color.red*255).toInt()
                            leds[2].colorG = (color.green*255).toInt()
                            leds[2].colorB = (color.blue*255).toInt()
                        }
                    }
                    showDialog = false
                    onColorChange(selectedLed,
                        (color.alpha * 255).toInt(),
                        (color.red * 255).toInt(),
                        (color.green * 255).toInt(),
                        (color.blue * 255).toInt()
                    )
                },
                type = ColorPickerType.Classic(false),
                show = showDialog, // or other types
            )
        }

        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
                , onClick = {
                selectedLed = 1
                showDialog = true
                      }, colors = ButtonColors(
                containerColor = Color(red = leds[0].colorR, green = leds[0].colorG, blue = leds[0].colorB),
                contentColor = Color.White,
                disabledContainerColor = Color.Black,
                disabledContentColor = Color.Black
            )

        ){
          Text(text = "LED 1",color = Color.Gray)
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
            , onClick = {
                selectedLed = 2
                showDialog = true
            }, colors = ButtonColors(
                containerColor = Color(red = leds[1].colorR, green = leds[1].colorG, blue = leds[1].colorB),
                contentColor = Color.White,
                disabledContainerColor = Color.Black,
                disabledContentColor = Color.Black
            )

        ){
            Text(text = "LED 2",color = Color.Gray)
        }
        Button(
            modifier = Modifier
                .weight(1f)
                .padding(4.dp)
                .clip(RoundedCornerShape(16.dp))
            , onClick = {
                selectedLed = 3
                showDialog = true
            }, colors = ButtonColors(
                containerColor = Color(red = leds[2].colorR, green = leds[2].colorG, blue = leds[2].colorB),
                contentColor = Color.White,
                disabledContainerColor = Color.Black,
                disabledContentColor = Color.Black
            )

        ){
            Text(text = "LED 3",color = Color.Gray)
        }
    }
}

@Composable
fun LedControl(led: LedState, onColorChange: (Int, Int, Int, Int) -> Unit) {

//    val controller = rememberColorPickerController()

    Row(modifier = Modifier
        .fillMaxWidth().background(color = Color.Gray)
        .padding(8.dp)
    ) {
        Text(
            text = "LED ${led.ledNumber}",
            modifier = Modifier
                .weight(1.5f),
            color = Color.Black,// Show dialog on click
        )


        // Example buttons (optional, can be removed if only using dialog)
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 255, 0, 0)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {}
        Spacer(modifier = Modifier.width(2.dp))
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 0, 255, 0)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
        ) {}
        Spacer(modifier = Modifier.width(2.dp))
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 0, 0, 255)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
        ) {}
        Spacer(modifier = Modifier.width(2.dp))
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 255, 255, 0)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
        ) {}
        Spacer(modifier = Modifier.width(2.dp))
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 255, 0, 255)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Magenta)
        ) {}
        Spacer(modifier = Modifier.width(2.dp))
        Button(modifier = Modifier.weight(1F),
            onClick = {
                onColorChange(255, 255, 255, 255)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {}
    }
}
@Composable
fun VoiceControlButton(onVoiceCommand: (String) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val spokenText =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            spokenText?.let { onVoiceCommand(it) }
        }
    IconButton( onClick = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        launcher.launch(intent)
    },) {
        Icon(painter = painterResource(R.drawable.mic_svgrepo_com), contentDescription = "Voice Command")
    }
}


// Define a constant for the UUID
private val MY_UUID: UUID =
//    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard SerialPortService ID
    UUID.fromString("04A204E9-20F2-11E8-8A95-8C164545A6DE") // Standard SerialPortService ID
