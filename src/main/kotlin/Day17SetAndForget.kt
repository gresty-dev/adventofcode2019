package com.cgresty.advent2019.day17

import com.cgresty.advent2019.IntCodeComputer
import com.cgresty.advent2019.Point2
import com.cgresty.advent2019.plus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.max

fun main() = runBlocking {
    val day17 = Day17SetAndForget()
    day17.part1()
    day17.part2()
}

class Day17SetAndForget {

    companion object {
        val vacuumChars = listOf('^', '>', '<', 'v')
        val scaffoldChars = listOf('#', '^', '>', '<', 'v')
        fun scaffold(map: Map<Point2, Char>, p: Point2) = scaffoldChars.contains(map.getOrDefault(p, ','))
    }

    val computer = initComputer()
    val map = mutableMapOf<Point2, Char>()
    var vacuum: Vacuum? = null
    var max = Point2(0, 0)

    suspend fun part1() {
        computer.set(0, 2)
        GlobalScope.launch { computer.execute() }
        map.clear()
        map.putAll(loadMap())
        var alignment = 0
        for (x in 0..max.x)
            for (y in 0..max.y)
                if (intersection(Point2(x, y)))
                    alignment += x * y
        println("Alignment parameter is $alignment")
    }

    suspend fun part2() {
        val allMovements = findPath()
        allMovements.forEach { println(it.instruction) }
        val solution = calculateInstructions(allMovements)
        println("Solution:\nA = ${solution.a.instruction}\nB = ${solution.b.instruction}\nC = ${solution.c.instruction}\n${solution.r.instruction}")
        val dust = executeInstructions(solution)
        println("The vacuum robot collected $dust grains of dust.")
    }

    private fun initComputer() = IntCodeComputer().load(File("day17.in").readText())

    private suspend fun loadMap() : Map<Point2, Char> {

        val map = mutableMapOf<Point2, Char>()

        var x = 0
        var y = 0

        var value: Long? = null
        do {
            val last = value
            value = computer.output.receive()
            if (value == 10L) {
                if (last == 10L) {
                    value = null // empty line -> end of input
                } else {
                    x = 0
                    y += 1
                }
            } else {
                val pos = Point2(x++, y)
                map[pos] = value.toChar()
                if (vacuumChars.contains(value.toChar())) {
                    vacuum = Vacuum(map, pos, value.toChar())
                }
            }
            max = Point2(max(x, max.x), max(y, max.y))
        } while (value != null)

        return map
    }

    private fun printMap(map: Map<Point2, Char>) {
        for (y in 0..max.y) {
            for (x in 0..max.x) {
                print(map.getOrDefault(Point2(x, y), '.'))
            }
            println()
        }
    }

    private fun intersection(point: Point2): Boolean {
        if (!scaffold(point)) return false
        var count = 0
        if (scaffold(point + Point2(-1, 0))) count++
        if (scaffold(point + Point2(1, 0))) count++
        if (scaffold(point + Point2(0, -1))) count++
        if (scaffold(point + Point2(0, 1))) count++
        return count > 2
    }

    private fun scaffold(point: Point2) = scaffold(map, point)

    private fun findPath() : List<Movement> {
        val path = mutableListOf<Movement>()
        var nextMove = vacuum!!.move(first = true)
        while (nextMove != null) {
            path += nextMove
            nextMove = vacuum!!.move()
        }
        return path
    }

    private fun calculateInstructions(movements: List<Movement>) : Solution {
        val pattern = mutableListOf(0, 1, 1)
        var solution: Solution? = null
        var cFailCount = 0 // hack hack hack
        while (solution == null) {
            try {
                pattern[0] += 1
                solution = testPattern(movements, pattern)
            } catch (ex: LimitReachedException) {
                when (ex.name) {
                    'A' -> {
                        println("Movement function 'A' too long")
                        pattern[0] = 0; pattern[1] += 1
                    }
                    'B' -> {
                        println("Movement function 'B' too long")
                        pattern[0] = 0; pattern[1] = 1; pattern[2] += 1
                    }
                    'C' -> {
                        println("Movement function 'C' too long")
                        if (++cFailCount > 20) {
                            println("Giving up.")
                            throw RuntimeException()
                        }
                    }
                    'X' -> {
                        println("Routine too long")
                    }
                }
            }
        }
        return solution
    }

    private suspend fun executeInstructions(solution: Solution) : Long {
        println(readAscii())
        writeAscii(solution.r.instruction)
        println(readAscii())
        writeAscii(solution.a.instruction)
        println(readAscii())
        writeAscii(solution.b.instruction)
        println(readAscii())
        writeAscii(solution.c.instruction)
        println(readAscii())
        writeAscii("n")
        println(readAscii())
        printMap(loadMap())
        return computer.output.receive()
    }

    private suspend fun writeAscii(ascii: String) {
        ascii.forEach { computer.input.send(it.toLong()) }
        computer.input.send(10)
    }

