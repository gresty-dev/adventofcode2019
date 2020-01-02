import java.io.File
import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    val day18 = Day18ManyWorldsInterpretation()
    day18.part1()
}

class Day18ManyWorldsInterpretation {

    fun part1() {

        val input = example1.split("\n")
        input.forEach(::println)

        val board = Board18(input)
        println(board.routes)

//        val shortest = depthFirstSearch(Board18(input))
//                .minBy { it.getTotalDistance() }
//
//        println("\n\nShortest: ${shortest?.getTotalDistance()} - ${shortest?.route()}")
    }

    fun depthFirstSearch(board: Board18) : List<Board18> {
        val keys = board.reachableKeys()
        if (keys.isEmpty())
            return if (board.foundAllKeys()) {
                println("\nFinished: ${board.getTotalDistance()} - ${board.route()}")
                listOf(board)
            } else {
                println("Failed: ${board.route()}")
                listOf()
            }
        val finishedBoards = mutableListOf<Board18>()
        keys.forEach { finishedBoards.addAll(depthFirstSearch(Board18(board).moveToKey(it))) }
        return finishedBoards
    }

    data class Board18(private val area: List<String>) {

        companion object {
            val dirsToTry = listOf(Point2(1, 0), Point2(0, 1), Point2(-1, 0), Point2(0, -1))
        }

        private val width = area[0].length
        private val height = area.size
        private val keys: Map<Char, Point2>
        private val doors: Map<Char, Point2>
        private val start: Point2
        val routes: Map<Pair<Char, Char>, Route>

        var position: Point2
        val foundKeys = mutableMapOf<Char, Int>()
        var reachableKeys: Map<Char, Int>

        init {
            val keyColl = mutableMapOf<Char, Point2>()
            val doorColl = mutableMapOf<Char, Point2>()
            var pos: Point2? = null
            for (x in 0.until(width))
                for (y in 0.until(height))
                    when (val c = charAt(x, y)) {
                        in 'a'..'z' -> keyColl += c to Point2(x, y)
                        in 'A'..'Z' -> doorColl += c to Point2(x, y)
                        '@' -> pos = Point2(x, y)
                    }
            keyColl['@'] = pos!!
            keys = keyColl
            doors = doorColl
            start = pos
            position = start
            reachableKeys = calculateReachableKeys()
            routes = calculateRoutes()
        }

        constructor(other: Board18) : this(other.area) {
            this.position = other.position
            this.foundKeys.putAll(other.foundKeys)
            this.reachableKeys = calculateReachableKeys()

        }

        fun reachableKeys() = reachableKeys.keys

        fun moveToKey(key: Char) : Board18 {
            foundKeys[key] = reachableKeys[key]!!
            position = keys[key]!!
            reachableKeys = calculateReachableKeys()
            return this
        }

        fun foundAllKeys() = foundKeys.size == keys.size

        fun getTotalDistance() = foundKeys.values.sum()

        fun route() = foundKeys.keys.joinToString()

        private fun calculateRoutes() : Map<Pair<Char, Char>, Route> {
            val routes = mutableMapOf<Pair<Char, Char>, Route>()
            for (start in keys.keys) {
                for (end in keys.keys) {
                    if (start != end)
                        routes[start to end] = Route(start, end, shortestPath(start, end) { charAt(it.x, it.y) != '#' }, this)
                }
            }
            return routes
        }

        private fun bfs(q: MutableList<Point2>,
                                       distances: MutableMap<Point2, Int>,
                        updateQueue: (pnew: Point2, dist: Int) -> Boolean) {
            if (q.size == 0) return
            val p = q.removeAt(0)
            val dist = distances[p]!!
            dirsToTry.forEach { d ->
                val pnew = p + d
                if (distances[pnew] == null && updateQueue(pnew, dist)) {
                            distances[pnew] = dist + 1
                            q.add(pnew)
                }
            }
            return bfs(q, distances, updateQueue)
        }


        private fun calculateReachableKeys() : Map<Char, Int> {
            val keyDistances: MutableMap<Char, Int> = mutableMapOf()
            bfs(mutableListOf(position), mutableMapOf(position to 0)) { pnew, dist ->
                    when (val c = charAt(pnew.x, pnew.y)) {
                        '.', '@' -> true
                        in 'a'..'z' -> {
                            if (!found(c)) keyDistances[c] = dist + 1
                            found(c)
                        }
                        in 'A'..'Z' -> open(c)
                        else -> false
                    }
            }
            return keyDistances
        }

        private fun shortestPath(from: Char, to: Char, isValid: (Point2) -> Boolean) : Node {
            val open = mutableListOf<Node>()
            val closed = mutableListOf<Node>()

            open.add(Node(keys[from]!!))

            while (open.isNotEmpty()) {
                val q = open.minBy { it.f } !!
                open.remove(q)

                dirsToTry.forEach {
                    val succ = Node(q.loc + it, q)
                    if (succ.loc == keys[to]) return succ
                    if (isValid(succ.loc)) {
                        succ.g = q.g + 1
                        succ.h = manhattan(succ.loc - keys[to]!!)
                        if (!containsLowerF(open, succ) && !containsLowerF(closed, succ)
                                && isValid(succ.loc))
                            open.add(succ)
                    }
                }

                closed.add(q)
            }
            throw RuntimeException("No route from $from to $to")
        }

        private fun manhattan(originTo: Point2) = abs(originTo.x) + abs(originTo.y)
        private fun containsLowerF(nodes: List<Node>, node: Node) =
                nodes.any { it.loc == node.loc && it.f < node.f }


        fun charAt(x: Int, y: Int) = area.elementAtOrElse(y) {""}.elementAtOrElse(x) {'#'}

        private fun found(key: Char) = foundKeys.contains(key)
        private fun open(door: Char) = foundKeys.contains(door.toLowerCase())


    }

    data class Node(val loc: Point2, val parent: Node? = null, var g: Int = 0, var h: Int = 0) {
        val f get() = g + h
    }

    class Route(val from: Char, val to: Char, node: Node, board: Board18) {
        val doors: List<Char>
        val dist: Int

        init {
            var d = 0;
            var dd = mutableListOf<Char>()
            var n: Node? = node;
            while (n != null) {
                d++
                val c = board.charAt(n.loc.x, n.loc.y)
                if (c in 'A'..'Z') dd.add(c)
                n = n.parent
            }
            doors = dd
            dist = d - 1
        }
    }

    private val realBoard = File("day18.in").readLines()

    val example1 = """
        #########
        #b.A.@.a#
        #########
    """.trimIndent()

    val example2 = """
        ########################
        #f.D.E.e.C.b.A.@.a.B.c.#
        ######################.#
        #d.....................#
        ########################
    """.trimIndent()

    val example3 = """
        ########################
        #...............b.C.D.f#
        #.######################
        #.....@.a.B.c.d.A.e.F.g#
        ########################
    """.trimIndent()

    val example4 = """
        #################
        #i.G..c...e..H.p#
        ########.########
        #j.A..b...f..D.o#
        ########@########
        #k.E..a...g..B.n#
        ########.########
        #l.F..d...h..C.m#
        #################
    """.trimIndent()

    val example5 = """
        ########################
        #@..............ac.GI.b#
        ###d#e#f################
        ###A#B#C################
        ###g#h#i################
        ########################
    """.trimIndent()

}