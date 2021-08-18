package com.fractal.tiler

import com.fractal.tiler.MainActivity.Companion.rand
import kotlin.math.*

data class DIconTrig(var c : DoubleArray, var s: DoubleArray)

class IconValues(squareVals: SquareValues, trigdepth: Int) {
    val square: SquareValues = squareVals

    var trigDepth = 4

    lateinit var dIconTrig : DIconTrig

    init {
        if (square.headOrTails()) square.alpha += square.getRand(-0.25, 0.25)
        if (square.headOrTails()) square.beta += square.getRand(-0.25, 0.25)
        if (square.headOrTails()) square.delta += square.getRand(0.0, 0.5)
        if (square.headOrTails()) square.lambda += square.getRand(-0.25, 0.25)
        setTrigArrays(trigdepth)
    }

    fun setTrigArrays(trigdepth : Int) {
        trigDepth = trigdepth
        if (trigDepth < 3) trigDepth = 3

        dIconTrig = DIconTrig(DoubleArray(trigDepth), DoubleArray(trigDepth))

        val arc = 1.0 / trigDepth

        for (i in 0 until trigDepth) {
            dIconTrig.c[i] = cos(2.0 * PI * i * arc)
            dIconTrig.s[i] = sin(2.0 * PI * i * arc)
        }
    }

    fun boundsTest(): Boolean{
        val sqr = square

        val a1 = sqr.alpha * sqr.alpha + sqr.gamma * sqr.gamma
        val a2 = sqr.beta * sqr.beta + sqr.lambda * sqr.lambda
        val a3: Double = sqr.alpha * sqr.lambda - sqr.beta * sqr.gamma

        if (a1 > 1.0 || a2 > 1.0 || (a1 * a2) > 1.0 - a3.pow(2.0)) return false

        return true
    }
}

fun randomizeIconValues(){
    val trigDepth = rand.nextInt(3, 31)

    icon.setTrigArrays(trigDepth)

    val sqr = icon.square
    do{
        if (rand.nextBoolean()) sqr.alpha = rand.nextDouble(-1.0, 1.0)
        if (rand.nextBoolean()) sqr.beta = rand.nextDouble(-1.0, 1.0)
        if (rand.nextBoolean()) sqr.gamma = rand.nextDouble(-1.0, 1.0)
        if (rand.nextBoolean()) sqr.lambda = rand.nextDouble(-1.0, 1.0)
        if (rand.nextBoolean()) sqr.omega = rand.nextDouble(-1.0, 1.0)

    }while(icon.boundsTest())

    if (rand.nextBoolean()) sqr.x = rand.nextDouble(0.0, 1.0)
    if (rand.nextBoolean()) sqr.y = rand.nextDouble(0.0, 1.0)
}

fun initIcon() : IconValues {
    val sqr = SquareValues(0.1, -0.1, 0.3, 0.65, 0.43, 0.4, 0.0, 0.9)

    return IconValues(sqr, 24)
}

fun runIcon(wide : Int, high : Int, icon : IconValues) :ArrayList<Hit> {

    // region Variable Declaration

    maxCount.also { maxCounter = it }

    val hits: ArrayList<Hit> = arrayListOf()

    val sqr = icon.square

    val iconTrig = icon.dIconTrig
    val trigDepth = icon.trigDepth
    var x1 : Double
    var y1 : Double
    var xnew : Double
    var ynew : Double
    var x = sqr.x
    var y = sqr.y

    var serpoint : Int

    var counter = 0

    // endregion

    do {
        xnew = sqr.alpha * x + sqr.beta * y + sqr.ma
        ynew = sqr.gamma * x + sqr.lambda * y + sqr.omega

        serpoint = rand.nextInt(trigDepth)
        x1 = xnew; y1 = ynew

        xnew = iconTrig.c[serpoint] * x1 - iconTrig.s[serpoint] * y1
        ynew = iconTrig.s[serpoint] * x1 - iconTrig.c[serpoint] * y1

        x = xnew; y = ynew

        xnew = (xnew - xnew.toInt()) + 1
        xnew -= xnew.toInt()
        ynew = (ynew - ynew.toLong()) + 1
        ynew -= ynew.toInt()

        hits.add(Hit((xnew * wide).toInt(), (ynew * high).toInt()))

    } while (++counter < maxCounter)

    mainCounter += maxCounter

    icon.square.x = x; icon.square.y = y

    return hits

}