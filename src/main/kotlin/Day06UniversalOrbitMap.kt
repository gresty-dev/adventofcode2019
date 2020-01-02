package com.cgresty.advent2019.day6

import java.io.File
import java.lang.RuntimeException

val testData = """COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L
K)YOU
I)SAN"""
//val orbits = testData
val orbits = File("day06.in").readText()
    .split("\n")
    .map { it
        .split(")")
        .let { it[1] to it[0] }
    }
    .associateBy({ it.first }, { it.second })

fun main() {
    var count = 0
    orbits.entries.forEach { (node, _) -> count += trace(node) }
    println("Total orbits: $count")

    val youPath = path("YOU")
    val santaPath = path("SAN", youPath)
    val meetAt = santaPath.last()
    val distance = santaPath.indexOf(meetAt) + youPath.indexOf(meetAt)
    println("Orbital transfers: $distance")

}

fun trace(node: String, count: Int = 0): Int {
    val parent = orbits[node] ?: return count
    return trace(parent, count + 1)
}

fun path(from: String, toAnyOf: List<String> = listOf("COM"), pathSoFar: MutableList<String> = ArrayList()) : List<String> {
    val parent = orbits[from] ?: throw RuntimeException("No direct path from $from (which is weird)")
    pathSoFar.add(parent)
    return if (toAnyOf.contains(parent)) pathSoFar else path(parent, toAnyOf, pathSoFar)
}