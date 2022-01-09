package com.fractal.tiler

import kotlin.math.sin
import kotlin.math.sqrt

val sq3 = sqrt(3.0)

class HexValues @JvmOverloads constructor(squareVals: SquareValues, randomLevel: Int = 0,
    sk11: Double = 1.0, sk12: Double = 0.0, sk21: Double = 0.5,
    sel11: Double = 1.0, sel21: Double = 0.0)
{
    // region Variable Declaration

    val square : SquareValues = squareVals

    var k11 : Double = 1.0; var k12 : Double = 0.0; var k21 : Double = 0.5; val k22 : Double

    var el11 : Double = 1.0; val el12 : Double; var el21 : Double = 0.0; val el22 : Double
    val el31 : Double; val el32 : Double

    val em11 : Double; val em12 : Double; val em21 : Double; val em22 : Double
    val em31 : Double; val em32 : Double

    val en11 : Double; val en12 : Double; val en21 : Double; val en22 : Double
    val en31 : Double; val en32 : Double

    val enh11 : Double; val enh12 : Double; val enh21 : Double; val enh22 : Double
    val enh31 : Double; val enh32 : Double

    val a11 : Double; val a12 : Double; val a21 : Double; val a22 : Double
    val a31 : Double; val a32 : Double

    val ah11 : Double; val ah12 : Double; val ah21 : Double; val ah22 : Double
    val ah31 : Double; val ah32 : Double

    // endregion

    init {
        k11 = sk11 //if (randomLevel == 0 && square.headOrTails() == false) sk11 else square.getRand(0.0, 1.0)
        k12 = sk12 //if (randomLevel == 0 && square.headOrTails() == false) sk12 else square.getRand(0.0, 1.0)
        k21 = sk21 //if (randomLevel == 0 && square.headOrTails() == false) sk21 else square.getRand(0.0, 1.0)
        el11 = sel11 //if (randomLevel == 0 && square.headOrTails() == false) sel11 else square.getRand(0.0, 1.0)
        el21 = sel21 //if (randomLevel == 0 && square.headOrTails() == false) sel21 else square.getRand(0.0, 1.0)

        val mult = if (square.headOrTails() == true)  1.0 else 2.0
        if (square.headOrTails()) square.alpha += square.getRand(-0.5, 0.5) * mult
        if (square.headOrTails()) square.beta = square.getRand(-0.5, 0.5) * mult
        if (square.headOrTails()) square.gamma = square.getRand(-0.25, 0.25) * mult
        if (square.headOrTails()) square.delta = square.getRand(-0.5, 0.5) * mult
        if (square.headOrTails()) square.ma = square.getRand(-0.25, 0.25) * mult
        if (square.headOrTails()) square.omega = square.getRand(-0.5, 0.5) * mult
        if (square.headOrTails()) square.shift = square.getRand(-1.0, 1.0) * mult
        k22 = sq3 / 2.0
        el12 = -1.0 / sq3
        el22 = 2.0 / sq3
        el31 = -(el11 + el21)
        el32 = -(el12 + el22)
        em11 = (2.0 * el11) + el21
        em12 = (2.0 * el12) + el22
        em21 = (2.0 * el21) + el31
        em22 = (2.0 * el22) + el32
        em31 = (2.0 * el31) + el11
        em32 = (2.0 * el32) + el12
        en11 = (3.0 * el11) + (2.0 * el21)
        en12 = (3.0 * el12) + (2.0 * el22)
        en21 = (3.0 * el21) + (2.0 * el31)
        en22 = (3.0 * el22) + (2.0 * el32)
        en31 = (3.0 * el31) + (2.0 * el11)
        en32 = (3.0 * el32) + (2.0 * el12)
        enh11 = (3.0 * el11) + el21
        enh12 = (3.0 * el12) + el22
        enh21 = (3.0 * el21) + el31
        enh22 = (3.0 * el22) + el32
        enh31 = (3.0 * el31) + el11
        enh32 = (3.0 * el32) + el12
        a11 = square.beta
        a12 = square.gamma
        a21 = (-a11 - (sq3 * a12)) / 2.0
        a22 = ((sq3 * a11) - a12) / 2.0
        a31 = -a11 - a21
        a32 = -a12 - a22
        ah11 = a11
        ah12 = -a12
        ah21 = (-ah11 - (sq3 * ah12)) / 2.0
        ah22 = ((sq3 * ah11) - ah12) / 2.0
        ah31 = -ah11 - ah21
        ah32 = -ah12 - ah22
    }
}