    private suspend fun readAscii() : String {
        val output = mutableListOf<Char>()
        var readVal = computer.output.receive()
        while (readVal != 10L) {
            output += readVal.toChar()
            readVal = computer.output.receive()
        }
        return output.joinToString(separator = "")
    }

    private fun testPattern(movements: List<Movement>, pattern: List<Int>) : Solution? {
        println("Trying $pattern")
        val routines = mutableListOf<MovementFunction>()
        var from = 0

        val a = functionFrom('A', movements, from, pattern[0]) ?: return null
        println("Trying A = ${a.instruction}")
        var matchFunction = matches(movements, listOf(a), from)
        while (matchFunction != null) {
            println("Matched ${matchFunction.name}")
            from += matchFunction.movements.size
            routines += matchFunction
            matchFunction = matches(movements, listOf(a), from)
        }

        val b = functionFrom('B', movements, from, pattern[1]) ?: return null
        println("Trying B = ${b.instruction}")
        matchFunction = matches(movements, listOf(a, b), from)
        while (matchFunction != null) {
            println("Matched ${matchFunction.name}")
            from += matchFunction.movements.size
            routines += matchFunction
            matchFunction = matches(movements, listOf(a, b), from)
        }

        val c = functionFrom('C', movements, from, pattern[2]) ?: return null
        println("Trying C = ${c.instruction}")
        matchFunction = matches(movements, listOf(a, b, c), from)
        while (matchFunction != null) {
            println("Matched ${matchFunction.name}")
            from += matchFunction.movements.size
            routines += matchFunction
            matchFunction = matches(movements, listOf(a, b, c), from)
        }

        return if (from == movements.size) Solution(a, b, c, MovementRoutine(routines))
        else null
    }

    private fun functionFrom(name: Char, movements: List<Movement>, from: Int, length: Int) : MovementFunction? {
        if (movements.size <= from + length) return null
        return MovementFunction(name, movements.subList(from, from + length))
    }

    private fun matches(movements: List<Movement>, functions: List<MovementFunction>, from: Int) =
            functions
                    .filter { func ->
                        movements.size >= from + func.movements.size
                    }
                    .firstOrNull { func ->
                        val m = movements.subList(from, from + func.movements.size)
                        val f = func.movements
                        f == m
                    }


    class Vacuum(val map: Map<Point2, Char>, var pos: Point2, dirChar: Char) {
        var direction = Direction.forChar(dirChar)

        fun move(first: Boolean = false): Movement? {
            val turn: Char?
            if (scaffold(map, pos + direction.dirInfo.step)) {
                turn = null
            } else if (scaffold(map, pos + direction.next().dirInfo.step)) {
                turn = 'R'
                direction = direction.next()
            } else if (scaffold(map, pos + direction.prev().dirInfo.step)) {
                turn = 'L'
                direction = direction.prev()
            } else if (first) {
                direction = direction.next()
                return Movement('R', 0)
            } else {
                return null // this is the end
            }

            return Movement(turn, availableSteps())
        }


        private fun availableSteps(): Int {
            var steps = 0;
            while (scaffold(map, pos + direction.dirInfo.step)) {
                pos += direction.dirInfo.step
                steps++
            }
            return steps
        }


    }

    data class DirInfo(val pointer: Char, val step: Point2)

    enum class Direction(val dirInfo: DirInfo) {
        UP(DirInfo('^', Point2(0, -1))),
        RIGHT(DirInfo('>', Point2(1, 0))),
        DOWN(DirInfo('v', Point2(0, 1))),
        LEFT(DirInfo('<', Point2(-1, 0)));

        companion object {
            fun forChar(dirChar: Char) = when (dirChar) {
                '^' -> UP
                '>' -> RIGHT
                'v' -> DOWN
                '<' -> LEFT
                else -> throw RuntimeException("Invalid direction char: $dirChar")
            }
        }

        fun next() = when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }

        fun prev() = when (this) {
            UP -> LEFT
            LEFT -> DOWN
            DOWN -> RIGHT
            RIGHT -> UP
        }
    }

    data class Movement(val turn: Char?, val distance: Int) {
        val instruction = when {
            turn == null -> "$distance"
            distance == 0 -> "$turn"
            else -> "$turn,$distance"
        }
    }

    class MovementFunction(val name: Char, val movements: List<Movement>) {
        val instruction = movements.joinToString(separator = ",", transform = Movement::instruction)
        init {
            if (instruction.length > 20) throw LimitReachedException(name)
        }
    }

    class MovementRoutine(val functions: List<MovementFunction>) {
        val instruction = functions.joinToString (separator = ",") { it.name.toString() }
        init {
            if (instruction.length > 20) throw LimitReachedException('X')
        }
    }

    class Solution(val a: MovementFunction, val b: MovementFunction, val c: MovementFunction, val r: MovementRoutine)

    class LimitReachedException(val name: Char) : RuntimeException("Instruction length limit exceeded for function $name")
}