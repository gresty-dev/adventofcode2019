package com.cgresty.advent2019.day8

import java.io.File

const val WIDTH = 25
const val HEIGHT = 6

fun main() {
    checksum()
    render(flatten())
}

fun flatten() : IntArray {
    val image = IntArray(WIDTH * HEIGHT) { 2 }
    val buffer = CharArray(WIDTH * HEIGHT)

    File("day08.in").bufferedReader().use {
        while (it.read(buffer) == WIDTH * HEIGHT) {
            combine(image, buffer)
        }
    }
    return image
}

fun combine(image: IntArray, buffer: CharArray) {
    for (i in 0.until(WIDTH * HEIGHT)) {
        val bufferCell = Character.getNumericValue(buffer[i])
        if (image[i] == 2 && bufferCell < 2) image[i] = bufferCell
    }
}

fun render(image: IntArray) {
    for (y in 0.until(HEIGHT)) {
        for (x in 0.until(WIDTH))
            when (image[y * WIDTH + x]) {
                0 -> print(" ")
                1 -> print("X")
                else -> print(" ")
            }
        println()
    }
}

fun checksum() {
    var counts = IntArray(3) { Int.MAX_VALUE }

    val buffer = CharArray(WIDTH * HEIGHT)
    File("day08.in").bufferedReader().use {
        while (it.read(buffer) == WIDTH * HEIGHT) {
            val newCounts = count(buffer)
            if (newCounts[0] < counts[0]) {
                counts = newCounts
            }
        }
    }
    println("Min zeroes: ${counts[0]} - checksum: ${counts[1] * counts[2]}")
}

fun count(buffer: CharArray) : IntArray {
    val counts = IntArray(3) { 0 }
    buffer.map(Character::getNumericValue)
        .filter { it < counts.size }
        .forEach{ counts[it]++ }
    return counts
}
