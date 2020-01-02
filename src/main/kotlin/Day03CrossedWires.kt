package com.cgresty.advent2019.day3

import java.io.File
import kotlin.math.abs
import kotlin.math.min


class Board {
    private val board = HashMap<Pair<Int, Int>, Int>()
    private var x = 0;
    private var y = 0;
    private var w = 0;
    private var wmax = 0;

    fun start(wire: Int) {
        x = 0
        y = 0
        w = 1 shl wire
        wmax = wmax or w
    }

    fun move(instr: String) {
        val dir = instr[0]
        val count = instr.substring(1).toInt()
        when (dir) {
            'L' -> for (c in 1..count) mark(Pair(--x, y))
            'R' -> for (c in 1..count) mark(Pair(++x, y))
            'U' -> for (c in 1..count) mark(Pair(x, ++y))
            'D' -> for (c in 1..count) mark(Pair(x, --y))
        }
    }

    fun nearestCrossing() = board.entries.filter { (_, value) -> value == wmax }
        .map { (cell, _) -> manhattan(cell) }
        .min()

    private fun manhattan(cell: Pair<Int, Int>) = abs(cell.first) + abs(cell.second)

    private fun mark(cell: Pair<Int, Int>) = board.compute(cell) { _, v -> if (v == null) w else v or w }
}

class Board2 {
    private val board = HashMap<Pair<Int, Int>, Int>()
    private var x = 0;
    private var y = 0;
    private var len = 0
    private var w = 0;
    private var nearest = Int.MAX_VALUE

    fun start(wire: Int) {
        x = 0
        y = 0
        len = 0
        w = wire
    }

    fun move(instr: String) {
        val dir = instr[0]
        val count = instr.substring(1).toInt()
        when (dir) {
            'L' -> for (c in 1..count) mark(Pair(--x, y))
            'R' -> for (c in 1..count) mark(Pair(++x, y))
            'U' -> for (c in 1..count) mark(Pair(x, ++y))
            'D' -> for (c in 1..count) mark(Pair(x, --y))
        }
    }

    fun nearestCrossing() = nearest

    private fun mark(cell: Pair<Int, Int>) {
        len++
        if (w == 0) {
            board.compute(cell) { _, v -> v ?: len }
        } else if (board.containsKey(cell)) {
            val steps = len + board[cell]!!
            nearest = min(nearest, steps)
        }
    }
}

fun main() {
    val wires = day3Load(File("day03.in"))
//    val wires = listOf(
//        "R75,D30,R83,U83,L12,D49,R71,U7,L72".split(","),
//        "U62,R66,U55,R34,D71,R55,D58,R83".split(","))
//    val wires = listOf(
//        "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51".split(","),
//        "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7".split(","))
    val board1 = Board()
    val board2 = Board2()
    for (w in 0 until wires.size) {
        board1.start(w)
        board2.start(w)
        for (i in wires[w]) {
            board1.move(i)
            board2.move(i)
        }
    }
    println("Part 1: ${board1.nearestCrossing()}")
    println("Part 2: ${board2.nearestCrossing()}")
}

fun day3Load(file: File) =
    file.readText(Charsets.UTF_8)
        .split("\n")
        .map { it.split(",") }
        .toList()
