import java.io.File
import java.util.stream.Collectors
import kotlin.math.sign

data class Moon(var position: Point3) {
    var velocity = Point3(0,0, 0)
    fun energy() = position.energy() * velocity.energy()
    fun state() = "${position.state()}:${velocity.state()}"
}

fun main() {
    part2(File("day12.in").readLines()
            .map(::moon)
            .toList())
}

fun part1(moons: List<Moon>) {
    for (t in 1..1000) {
        forEachPair(moons, ::applyGravity)
        moons.forEach(::applyVelocity)
    }

    val energy = moons.stream().mapToInt(Moon::energy).sum()
    println("Total energy: $energy")
}

fun part2(moons: List<Moon>) {
    val xStates = HashSet<String>(1000)
    val yStates = HashSet<String>(1000)
    val zStates = HashSet<String>(1000)

    do {
        val state = states(moons)
        val doneX = !xStates.add(state[0])
        val doneY = !yStates.add(state[1])
        val doneZ = !zStates.add(state[2])

        forEachPair(moons, ::applyGravity)
        moons.forEach(::applyVelocity)
    } while (!(doneX && doneY && doneZ))

    println("X repeats after ${xStates.size}")
    println("Y repeats after ${yStates.size}")
    println("Z repeats after ${zStates.size}")

    println("All repeat after ${lcm(xStates.size.toLong(), yStates.size.toLong(), zStates.size.toLong())}")
}



fun forEachPair(moons: List<Moon>, action: (Moon, Moon) -> Unit) {
    for ((index, from) in moons.withIndex()) {
        for (to in moons.subList(index, moons.size)) {
            if (from != to) action(from, to)
        }
    }
}

fun applyGravity(a: Moon, b: Moon) {
    val dv = (b.position - a.position).apply { n -> n.sign }
    a.velocity += dv
    b.velocity -= dv
}

fun applyVelocity(m: Moon) {
    m.position += m.velocity
}

fun moon(coord: String) : Moon {
    val coordList = coord.substring(1.until(coord.length - 1))
            .split(",")
            .stream()
            .map { it.trim().substring(2).toInt() }
            .collect(Collectors.toList())
    return Moon(Point3(coordList[0], coordList[1], coordList[2]))
}

fun states(moons: List<Moon>) : List<String> {
    val states = mutableListOf("", "", "")
    moons.forEach {
        states[0] += "${it.position.x}:${it.velocity.x};"
        states[1] += "${it.position.y}:${it.velocity.y};"
        states[2] += "${it.position.z}:${it.velocity.z};"
    }
    return states
}