import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

enum class TileType(val id: Int) {
    WALL(0), SPACE(1), OXYGEN(2)
}

enum class Direction(val id: Int, val move: Point2) {
    NORTH(1, Point2(0, -1)),
    SOUTH(2, Point2(0, 1)),
    WEST(3, Point2(-1, 0)),
    EAST(4, Point2(1, 0))
}

class Tile(val type: TileType) {
    var distance: Int? = null
}

class DroidController {
    val brain = IntCodeComputer()
    val map = mutableMapOf<Point2, Tile>()

    var droid = Point2(0, 0)

    init {
        brain.load(File("day15.in").readText())
        GlobalScope.launch { brain.execute() }
    }

    fun explore() {
        moveNorthToWall()
        followWallToStart()
    }

    fun moveNorthToWall() {

    }

    fun followWallToStart() {

    }
}

