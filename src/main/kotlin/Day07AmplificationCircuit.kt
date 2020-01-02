package com.cgresty.advent2019.day7

import com.cgresty.advent2019.IntCodeComputer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.io.File

val testProgram1 = "3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0"
val testProgram2 = "3,23,3,24,1002,24,10,24,1002,23,-1,23," +
        "101,5,23,23,1,24,23,23,4,23,99,0,0"
val testProgram3 = "3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33," +
        "1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0"
val realProgram = File("day07.in").readText()

val program = realProgram

private val log = LoggerFactory.getLogger(IntCodeComputer.javaClass)

fun main() = runBlocking {
    feedback()
}

suspend fun single() {
    val amp = createAmp("Computer", 0)
    amp.writeOutputToStdOut()
    amp.input.send(3L) // phase
    amp.input.send(0L) // input signal
    amp.execute()
}

suspend fun basic() {
    var maxSignal = 0L
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

suspend fun feedback() {
    var maxSignal = 0L
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

suspend fun execute(phases: IntArray, withFeedback: Boolean = false) : Long {
    val amps = ArrayList<IntCodeComputer>(phases.size)

    var prevAmp: IntCodeComputer? = null
    var seq = 0
    for (phase in phases) {
        val amp = createAmp(name(phases, seq++), phase, prevAmp)
        amps.add(amp)
        prevAmp = amp
    }

    val lastAmp = prevAmp

    amps[0].input.send(0L)
    if (withFeedback) {
        GlobalScope.launch { while(true) amps[0].input.send(lastAmp!!.output.receive()) }
    }

    val jobs = mutableListOf<Job>()
    for (amp in amps) {
        jobs.add(GlobalScope.launch { amp.execute() })
    }

    log.info("Waiting for amps to complete")
    for (job in jobs) job.join()

    log.info("Retrieving last output from ${amps[0].name}")
    return amps[0].input.receive() ?: 0L
}

fun name(phases: IntArray, seq: Int) =
        "Phase" +
                phases.map { it.toString() }.reduce { acc, s -> acc + s } +
                "-$seq"

suspend fun createAmp(name: String, phase: Int, prevAmp: IntCodeComputer? = null) : IntCodeComputer {
    val amp = IntCodeComputer(name)
    amp.load(program)
    amp.input.send(phase.toLong())
    if (prevAmp != null)
        GlobalScope.launch { while (true) amp.input.send(prevAmp.output.receive()) }
    return amp
}