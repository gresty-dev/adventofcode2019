import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

val testProgram1 = "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0"
val testProgram2 = "3,23,3,24,1002,24,10,24,1002,23,-1,23," +
        "101,5,23,23,1,24,23,23,4,23,99,0,0"
val testProgram3 = "3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33," +
        "1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0"
val realProgram = File("day07.in").readText()

val program = realProgram

val executor = Executors.newFixedThreadPool(5)

fun main() {
    feedback()
    executor.shutdown()
}

fun basic() {
    var maxSignal = 0
    var idealPhaseSetting: List<Int>? = null

    permute(listOf(0, 1, 2, 3, 4))
        .forEach {
            val result = execute(it.toIntArray())
            if (result > maxSignal) {
                maxSignal = result
                idealPhaseSetting = it
            }
        }
    println("Max signal: $maxSignal")
    println(idealPhaseSetting)
}

fun feedback() {
    var maxSignal = 0
    var idealPhaseSetting: List<Int>? = null

    permute(listOf(5, 6, 7, 8, 9))
        .forEach {
            val result = execute(it.toIntArray(), true)
            if (result > maxSignal) {
                maxSignal = result
                idealPhaseSetting = it
            }
        }
    println("Max signal: $maxSignal")
    println(idealPhaseSetting)

}

fun <T> permute(input: List<T>) : List<List<T>> {
    if (input.size == 1) return listOf(input)
    val perms = mutableListOf<List<T>>()
    val toInsert = input[0]
    for (perm in permute(input.drop(1))) {
        for (i in 0..perm.size) {
            val newPerm = perm.toMutableList()
            newPerm.add(i, toInsert)
            perms.add(newPerm)
        }
    }
    return perms
}
fun execute(phases: IntArray, withFeedback: Boolean = false) : Int {
    val amps = ArrayList<IntcodeComputer05>(phases.size)

    var prevAmp: IntcodeComputer05? = null
    for (phase in phases) {
        val amp = createAmp(phase, prevAmp)
        amps.add(amp)
        prevAmp = amp
    }

    if (withFeedback) {
        val amp0input = listOf(phases[0], 0).iterator()
        amps[0].setInput { if (amp0input.hasNext()) amp0input.next() else prevAmp?.getOutput()?.take() ?: 0 }
    }

    val futures = ArrayList<Future<*>>()
    for (amp in amps) {
        futures.add(executor.submit { amp.execute() })
    }

    for (future in futures) future.get()

    return prevAmp?.getOutput()?.remove() ?: 0
}

fun createAmp(phase: Int, prevAmp: IntcodeComputer05? = null) : IntcodeComputer05 {
    val amp = IntcodeComputer05()
    val input = listOf(phase).iterator()
    amp.load(program)
    amp.setInput { if (input.hasNext()) input.next() else prevAmp?.getOutput()?.take() ?: 0 }
    return amp
}