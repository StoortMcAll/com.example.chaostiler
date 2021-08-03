package com.example.chaostiler

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
    var y : Double
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

    constructor(randLevel : Int) {
        x = 0.001 + MainActivity.rand.nextDouble() * 0.998
        y = 0.001 + MainActivity.rand.nextDouble() * 0.998

        shift = getRand(-1.0, 1.0)
        delta = getRand(-1.0, 1.0)

        when (randLevel){
            0->{
                alpha = getRand(-1.0, 1.0)
                beta = getRand(-1.0, 1.0)
                gamma = getRand(-1.0, 1.0)
                lambda = getRand(-1.0, 1.0)
                ma = getRand(-1.0, 1.0)
                omega = getRand(-1.0, 1.0)
            }
            1->{
                alpha = getRand(-2.0, 2.0)
                beta = getRand(-2.0, 2.0)
                gamma = getRand(-2.0, 2.0)
                lambda = getRand(-2.0, 2.0)
                ma = getRand(-2.0, 2.0)
                omega = getRand(-2.0, 2.0)
            }
            else->{
                alpha = getRand(-4.0, 4.0)
                beta = getRand(-4.0, 4.0)
                gamma = getRand(-4.0, 4.0)
                lambda = getRand(-4.0, 4.0)
                ma = getRand(-4.0, 4.0)
                omega = getRand(-4.0, 4.0)
            }
        }
    }

    private fun getRand(min : Double, max : Double) : Double {
        var value = MainActivity.rand.nextDouble(min, max)
        if (value == 0.0) value = 0.01

        return value
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
        xnew += square.beta * sin(2 * p2x)
        xnew += square.gamma * sin(3 * p2x) * cos(2 * p2y)
        xnew += square.ma * x

        ynew = (square.lambda + square.alpha * cos(p2x)) * sy
        ynew += square.omega * sx
        ynew += square.beta * sin(2 * p2y)
        ynew += square.gamma * sin(3 * p2y) * cos(2 * p2x)
        ynew += square.ma * y

        xnew = (xnew - xnew.toInt()) + 1
        xnew -= xnew.toInt()
        ynew = (ynew - ynew.toInt()) + 1
        ynew -= ynew.toInt()

        x = xnew; y = ynew

        hits.add(Hit((x * wide).toInt(), (y * high).toInt()))

    } while (++counter < maxCounter)

    mainCounter += maxCounter

    square.x = x; square.y = y

    return hits
}

