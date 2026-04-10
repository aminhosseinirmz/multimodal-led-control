# Multimodal LED Control

An Android–Arduino system for controlling RGB LEDs using touch, voice commands, and hand gestures over Bluetooth.

## Overview

This project is a multimodal LED control system built with Android and Arduino. The Android app allows the user to control three RGB LEDs through three different interaction modes:

- Touch-based control through a Jetpack Compose interface
- Voice commands using Android speech recognition
- Hand gestures using MediaPipe and CameraX

The app sends LED state data to an Arduino board through Bluetooth, and the Arduino updates the LEDs in real time.

## Features

- Control 3 RGB LEDs from an Android app
- Manual color selection with buttons and color picker
- Voice commands for turning lights on/off and changing colors
- Gesture recognition mapped to LED commands
- Bluetooth communication with Arduino
- Real-time LED state updates

## Tech Stack

### Android
- Kotlin
- Jetpack Compose
- Android Bluetooth API
- Google Speech Recognition
- MediaPipe Gesture Recognizer
- CameraX
- MVVM-style state handling

### Hardware
- Arduino Uno
- HC-06 Bluetooth module
- 3 RGB LEDs
- Resistors
- Breadboard

## Project Structure

- `MainActivity.kt` - app entry point, gesture-to-command mapping, Bluetooth sending
- `LedController.kt` - LED state logic, voice command handling, main control UI
- `DeviceScreen.kt` - paired Bluetooth device selection and connection
- `CameraScreen.kt` - camera preview and gesture recognition pipeline
- `GestureRecognizerHelper.kt` - MediaPipe gesture recognizer helper
- `Utils.kt`, `Packet.kt`, `Step.kt` - Bluetooth packet creation and command formatting

## How It Works

1. The user selects a Bluetooth device from the Android app.
2. The app connects to the Arduino through a Bluetooth socket.
3. The user controls LEDs using touch, voice, or gestures.
4. The app converts LED states into byte packets.
5. The packets are sent to Arduino over Bluetooth.
6. Arduino reads the packet and updates LED colors using PWM.

## Gesture Commands

Examples of gesture mappings used in the app:

- `Thumb_Up` → turn on all lights
- `Thumb_Down` → turn off all lights
- `Open_Palm` → set all blue
- `Closed_Fist` → set all red
- `Pointing_Up` → set all yellow
- `Victory` → set all green
- `ILoveYou` → set all purple

## Voice Commands

Examples:
- `turn on all lights`
- `turn off all lights`
- `light one blue`
- `light two red`
- `light three green`
- `set all yellow`

## Requirements

### Software
- Android Studio
- Android device with Bluetooth
- Minimum Android version supported by the project setup
- MediaPipe gesture model asset

### Hardware
- Arduino Uno
- HC-06 Bluetooth module
- RGB LEDs and resistors

## Important Note

The gesture recognition system depends on the MediaPipe task file:

`gesture_recognizer.task`

Make sure it is placed in the correct Android assets location, otherwise gesture recognition will not work.

## Arduino Side

The Arduino receives LED packets over serial Bluetooth and controls three RGB LEDs. Two LEDs use hardware PWM pins directly, and the third LED is handled with a software-based PWM loop in the Arduino code.

## Future Improvements

- Better UI polish
- More gesture commands
- Custom voice command definitions
- Scheduling and timer features
- Improved Bluetooth reliability handling

## Author

Seyedamin Hosseini