fun runHexagon(wide : Int, high : Int, hexagon : HexValues) :ArrayList<Hit> {

    // region Variable Declaration

    maxCount.also { maxCounter = it }

    val hits: ArrayList<Hit> = arrayListOf()

    val hex = hexagon.square

    var bx: Double; var by : Double
    var xnew = hex.x; var ynew = hex.y

    var s11 : Double; var s12 : Double; var s13 : Double
    var s21 : Double; var s22 : Double; var s23 : Double
    var s31 : Double; var s32 : Double; var s33 : Double
    var s3h1 : Double; var s3h2 : Double; var s3h3 : Double


    var counter = 0

    val p2 = 2.0 * Math.PI

    // endregion

    do {
        s11 = sin(p2 * (hexagon.el11 * xnew + hexagon.el12 * ynew))
        s12 = sin(p2 * (hexagon.el21 * xnew + hexagon.el22 * ynew))
        s13 = sin(p2 * (hexagon.el31 * xnew + hexagon.el32 * ynew))
        s21 = sin(p2 * (hexagon.em11 * xnew + hexagon.em12 * ynew))
        s22 = sin(p2 * (hexagon.em21 * xnew + hexagon.em22 * ynew))
        s23 = sin(p2 * (hexagon.em31 * xnew + hexagon.em32 * ynew))
        s31 = sin(p2 * (hexagon.en11 * xnew + hexagon.en12 * ynew))
        s32 = sin(p2 * (hexagon.en21 * xnew + hexagon.en22 * ynew))
        s33 = sin(p2 * (hexagon.en31 * xnew + hexagon.en32 * ynew))
        s3h1 = sin(p2 * (hexagon.enh11 * xnew + hexagon.enh12 * ynew))
        s3h2 = sin(p2 * (hexagon.enh21 * xnew + hexagon.enh22 * ynew))
        s3h3 = sin(p2 * (hexagon.enh31 * xnew + hexagon.enh32 * ynew))

        val sx = (hexagon.el11 * s11 + hexagon.el21 * s12 + hexagon.el31 * s13)
        val sy = (hexagon.el12 * s11 + hexagon.el22 * s12 + hexagon.el32 * s13)

        xnew = hex.ma * xnew + hex.lambda * sx - hex.omega * sy
        ynew = hex.ma * ynew + hex.lambda * sy + hex.omega * sx
        xnew = xnew + (hex.alpha * (hexagon.em11 * s21 + hexagon.em21 * s22 + hexagon.em31 * s23))
        ynew = ynew + (hex.alpha * (hexagon.em12 * s21 + hexagon.em22 * s22 + hexagon.em32 * s23))
        xnew = xnew +  (hexagon.a11 * s31 + hexagon.a21 * s32 + hexagon.a31 * s33)
        ynew = ynew + (hexagon.a12 * s31 + hexagon.a22 * s32 + hexagon.a32 * s33)
        xnew = xnew + (hexagon.ah11 * s3h1 + hexagon.ah21 * s3h2 + hexagon.ah31 * s3h3)
        ynew = ynew + (hexagon.ah12 * s3h1 + hexagon.ah22 * s3h2 + hexagon.ah32 * s3h3)

        by = 2.0 * ynew / sq3; bx = xnew - (by / 2.0)

        bx = (bx - bx.toInt()) + 1
        bx -= bx.toInt()
        by = (by - by.toInt()) + 1
        by -= by.toInt()

        xnew = bx * hexagon.k11 + by * hexagon.k21
        ynew = bx * hexagon.k12 + by * hexagon.k22

        hits.add(Hit((bx * wide).toInt(), (by * high).toInt()))

    } while (++counter < maxCounter)

    mainCounter += maxCounter

    hexagon.square.x = xnew; hexagon.square.y = ynew

    return hits
}