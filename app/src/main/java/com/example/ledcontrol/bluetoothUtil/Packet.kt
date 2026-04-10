package com.example.ledcontrol.bluetoothUtil

import java.nio.ByteBuffer
import java.nio.ByteOrder

@OptIn(ExperimentalUnsignedTypes::class)
class Packet @OptIn(ExperimentalUnsignedTypes::class) constructor(
    var ledNumber: UByte,
    colorR: Int,
    colorG: Int,
    colorB: Int,


    ){
    var color: UByteArray = UByteArray(3)

    init {
        color[0] = colorR.toUByte()
        color[1] = colorG.toUByte()
        color[2] = colorB.toUByte()

    }
    fun createBytes( ): ByteArray? {
        val buffer: ByteBuffer = ByteBuffer.allocate(15).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(76)
        buffer.put((ledNumber+ 48u).toByte())

        for(a in color) {
            buffer.put(a.toByte())
        }

        return buffer.array()
    }
}