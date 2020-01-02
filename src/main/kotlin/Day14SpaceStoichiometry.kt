package com.cgresty.advent2019.day14

import java.io.File
import java.lang.RuntimeException
import kotlin.math.min
import kotlin.math.sign

fun main() {
    part1()
    part2()
}

fun part1() {
    println("For 1 FUEL we need ${getOreFor("FUEL", 1)} ORE")
}

fun part2() {
    val maxOre = 1_000_000_000_000
    val maxFuel = search(500_000L, 10_000_000L) { q ->
        val ore = getOreFor("FUEL", q)

        if (ore > maxOre) 1
        else if (ore == maxOre) 0
        else {
            val oreForOneMore = getOreFor("FUEL", q + 1)
            if (oreForOneMore < maxOre) -1
            else 0
        }
    }
    println("We can make $maxFuel FUEL from $maxOre ORE")
}

fun search(low: Long, high: Long, choose: (Long) -> Int) : Long {
    val mid = (low + high) / 2
    return when (choose(mid)) {
        1 -> search(low, mid, choose)
        -1 -> search(mid, high, choose)
        0 -> mid
        else -> throw RuntimeException("Dodgy return value!")
    }
}


fun getOreFor(type: String, quantity: Long, spares: MutableMap<String, Long> = mutableMapOf()) : Long {
    var ore = 0L
    val chemical = chemicals[type]!!
    val (units, extra, sparesUsed) = requiredUnits(quantity, chemical.output.quantity, spares[type])
    if (sparesUsed != 0L) spares[type] = spares[type]!! - sparesUsed
    if (extra != 0L) spares.compute(type) { _, q -> extra + (q ?: 0) }

    chemical.input.forEach {
        ore += if (it.type == "ORE") {
            units * it.quantity
        } else {
            getOreFor(it.type, units * it.quantity, spares)
        }
    }
    return ore
}

fun requiredUnits(required: Long, unitSize: Int, spares: Long?) : Triple<Long, Long, Long> {
    val sparesUsed = min(required, spares ?: 0)
    val whole = (required - sparesUsed) / unitSize
    val extra = ((required - sparesUsed) % unitSize).sign
    val units = whole + extra
    return Triple(units, units * unitSize - (required - sparesUsed), sparesUsed)
}

data class Chemical(val recipe: String) {
    val output: Ingredient
    val input: List<Ingredient>

    init {
        recipe.split("=>")
                .let { inout ->
                    input = inout.first().split(",")
                            .map { Ingredient(it) }
                            .toList()
                    output = Ingredient(inout.last())
                }
    }

    fun type() = output.type
}

class Ingredient(stuff: String) {
    val type: String
    val quantity: Int

    init {
        stuff.trim().split(" ")
                .let {
                    quantity = it.first().toInt()
                    type = it.last()
                }
    }
}

val day14 = File("day14.in").readLines()

val day14test0 = """
    10 ORE =>  1 A
    5 ORE =>  2 B
    2 A, 1 B => 1 FUEL
""".trimIndent().lines()

val day14test1 = """
    10 ORE => 10 A
    1 ORE => 1 B
    7 A, 1 B => 1 C
    7 A, 1 C => 1 D
    7 A, 1 D => 1 E
    7 A, 1 E => 1 FUEL
""".trimIndent().lines()

val day14test2 = """
    9 ORE => 2 A
    8 ORE => 3 B
    7 ORE => 5 C
    3 A, 4 B => 1 AB
    5 B, 7 C => 1 BC
    4 C, 1 A => 1 CA
    2 AB, 3 BC, 4 CA => 1 FUEL
""".trimIndent().lines()

val chemicals = day14
        .map { Chemical(it) }
        .map { it.type() to it }
        .toMap()
