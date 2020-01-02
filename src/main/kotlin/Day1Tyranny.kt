package com.cgresty.advent2019.day1

import java.io.File
import kotlin.math.floor

fun main() {
    var totalNaive = 0
    File("day01.in")
        .forEachLine{ line -> totalNaive += calcFuel(line.toInt())}
    println("Total mass: $totalNaive")

    var total = 0
    File("day01.in")
        .forEachLine{ line -> total += calcFuelProperly(0, line.toInt())}
    println("Total mass (calculated properly): $total")

}

fun calcFuel(mass: Int) = (floor(mass / 3.0) - 2).toInt()

fun calcFuelProperly(accumulator: Int, mass: Int) : Int {
    val fuelForMass = calcFuel(mass)
    return if (fuelForMass <= 0) accumulator
    else calcFuelProperly(accumulator + fuelForMass, fuelForMass)
}