import java.io.File
import TileType.*
import Direction.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import kotlin.math.max
import kotlin.math.min

enum class TileType(val id: Long, val display: String) {
    WALL(0, " "),
    FLOOR(1, "+"),
    OXYGEN(2, "O"),
    START(3, "#");

    companion object {
        fun byId(id: Long) = when (id) {
            0L -> WALL
            1L -> FLOOR
            2L -> OXYGEN
            else -> throw RuntimeException("Invalid tile id $id")
        }
    }
}

enum class Direction(val id: Long, val vector: Point2) {
    NORTH(1, Point2(0, -1)),
    SOUTH(2, Point2(0, 1)),
    WEST(3, Point2(-1, 0)),
    EAST(4, Point2(1, 0));

    companion object {
        fun byId(id: Long) = when (id) {
            1L -> NORTH
            2L -> SOUTH
            3L -> WEST
            4L -> EAST
            else -> throw RuntimeException("Invalid direction id $id")
        }
    }
}

typealias Board15 = MutableMap<Point2, TileType>

val computer15 = IntCodeComputer()
val map = mutableMapOf<Point2, TileType>()

fun main() = runBlocking {
    computer15.load(File("day15b.in").readText())
    val computerJob = GlobalScope.launch { computer15.execute() }
    val droid = Droid()
    val exploreJob = GlobalScope.launch { droid.explore() }
    exploreJob.join()


}


class Droid() {

    var loc = Point2(0, 0)
    var oxygen: Point2? = null
    var min = loc
    var max = loc

    suspend fun explore() {
        map[loc] = START
        moveNorthToWall()
        followWallToStart()
        renderMap()
        println("Shortest route to Oxygen is ${breadthFirstSearch()}")
        println("Oxygen dissipation is ${oxygenSpread()}")

    }

    suspend fun moveNorthToWall() {
        while(move(NORTH) != WALL) ;
    }

    suspend fun followWallToStart() {
        val start = loc
        var lastDirection = EAST
        do {
            for (d in dirsToTry(lastDirection)) {
                val nextTile = move(d)
                if (nextTile == WALL) continue
                lastDirection = d
                break
            }
        } while (loc != start)
    }

    fun dirsToTry(facing: Direction = NORTH) = when(facing) {
        NORTH -> listOf(WEST, NORTH, EAST, SOUTH)
        EAST -> listOf(NORTH, EAST, SOUTH, WEST)
        SOUTH -> listOf(EAST, SOUTH, WEST, NORTH)
        WEST -> listOf(SOUTH, WEST, NORTH, EAST)
    }

    suspend fun move(d: Direction) : TileType {
        println("Sending ${d.id}")
        computer15.input.send(d.id)
        val recv = computer15.output.receive()
        println("Received $recv")
        val found = TileType.byId(recv)
        println("Found $found")
        when(found) {
            WALL -> map[loc + d.vector] = WALL
            FLOOR -> { loc += d.vector; map[loc] = map[loc] ?: FLOOR }
            OXYGEN -> { loc += d.vector; oxygen = loc; map[loc] = map[loc] ?: OXYGEN }
        }
        min = Point2(min(min.x, loc.x), min(min.y, loc.y))
        max = Point2(max(max.x, loc.x), max(max.y, loc.y))
        return found
    }

    fun renderMap() {
        for (y in min.y..max.y) {
            for (x in min.x..max.x) {
                print(map[Point2(x, y)]?.display ?: " ")
            }
            println()
        }
    }


    fun breadthFirstSearch(q: MutableList<Point2> = mutableListOf(Point2(0, 0)),
                           route: MutableMap<Point2, Int> = mutableMapOf(Point2(0, 0) to 0))
            : Int {
        if (q.size == 0) throw RuntimeException("Could not find oxygen, we are going to die.")
        val p = q.removeAt(0)
        val dist = route[p]!!
        dirsToTry().forEach { d ->
            val pnew = p + d.vector
            if (route[pnew] == null) {
                val tnew = map[pnew]
                if (tnew == FLOOR) {
                    route[pnew] = dist + 1
                    q.add(pnew)
                }
                else if (tnew == OXYGEN) {
                    route[pnew] = dist + 1
                    return dist + 1
                }
            }
        }
        return breadthFirstSearch(q, route)
    }

    fun oxygenSpread(q: MutableList<Point2> = mutableListOf(oxygen!!),
                     route: MutableMap<Point2, Int> = mutableMapOf(oxygen!! to 0),
                     maxDist: Int = 0)
            : Int {
        if (q.size == 0) return maxDist - 1
        val p = q.removeAt(0)
        val dist = route[p]!!
        dirsToTry().forEach { d ->
            val pnew = p + d.vector
            if (route[pnew] == null) {
                val tnew = map[pnew]
                if (tnew == FLOOR || tnew == START) {
                    route[pnew] = dist + 1
                    q.add(pnew)
                }
            }
        }
        return oxygenSpread(q, route, dist + 1)
    }
}