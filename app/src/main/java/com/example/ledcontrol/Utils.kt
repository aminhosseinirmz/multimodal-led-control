package com.example.ledcontrol

import com.example.ledcontrol.bluetoothUtil.Packet
import com.example.ledcontrol.bluetoothUtil.Step
import com.example.ledcontrol.bluetoothUtil.Steps

fun makePacket(led1: LedState, led2: LedState, led3: LedState): Steps {
    val packet1 = Packet(
        ledNumber = led1.ledNumber.toUByte(),
        led1.colorR,
        led1.colorG,
        led1.colorB

    )
    val packet2 = Packet(
        ledNumber = led2.ledNumber.toUByte(),
        led2.colorR,
        led2.colorG,
        led2.colorB
    )
    val packet3 = Packet(
        ledNumber = led3.ledNumber.toUByte(),
        led3.colorR,
       led3.colorG,
       led3.colorB
    )

    // Get the appropriate information and send it along
    val packetToSend = Steps()
    val step1 = Step(
        0, packet1
    )
    val step2 = Step(
        0, packet2
    )
    val step3 = Step(
        0, packet3
    )

    packetToSend.addStep(step1)
    packetToSend.addStep(step2)
    packetToSend.addStep(step3)

    return packetToSend
}