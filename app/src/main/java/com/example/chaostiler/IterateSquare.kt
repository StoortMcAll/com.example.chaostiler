package com.example.chaostiler

// region Variable Declaration

import com.example.chaostiler.MainActivity.Companion.mainCounter
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.sin

val parentJob = Job()
val coroutineScope = CoroutineScope(
    Dispatchers.Default + parentJob)

var maxCount = 7500

// endregion

class SquareValues{
    // region Variable Declaration

    var x : Double
    var y : Double
    val alpha : Double
    val beta : Double
    val gamma : Double
    val lambda : Double
    val ma : Double
    val omega : Double

    // endregion

    constructor(x: Double, y: Double, alpha: Double, beta: Double,
            gamma: Double, lambda: Double, ma: Double, omega: Double) {
        this.x = x; this.y = y
        this.alpha = alpha; this.beta = beta
        this.gamma = gamma; this.lambda = lambda
        this.ma = ma; this.omega = omega
    }

    constructor(randLevel : Int) {
        x = 0.001 + MainActivity.rand.nextDouble() * 0.998
        y = 0.001 + MainActivity.rand.nextDouble() * 0.998

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
                gamma = getRand(-1.0, 1.0)
                lambda = getRand(-1.0, 1.0)
                ma = getRand(-1.0, 1.0)
                omega = getRand(-1.0, 1.0)
            }
            else->{
                alpha = getRand(-3.0, 3.0)
                beta = getRand(-2.0, 2.0)
                gamma = getRand(-2.0, 2.0)
                lambda = getRand(-2.0, 2.0)
                ma = getRand(-2.0, 2.0)
                omega = getRand(-2.0, 2.0)
            }
        }
    }

    private fun getRand(min : Double, max : Double) : Double {
        var value = MainActivity.rand.nextDouble(min, max)
        if (value == 0.0) value = 0.01

        return value
    }

    inline fun yesNo(): Boolean {
        return MainActivity.rand.nextBoolean()
    }
}

fun runSquare(wide : Int, high : Int, square : SquareValues) :ArrayList<Hit> {

    // region Variable Declaration

    maxCount = 7500

    var hits : ArrayList<Hit> = arrayListOf()

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
        xnew -= xnew.toInt();
        ynew = (ynew - ynew.toInt()) + 1;
        ynew -= ynew.toInt();

        x = xnew; y = ynew;

        hits.add(Hit((x * wide).toInt(), (y * high).toInt()))

    } while (counter++ < maxCount);

    mainCounter += maxCount

    square.x = x; square.y = y;

    return hits
}

