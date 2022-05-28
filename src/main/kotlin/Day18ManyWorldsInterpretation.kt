package com.cgresty.advent2019.day18

import com.cgresty.advent2019.*
import java.io.File
import java.lang.Integer.MAX_VALUE
import java.lang.RuntimeException
import kotlin.math.abs

fun main() {
    part2()
}

fun part1() {

    val input = realBoard
    input.forEach(::println)

    val start = System.currentTimeMillis()
    val board = Board(input)
    println(board.routes)
    val boardTime = System.currentTimeMillis() - start
    val shortest = depthFirstSearch2(BoardState(board))
    val searchTime = System.currentTimeMillis() - start - boardTime

    println("\n\nShortest (board: $boardTime, search: $searchTime) : ${shortest?.getTotalDistance()} " +
            "- ${shortest?.route()}")
}

fun part2() {
    val input = replace(example8)
    input.forEach(::println)

    val start = System.currentTimeMillis()
    val board = Board(input)
    println(board.routes)
    val boardTime = System.currentTimeMillis() - start
    val shortest = depthFirstSearch2(BoardState(board))
    val searchTime = System.currentTimeMillis() - start - boardTime

    println("\n\nShortest (board: $boardTime, search: $searchTime) : ${shortest?.getTotalDistance()} " +
            "- ${shortest?.route()}")

}

fun replace(original: List<String>) : List<String> {
    val updated = original.toMutableList()
    for (y in 0.until(original.size - 2)) {
        var x = -1
        do {
            x = original[y].indexOf(toReplace[0], x + 1)
            if (x >= 0
                    && toReplace[1] == original[y + 1].substring(x, x + 3)
                    && toReplace[2] == original[y + 2].substring(x, x + 3)) {
                updated[y] = original[y].replaceRange(x, x + 3, replacement[0])
                updated[y + 1] = original[y + 1].replaceRange(x, x + 3, replacement[1])
                updated[y + 2] = original[y + 2].replaceRange(x, x + 3, replacement[2])
                return updated
            }
        } while (x >= 0)
    }
    return original
}


// Map of a board state to the best known finished state from there
val cache = mutableMapOf<String, BoardState>()
fun depthFirstSearch2(state: BoardState) : BoardState? {


    val keys = state.reachableKeys()
    if (keys.all { (robot, keys) -> keys.isEmpty() } )
        return if (state.foundAllKeys()) {
            println("\nFinished: ${state.getTotalDistance()} - ${state.route()}")
            state
        } else {
            println("Failed: ${state.route()}")
            null
        }

    val cached = cache[state.cacheIndex()]
    if (cached != null) {
//        println("Cache hit! ${state.cacheIndex()}")
        return state.finishWith(cached)
    }

    var shortestBoard: BoardState? = null
    keys.forEach {(robot, keys) ->
        keys.forEach { key ->
            val nextState = state.moveToKey(robot, key)
            val nextFinishedState = depthFirstSearch2(nextState)
            if (boardDistance(nextFinishedState) < boardDistance(shortestBoard)) {
                shortestBoard = nextFinishedState
            }
        }
    }
    if (shortestBoard != null)
        cache[state.cacheIndex()] = shortestBoard!!

    return shortestBoard
}

fun boardDistance(state: BoardState?) = state?.getTotalDistance() ?: MAX_VALUE

data class BoardState(val board: Board, var atKey: List<Char>, var foundKeys: MutableMap<Char, Int>) {

    // robot -> (key -> distance))
    private var reachableKeys: Map<Int, Map<Char, Int>>

    init {
        reachableKeys = calculateReachableKeys()
    }

    constructor(board: Board) : this(board, 0.until(board.start.size).map {'0' + it}.toMutableList(), mutableMapOf())

    fun reachableKeys() = reachableKeys.map { (robot, keyDist) -> robot to keyDist.keys }

    fun moveToKey(robot: Int, key: Char): BoardState {
        val newFoundKeys = foundKeys.toMutableMap()
        newFoundKeys[key] = reachableKeys[robot]!![key] ?: error("No such reachable key '$key' for robot $robot")
        val newAtKeys = atKey.toMutableList()
        newAtKeys[robot] = key
        return BoardState(board, newAtKeys, newFoundKeys)
    }

    fun cacheIndex() : String {
        val index = atKey.toMutableList()
        board.keys.keys.filter { !found(it) }
                .filter { it != '@' }
                .sorted()
                .let { index.addAll(it) }
        return index.joinToString("")
    }

    fun finishWith(other: BoardState) : BoardState {
        other.foundKeys
                .filter { !foundKeys.containsKey(it.key) }
                .let { foundKeys.putAll(it) }
        atKey = other.atKey.toList()
        reachableKeys = mapOf()
        return this
    }

    fun foundAllKeys() = foundKeys.size == board.keys.size

    fun getTotalDistance() = foundKeys.values.sum()

    fun route() = foundKeys.keys.joinToString()

    private fun calculateReachableKeys() =
            atKey.withIndex()
                .map { (robot, _) -> robot to calculateReachableKeys2(robot) }
                .toMap()

    private fun calculateReachableKeys2(robot: Int) =
            (board.routes[atKey[robot]] ?: error("No such key '${atKey[robot]}' for robot $robot"))
                    .map { entry -> entry.key to entry.value }
                    .filter { (key, _) -> key != '@' && !found(key) }
                    .filter { (_, route) -> route.doors.all { open(it) } }
                    .filter { (_, route) -> route.keys.all { found(it) } }
                    .map { (key, route) -> key to route.dist }
                    .toMap()

