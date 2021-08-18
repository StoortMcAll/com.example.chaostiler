package com.fractal.tiler

// region Variable Declaration

import kotlin.math.cos
import kotlin.math.sin

const val maxCount = 5000
var maxCounter = maxCount

var mainCounter = 0

// endregion

class SquareValues{

    // region Variable Declaration

    var x : Double
    var y  : Double
    var alpha : Double
    var beta : Double
    var gamma : Double
    var lambda : Double
    var ma : Double
    var omega : Double
    var shift : Double
    var delta : Double

    // endregion

    constructor(x: Double, y: Double, alpha: Double, beta: Double,
            gamma: Double, lambda: Double, ma: Double, omega: Double,
            shift: Double = 0.0, delta: Double = 0.0) {
        this.x = x; this.y = y
        this.alpha = alpha; this.beta = beta
        this.gamma = gamma; this.lambda = lambda
        this.ma = ma; this.omega = omega

        this.shift = shift
        this.delta = delta
    }

    constructor(square : SquareValues, randLevel : Double) {
        val min = -randLevel

        x = square.x
        y = square.y
        alpha = square.alpha
        beta = square.beta
        gamma = square.omega
        lambda = square.lambda
        ma = square.ma
        omega = square.omega
        delta = square.delta
        shift = square.shift

        if (headOrTails()) alpha += getRand(min, randLevel)
        if (headOrTails()) beta += getRand(min, randLevel)
        if (headOrTails()) gamma += getRand(min, randLevel)
        if (headOrTails()) lambda += getRand(min, randLevel)
        if (headOrTails()) ma += getRand(min, randLevel)
        if (headOrTails()) omega += getRand(min, randLevel)
        if (headOrTails()) delta += getRand(min, randLevel)
    }

    fun getRand(min : Double, max : Double) : Double {
        return MainActivity.rand.nextDouble(min, max)
    }
    fun headOrTails() : Boolean{
        return MainActivity.rand.nextInt(until = 4) == 3
    }
}

fun runSquare(wide : Int, high : Int, square : SquareValues) :ArrayList<Hit> {

    // region Variable Declaration

    maxCount.also { maxCounter = it }

    val hits : ArrayList<Hit> = arrayListOf()

    var xnew: Double
    var ynew: Double
    var x = square.x
    var y = square.y

    var p2x: Double
    var p2y: Double
    var sx: Double
    var sy: Double

    var counter = 0

    val p2 = 2.0 * Math.PI

    // endregion

    do {
        p2x = p2 * x; p2y = p2 * y
        sx = sin(p2x); sy = sin(p2y)

        xnew = (square.lambda + square.alpha * cos(p2y)) * sx
        xnew -= square.omega * sy
        xnew += square.beta * sin(2.0 * p2x)
        xnew += square.gamma * sin(3.0 * p2x) * cos(2 * p2y)
        xnew += square.ma * x

        ynew = (square.lambda + square.alpha * cos(p2x)) * sy
        ynew += square.omega * sx
        ynew += square.beta * sin(2.0 * p2y)
        ynew += square.gamma * sin(3.0 * p2y) * cos(2 * p2x)
        ynew += square.ma * y

        xnew = (xnew - xnew.toLong()) + 1.0
        xnew -= xnew.toInt()
        ynew = (ynew - ynew.toLong()) + 1.0
        ynew -= ynew.toInt()

        x = xnew; y = ynew

        hits.add(Hit((xnew * wide).toInt(), (ynew * high).toInt()))

    } while (++counter < maxCounter)

    mainCounter += maxCounter

    square.x = x; square.y = y

    return hits
}

