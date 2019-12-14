import java.io.File
import java.util.concurrent.Executors

class Robot(val computer: IntcodeComputer09) {
    var loc = Point(0, 0)
    var dir = 0

    val dirs = arrayOf(Point(0, -1), Point(1, 0), Point(0, 1), Point(-1, 0))
    val panel = mutableSetOf<Point>()
    val executor = Executors.newSingleThreadExecutor()

    fun paint(colour: Int) {
        panel.add(loc)
    }

    fun move(turn: Int) {
        dir = (dir + 2 * turn - 1) % 4
        loc += dirs[dir]
    }

    fun execute() =
        executor.submit {
            var running = true
            while(running) {
                val colour = computer.getOutput().take()
                if (colour == 99L) {
                    running = false
                } else {
                    paint(colour.toInt())
                    val turn = computer.getOutput().take()
                    move(turn.toInt())
                }
            }
        }

    fun paintedPanels() = panel.size
}

data class Point(val x: Int, val y:Int)
operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)

val computer = IntcodeComputer09()

fun main() {
    computer.load(File("day11.in").readText())
    val robot = Robot(computer)
    val future = robot.execute()
    computer.execute()
    future.get()
    println("${robot.paintedPanels()} panles painted.")
}