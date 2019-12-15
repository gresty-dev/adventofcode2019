import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun main() = runBlocking {
    val computer = IntCodeComputer()
    computer.load(File("day13.in").readText())
    part2(computer)
}

suspend fun part1(computer: IntCodeComputer) {
    val blocks = mutableListOf<Point2>()
    var min = Point2(Int.MAX_VALUE, Int.MAX_VALUE)
    var max = Point2(Int.MIN_VALUE, Int.MIN_VALUE)
    val job = GlobalScope.launch {
        while (true) {
            val x = computer.output.receive().toInt()
            val y = computer.output.receive().toInt()
            min = Point2(min(min.x, x), min(min.y, y))
            max = Point2(max(max.x, x), max(max.y, y))
            val t = computer.output.receive().toInt()
            if (t == 2) blocks.add(Point2(x, y))
        }
    }
    computer.execute()
    job.join()
    println("${blocks.size} blocks onscreen, min $min, max $max")
}

suspend fun part2(computer: IntCodeComputer) {
    val screen = Cabinet(computer)
    computer.set(0, 2L)
    computer.readInputFromStdin()
    computer.execute()
    screen.waitForCompletion()
}

class Cabinet(private val computer: IntCodeComputer) {
    private val display = MutableList(24) { IntArray(45) { 0 } }
    private val job = GlobalScope.launch { handleInput() }

    var score = 0
    var paddle = Point2(0,0)
    var ball = Point2(0, 0)

    private suspend fun handleInput() {
        while (true) {
            try {
                val x = computer.output.receive().toInt()
                val y = computer.output.receive().toInt()
                val t = computer.output.receive().toInt()
                if (x == -1) score = t
                else display[y][x] = t
                if (t == 3) paddle = Point2(x, y)
                if (t == 4) {
                    ball = Point2(x, y)
                    moveJoystick()
//                    render()
                }
            } catch (ex: ClosedReceiveChannelException) {
                println("GAME OVER")
                println("Your score is: $score")
                return
            }
        }
    }

    private fun render() {
        println("Score: $score")
        for (row in display) {
            for (col in row) {
                when (col) {
                    0 -> print("  ")
                    1 -> print("[]")
                    2 -> print("XX")
                    3 -> print("==")
                    4 -> print("<>")
                }
            }
            println()
        }
    }

    suspend fun waitForCompletion() = job.join()

    private suspend fun moveJoystick() =
        computer.input.send((ball.x - paddle.x).sign.toLong())
}
