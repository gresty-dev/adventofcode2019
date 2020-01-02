package com.cgresty.advent2019.day5

import com.cgresty.advent2019.IntCodeComputer
import kotlinx.coroutines.runBlocking
import java.io.File

fun main() = runBlocking {

    val computer = IntCodeComputer()
    computer.readInputFromStdin()
    computer.writeOutputToStdOut()
    computer.load(File("day05.in").readText())
    computer.execute()
}

