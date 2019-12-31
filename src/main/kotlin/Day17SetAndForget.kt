import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.RuntimeException
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

    val computer = IntCodeComputer().load(File("day17.in").readText())
    val map = mutableMapOf<Point2, Char>()
    var vacuum: Vacuum? = null
    var max = Point2(0, 0)

    suspend fun part1() {
        GlobalScope.launch { computer.execute() }
        loadMap()
        var alignment = 0
        for (x in 0..max.x)
            for (y in 0..max.y)
                if (intersection(Point2(x, y)))
                    alignment += x * y
        println("Alignment parameter is $alignment")
    }

    fun part2() {
        val allMovements = findPath()
        allMovements.forEach { println(it.instruction) }
    }

    private suspend fun loadMap() {
        var x = 0
        var y = 0

        var value: Long? = null
        do {
            value = computer.output.receiveOrNull()
            if (value == 10L) {
                x = 0
                y += 1
            } else if (value != null) {
                val pos = Point2(x++, y)
                map[pos] = value.toChar()
                if (vacuumChars.contains(value.toChar())) {
                    vacuum = Vacuum(map, pos, value.toChar())
                }
            }
            max = Point2(max(x, max.x), max(y, max.y))
        } while (value != null)
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

    private fun testPattern(movements: List<Movement>, pattern: IntArray) : Solution? {
        val a = functionFrom('A', movements, 0, pattern[0]) ?: return null
        
    }

    private fun functionFrom(name: Char, movements: List<Movement>, from: Int, length: Int) : MovementFunction? {
        if (movements.size >= from + length) return null
        return MovementFunction(name, movements.subList(from, from + length))
    }

    private fun matches(movements: List<Movement>, functions: List<MovementFunction>, from: Int) =
        functions.filter { movements.size <= from + it.movements.size }
                .firstOrNull { movements.subList(from, from + it.movements.size) == it.movements }


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
        val instruction = movements.joinToString(transform = Movement::instruction)
    }

    class MovementRoutine(vararg val functions: MovementFunction) {
        val instruction = functions.joinToString { it.name.toString() }
    }

    class Solution(val a: MovementFunction, val b: MovementFunction, val c: MovementFunction, val r: MovementRoutine)
}