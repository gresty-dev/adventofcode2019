import java.io.File
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.TransferQueue
import kotlin.math.pow
import kotlin.text.Charsets.UTF_8

class IntcodeComputer05 {
    private lateinit var memory: MutableList<Int>
    private var ic = 0
    private var running = false
    private val outputBuffer = LinkedBlockingQueue<Int>()
    private var getNext = { readLine()!!.toInt() }

    fun load(program: String) {
        memory = program.split(",")
            .map { it.toInt() }
            .toMutableList()
    }

    fun set(loc: Int, value: Int) {
        memory[loc] = value
    }

    fun get(loc: Int) : Int {
        return memory[loc]
    }

    fun setInput(getNext : () -> Int) {
        this.getNext = getNext
    }

    fun getOutput() = outputBuffer

    fun execute() {
        running = true
        while(running) {
            val (opcode, mode) = decodeInstruction(memory[ic])
            ic = when (opcode) {
                1 -> add(mode)
                2 -> multiply(mode)
                3 -> input(mode)
                4 -> output(mode)
                5 -> jumpIfTrue(mode)
                6 -> jumpIfFalse(mode)
                7 -> lessThan(mode)
                8 -> eqals(mode)
                99 -> halt()
                else -> error(opcode)
            }
        }
    }

    private fun decodeInstruction(instr: Int) : Pair<Int, IntArray> {
        val a = asArray(instr)
        val opcode = value(a, 0) + 10 * value(a, 1)
        val mode = intArrayOf(value(a, 2), value(a, 3), value(a, 4), value(a, 5))
        return opcode to mode
    }

    private fun asArray(num: Int) : IntArray {
        val a = IntArray(6)
        for (i in 0..5) {
            a[i] = num % (10.0.pow(i + 1).toInt()) / (10.0.pow(i).toInt())
        }
        return a
    }

    fun value(a: IntArray, i: Int) = if (i < a.size) a[i] else 0

    fun readParam(pos: Int, mode: IntArray) : Int {
        val paramMode = mode[pos - 1]
        return if (paramMode == 0) memory[memory[ic + pos]] else memory[ic + pos]
    }

    fun writeParam(pos: Int, value: Int) {
        memory[memory[ic + pos]] = value
    }

    fun add(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a + b)
        return ic + 4
    }

    fun multiply(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a * b)
        return ic + 4
    }

    fun input(mode: IntArray) : Int {
        val value = getNext()
        writeParam(1, value)
        return ic + 2
    }

    fun output(mode: IntArray) : Int {
        val value = readParam(1, mode)
        println("Output: $value")
        outputBuffer.add(value)
        return ic + 2
    }

    fun jumpIfTrue(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        return if (a != 0) b else ic + 3
    }

    fun jumpIfFalse(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        return if (a == 0) b else ic + 3
    }

    fun lessThan(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a < b) 1 else 0)
        return ic + 4
    }

    fun eqals(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a == b) 1 else 0)
        return ic + 4
    }

    fun halt() : Int {
        running = false
        return ic + 1
    }

    fun error(opcode: Int) : Int {
        println("Error: Unknown opcode $opcode")
        running = false
        return ic + 1
    }
}

fun main() {

    val computer = IntcodeComputer05()
    computer.load(File("day05.in").readText())
//    computer.load("3,9,8,9,10,9,4,9,99,-1,8")
    computer.execute()
}

