import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.max

fun main() = runBlocking {
    Day17SetAndForget().part1()
}

class Day17SetAndForget {

    companion object {
        val scaffoldChars = listOf('#', '^', '>', '<', 'v')
        val directions = listOf('^', '>', 'v', '<')
    }

    val computer = IntCodeComputer().load(File("day17.in").readText())
    val map = mutableMapOf<Point2, Char>()
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
                map[Point2(x++, y)] = value.toChar()
            }
            max = Point2(max(x, max.x), max(y, max.y))
        } while (value != null)
    }

    private fun intersection(point: Point2) : Boolean {
        if (!scaffold(point)) return false
        var count = 0
        if (scaffold(point + Point2(-1, 0))) count++
        if (scaffold(point + Point2(1, 0))) count++
        if (scaffold(point + Point2(0, -1))) count++
        if (scaffold(point + Point2(0, 1))) count++
        return count > 2
    }

    private fun scaffold(point: Point2) = scaffoldChars.contains(map.getOrDefault(point, '.'))

    class Vacuum(var pos: Point2, dirChar: Char) {
        var direction = directions.indexOf(dirChar)
    }
}