    private fun found(key: Char) = foundKeys.contains(key)
    private fun open(door: Char) = foundKeys.contains(door.toLowerCase())
}

data class Board(private val area: List<String>) {

    private val width = area[0].length
    private val height = area.size
    val keys: Map<Char, Point2>
    private val doors: Map<Char, Point2>
    val start: List<Point2>
    val routes: Map<Char, Map<Char, Route>>

    init {
        val keyColl = mutableMapOf<Char, Point2>()
        val doorColl = mutableMapOf<Char, Point2>()
        val startColl = mutableListOf<Point2>()
        for (x in 0.until(width))
            for (y in 0.until(height))
                when (val c = charAt(x, y)) {
                    in 'a'..'z' -> keyColl += c to Point2(x, y)
                    in 'A'..'Z' -> doorColl += c to Point2(x, y)
                    '@' -> startColl.add(Point2(x, y))
                }
        keys = keyColl
        doors = doorColl
        start = startColl
        routes = calculateRoutes()
    }

    private fun calculateRoutes() : Map<Char, Map<Char, Route>> {
        val routes = mutableMapOf<Char, MutableMap<Char, Route>>()
        val starts = keys.keys.toMutableList()
        for (i in 0.until(start.size)) starts.add('0' + i)

        for (start in starts) {
            routes[start] = mutableMapOf()
            for (end in keys.keys) {
                if (start != end) {
                    val path = shortestPath(start, end) { charAt(it) != '#' }
                    if (path != null) {
                        routes[start]!![end] = Route(start, end, path, this)
                    }
                }
            }
        }
        return routes
    }

    private fun shortestPath(from: Char, to: Char, isValid: (Point2) -> Boolean) : Node? {
        val open = mutableListOf<Node>()
        val closed = mutableListOf<Node>()

        open.add(Node(keys[from] ?: start[from - '0'] ?: error("No such key '$from'")))

        while (open.isNotEmpty()) {
            val q = open.minBy { it.f } !!
            open.remove(q)

            dirsToTry.forEach {
                val succ = Node(q.loc + it, q)
                if (succ.loc == keys[to]) return succ
                if (isValid(succ.loc)) {
                    succ.g = q.g + 1
                    succ.h = manhattan(succ.loc - (keys[to] ?: error("No such key '$to'")))
                    if (!containsLowerF(open, succ) && !containsLowerF(closed, succ)
                            && isValid(succ.loc))
                        open.add(succ)
                }
            }

            closed.add(q)
        }
        return null // No available route
    }

    private fun manhattan(originTo: Point2) = abs(originTo.x) + abs(originTo.y)
    private fun containsLowerF(nodes: List<Node>, node: Node) =
            nodes.any { it.loc == node.loc && it.f < node.f }


    private fun charAt(x: Int, y: Int) = area.elementAtOrElse(y) {""}.elementAtOrElse(x) {'#'}
    fun charAt(loc: Point2) = charAt(loc.x, loc.y)
}

data class Node(val loc: Point2, val parent: Node? = null, var g: Int = 0, var h: Int = 0) {
    val f get() = g + h
}

class Route(private val from: Char, private val to: Char, node: Node, board: Board) {
    val doors: List<Char>
    val keys: List<Char>
    val dist: Int

    init {
        var d = 0
        val dd = mutableListOf<Char>()
        val kk = mutableListOf<Char>()
        var n: Node? = node
        while (n != null) {
            d++
            val c = board.charAt(n.loc)
            if (c in 'A'..'Z') dd.add(c)
            if (c in 'a'..'z' && c != from && c != to) kk.add(c)
            n = n.parent
        }
        keys = kk
        doors = dd
        dist = d - 1
    }

    override fun toString() = "Route: from=$from, to=$to, doors=$doors, keys=$keys, dist=$dist"
}

val dirsToTry = listOf(Point2(1, 0), Point2(0, 1), Point2(-1, 0), Point2(0, -1))

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

private val realBoard = File("day18.in").readLines()

val toReplace = """
    ...
    .@.
    ...
""".trimIndent().split("\n")

val replacement = """
    @#@
    ###
    @#@
""".trimIndent().split("\n")

val example1 = """
    #########
    #b.A.@.a#
    #########
""".trimIndent().split("\n")

val example2 = """
    ########################
    #f.D.E.e.C.b.A.@.a.B.c.#
    ######################.#
    #d.....................#
    ########################
""".trimIndent().split("\n")

val example3 = """
    ########################
    #...............b.C.D.f#
    #.######################
    #.....@.a.B.c.d.A.e.F.g#
    ########################
""".trimIndent().split("\n")

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
""".trimIndent().split("\n")

val example5 = """
    ########################
    #@..............ac.GI.b#
    ###d#e#f################
    ###A#B#C################
    ###g#h#i################
    ########################
""".trimIndent().split("\n")

val example6 = """
    #######
    #a.#Cd#
    ##...##
    ##.@.##
    ##...##
    #cB#Ab#
    #######
""".trimIndent().split("\n")

val example7 = """
    ###############
    #d.ABC.#.....a#
    ######...######
    ######.@.######
    ######...######
    #b.....#.....c#
    ###############
""".trimIndent().split("\n")

val example8 = """
    #############
    #g#f.D#..h#l#
    #F###e#E###.#
    #dCba...BcIJ#
    #####.@.#####
    #nK.L...G...#
    #M###N#H###.#
    #o#m..#i#jk.#
    #############
""".trimIndent().split("\n")