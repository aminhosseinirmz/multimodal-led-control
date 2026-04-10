package com.example.ledcontrol.bluetoothUtil

import java.util.ArrayList

class Step(val duration: Int, val packet : Packet)
class Steps : ArrayList<Step>()
{
    fun addStep(duration: Int, ledNumber : UByte,color1 : Int,
                color2 : Int, color3 : Int )
    {
        this.add(
            Step(duration, Packet(ledNumber,color1,color2,color3))
        )
    }

    fun addStep(step : Step)
    {
        this.add(step)
    }
}
