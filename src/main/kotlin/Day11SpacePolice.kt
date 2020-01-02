package com.cgresty.advent2019.day11

import com.cgresty.advent2019.IntCodeComputer
import com.cgresty.advent2019.Point2
import com.cgresty.advent2019.plus
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.max
import kotlin.math.min

val LOG = LoggerFactory.getLogger("")
class Robot(val computer: IntCodeComputer) {
    var loc = Point2(0, 0)
    var dir = 0
    var min = loc
    var max = loc

    val dirs = arrayOf(Point2(0, -1), Point2(1, 0), Point2(0, 1), Point2(-1, 0))
    val panel = mutableMapOf<Point2, Int>()
    val colours = arrayOf("  ", "XX")

    init {
        panel[loc] = 1
    }

    fun paint(colour: Int) {
        panel[loc] = colour
    }

    fun move(turn: Int) {
        dir = Math.floorMod(dir + 2 * turn - 1, 4)
        loc += dirs[dir]
        min = Point2(min(min.x, loc.x), min(min.y, loc.y))
        max = Point2(max(max.x, loc.x), max(max.y, loc.y))
    }

    fun colour() = colour(loc)

    fun colour(at: Point2) = panel.getOrDefault(at, 0)

    fun renderPanel() {
        for (y in min.y..max.y) {
            for (x in min.x..max.x) {
                print(colours[colour(Point2(x, y))])
            }
            println()
        }
    }

    suspend fun execute() {

        var running = true
        while(running) {
            computer.input.send(colour().toLong())
            try {
                val colour = computer.output.receive()
                paint(colour.toInt())
                val turn = computer.output.receive()
                move(turn.toInt())
            } catch (ex: ClosedReceiveChannelException) {
                running = false
            }
        }
        LOG.info("Robot done")
    }

    fun paintedPanels() = panel.size
}

val computer = IntCodeComputer()

fun main() = runBlocking {
    LOG.info("Loading program")
    computer.load(File("day11.in").readText())
    LOG.info("Creating robot")
    val robot = Robot(computer)
    LOG.info("Launching robot")
    val job = launch { robot.execute() }
    LOG.info("Executing program")
    computer.execute()
    LOG.info("Waiting for robot to complete")
    job.join()
    println("${robot.paintedPanels()} panels painted.")
    robot.renderPanel()
}