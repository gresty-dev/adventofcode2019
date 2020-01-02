package com.cgresty.advent2019

import kotlin.math.absoluteValue

data class Point2(val x: Int, val y: Int)
operator fun Point2.plus(c: Point2) = Point2(x + c.x, y + c.y)
operator fun Point2.minus(c: Point2) = Point2(x - c.x, y - c.y)

data class Point3(val x: Int, val y: Int, val z: Int)
operator fun Point3.plus(c: Point3) = Point3(x + c.x, y + c.y, z + c.z)
operator fun Point3.minus(c: Point3) = Point3(x - c.x, y - c.y, z - c.z)
operator fun Point3.times(s: Int) = Point3(s * x, s * y, s * z)
fun Point3.apply(func: (Int) -> Int) = Point3(func(x), func(y), func(z))
fun Point3.energy() = x.absoluteValue + y.absoluteValue + z.absoluteValue
fun Point3.state() = "$x,$y,$z"

data class Polar(val r: Double, val d: Double)

//  Euclid
fun gcd(x: Long, y: Long): Long {
    val xx = x.absoluteValue
    val yy = y.absoluteValue

    return when {
        xx == yy -> xx
        xx > yy -> gcd(xx - yy, yy)
        xx < yy -> gcd(xx, yy - xx)
        else -> throw RuntimeException("No GCD for $xx and $yy")
    }
}

// Extended Stein
fun gcd(vararg nn: Long) : Long {
    var n = nn
    var doubler = 1

    do {
        n = n.filter { it != 0L }.toLongArray()
        if (allEven(n)) {
            for (i in 0.until(n.size)) n[i] /= 2L
            doubler *= 2
        }
        if (!allEven(n) && !allOdd(n)) {
            for (i in 0.until(n.size)) n[i] = if (n[i] % 2 == 0L) n[i] / 2L else n[i]
        }
        if (allOdd(n)) {
            val k = n.min()!!
            val ik = n.indexOf(k)
            for (i in 0.until(n.size)) n[i] = if (i == ik) n[i] else (n[i] - k) / 2L
        }
    }
    while (n.size > 1)
    return n[0] * doubler
}

fun allEven(n: LongArray) = n.all { it % 2 == 0L }
fun allOdd(n: LongArray) = n.all { it % 2 == 1L }

fun lcm(a: Long, b: Long) = a * b / gcd(a, b)
fun lcm(a: Long, b: Long, c: Long) = a * b * c / gcd(a * b, a * c, b * c)