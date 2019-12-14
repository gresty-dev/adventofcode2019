import java.io.File
import kotlin.math.*

data class Radial(val r: Double, val d: Double)

data class Coord(val x: Int, val y: Int)

operator fun Coord.plus(c: Coord) = Coord(this.x + c.x, this.y + c.y)
operator fun Coord.minus(c: Coord) = Coord(this.x - c.x, this.y - c.y)

data class Asteroid(val cart: Coord, val radial: Radial)

typealias AsteroidMap = List<String>

fun forEachAsteroidIn(map: AsteroidMap, action: (Coord) -> Unit) {
    for (y in 0.until(map.size))
        for (x in 0.until(map[y].length))
            if (asteroidAt(Coord(x, y), map))
                action(Coord(x, y))
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

fun sortForVaporization(from: Coord, map: AsteroidMap): List<Coord> {
    val ordered = sortedMapOf<Double, MutableSet<Asteroid>>()
    forEachAsteroidIn(map) { loc ->
        val radial = getRadialFor(loc, from)
        ordered.computeIfAbsent(radial.r) { _ -> sortedSetOf(compareBy { it.radial.d }) }
            .add(Asteroid(loc, radial))
    }

    val pivot = mutableListOf<MutableList<Coord>>()
    ordered.forEach { (_, asteroidSet) ->
        for (i in 0.until(asteroidSet.size)) {
            if (pivot.size <= i) pivot.add(mutableListOf())
            pivot[i].add(asteroidSet.elementAt(i).cart)
        }
    }

    val orderedForVaporization = mutableListOf<Coord>()
    pivot.forEach { it.forEach { coord -> orderedForVaporization.add(coord) } }
    return orderedForVaporization
}

fun getRadialFor(asteroid: Coord, from: Coord): Radial {
    val relative = asteroid - from
    val x = relative.x.toDouble()
    val y = relative.y.toDouble()
    val d = sqrt(x * x + y * y)
    val r = atan2(-x, y)
    return Radial(r, d)
}

fun getBestLocation(map: AsteroidMap): Pair<Coord, Int> {
    var bestCount = -1
    var bestLoc: Coord? = null

    forEachAsteroidIn(map) { loc ->
        val count = asteroidsVisibleFrom(loc, map)
        if (count > bestCount) {
            bestCount = count
            bestLoc = loc
        }
    }

    return Pair(bestLoc!!, bestCount)
}

fun asteroidsVisibleFrom(pos: Coord, map: AsteroidMap): Int {
    var count = 0
    forEachAsteroidIn(map) { asteroid -> if (isVisible(asteroid, pos, map)) count++ }
    return count
}

fun isVisible(target: Coord, from: Coord, map: AsteroidMap): Boolean {
    if (target == from) return false
    return getPath(from, target)
        .filter { asteroidAt(it, map) }
        .count() == 1
}

fun asteroidAt(pos: Coord, map: AsteroidMap) = map[pos.y][pos.x] == '#'

fun getPath(from: Coord, to: Coord): List<Coord> {
    val vector = to - from
    val step = step(vector.x, vector.y)
    var pos = Coord(0, 0)
    val path = mutableListOf<Coord>()
    while (pos != vector) {
        pos += step
        path.add(from + pos)
    }
    return path
}

fun step(x: Int, y: Int) = when {
    x == 0 && y == 0 -> Coord(0, 0)
    x == 0 -> Coord(0, y.sign)
    y == 0 -> Coord(x.sign, 0)
    else -> {
        val gcd = gcd(x, y)
        Coord(x / gcd, y / gcd)
    }
}

fun gcd(x: Int, y: Int): Int {
    val xx = x.absoluteValue
    val yy = y.absoluteValue

    return when {
        xx == yy -> xx
        xx > yy -> gcd(xx - yy, yy)
        xx < yy -> gcd(xx, yy - xx)
        else -> throw RuntimeException("No GCD for $xx and $yy")
    }
}