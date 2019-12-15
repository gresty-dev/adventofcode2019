import java.io.File
import kotlin.math.*

data class Asteroid(val cart: Point2, val polar: Polar)

typealias AsteroidMap = List<String>

fun forEachAsteroidIn(map: AsteroidMap, action: (Point2) -> Unit) {
    for (y in 0.until(map.size))
        for (x in 0.until(map[y].length))
            if (asteroidAt(Point2(x, y), map))
                action(Point2(x, y))
}

fun main() {
//    val map = listOf(".....", ".....", "..#..", ".....", ".....")
//    val map = listOf(".#..#", ".....", "#####", "....#", "...##")
    val map = loadMap()
    val best = getBestLocation(map)
    println("Best location is ${best.first}, which can see ${best.second} other asteroids.")

    val vaporize = sortForVaporization(best.first, map)
    val a200 = vaporize[199]
    println("The 200th asteroid vaporized is at ${a200.x * 100 + a200.y}")
}

fun loadMap() = File("day10.in").readLines()

fun sortForVaporization(from: Point2, map: AsteroidMap): List<Point2> {
    val ordered = sortedMapOf<Double, MutableSet<Asteroid>>()
    forEachAsteroidIn(map) { loc ->
        val radial = getRadialFor(loc, from)
        ordered.computeIfAbsent(radial.r) { _ -> sortedSetOf(compareBy { it.polar.d }) }
            .add(Asteroid(loc, radial))
    }

    val pivot = mutableListOf<MutableList<Point2>>()
    ordered.forEach { (_, asteroidSet) ->
        for (i in 0.until(asteroidSet.size)) {
            if (pivot.size <= i) pivot.add(mutableListOf())
            pivot[i].add(asteroidSet.elementAt(i).cart)
        }
    }

    val orderedForVaporization = mutableListOf<Point2>()
    pivot.forEach { it.forEach { coord -> orderedForVaporization.add(coord) } }
    return orderedForVaporization
}

fun getRadialFor(asteroid: Point2, from: Point2): Polar {
    val relative = asteroid - from
    val x = relative.x.toDouble()
    val y = relative.y.toDouble()
    val d = sqrt(x * x + y * y)
    val r = atan2(-x, y)
    return Polar(r, d)
}

fun getBestLocation(map: AsteroidMap): Pair<Point2, Int> {
    var bestCount = -1
    var bestLoc: Point2? = null

    forEachAsteroidIn(map) { loc ->
        val count = asteroidsVisibleFrom(loc, map)
        if (count > bestCount) {
            bestCount = count
            bestLoc = loc
        }
    }

    return Pair(bestLoc!!, bestCount)
}

fun asteroidsVisibleFrom(pos: Point2, map: AsteroidMap): Int {
    var count = 0
    forEachAsteroidIn(map) { asteroid -> if (isVisible(asteroid, pos, map)) count++ }
    return count
}

fun isVisible(target: Point2, from: Point2, map: AsteroidMap): Boolean {
    if (target == from) return false
    return getPath(from, target)
        .filter { asteroidAt(it, map) }
        .count() == 1
}

fun asteroidAt(pos: Point2, map: AsteroidMap) = map[pos.y][pos.x] == '#'

fun getPath(from: Point2, to: Point2): List<Point2> {
    val vector = to - from
    val step = step(vector.x, vector.y)
    var pos = Point2(0, 0)
    val path = mutableListOf<Point2>()
    while (pos != vector) {
        pos += step
        path.add(from + pos)
    }
    return path
}

fun step(x: Int, y: Int) = when {
    x == 0 && y == 0 -> Point2(0, 0)
    x == 0 -> Point2(0, y.sign)
    y == 0 -> Point2(x.sign, 0)
    else -> {
        val gcd = gcd(x.toLong(), y.toLong()).toInt()
        Point2(x / gcd, y / gcd)
    }
}
