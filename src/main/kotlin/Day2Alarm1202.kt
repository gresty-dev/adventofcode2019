package com.cgresty.advent2019.day2

import java.io.File
import kotlin.text.Charsets.UTF_8

fun main() {
    println("Part 1: ${execute(12, 2, 0)}")

    for (n in 0..99)
        for (v in 0..99)
            if (execute(n, v, 0) == 19690720) {
                println(n * 100 + v)
            }
}

fun execute(noun: Int, verb: Int, retloc: Int) : Int {
    val program = load(File("day02.in"))
    var pc = 0
    program[1] = noun
    program[2] = verb
    while(exec(program, pc)) { pc += 4 }
    return program[retloc]
}

fun load(file: File) =
    file.readText(UTF_8)
        .split(",")
        .map { it.toInt() }
        .toMutableList()

fun exec(program: MutableList<Int>, pc: Int) : Boolean {
    val cmd = program[pc]
    if (cmd == 99) return false
    val a = program[program[pc + 1]]
    val b = program[program[pc + 2]]
    val loc = program[pc + 3]
    program[loc] = if (cmd == 1) a + b else a * b
    return true
}

