import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.pow

val test1 = "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
val test2 = "1102,34915192,34915192,7,4,7,99,0"
val test3 = "104,1125899906842624,99"

fun main() {
    val computer = IntcodeComputer09()
    computer.load(File("day09.in").readText())
    computer.execute()
}

class IntcodeComputer09 {

    private val MEMSIZE = 4096

    private var memory = LongArray(MEMSIZE) { 0 }
    private var ic = 0
    private var relativeBase = 0

    private var running = false
    private val outputBuffer = LinkedBlockingQueue<Long>()
    private var getNext = { readLine()!!.toLong() }

    fun load(program: String) {
        program.split(",")
            .map { it.toLong() }
            .toLongArray()
            .copyInto(memory)
    }

    fun set(loc: Int, value: Long) {
        memory[loc] = value
    }

    fun get(loc: Int) : Long {
        return memory[loc]
    }

    fun setInput(getNext : () -> Long) {
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
                9 -> setRel(mode)
                99 -> halt()
                else -> error(opcode)
            }
        }
    }

    private fun decodeInstruction(instr: Long) : Pair<Int, IntArray> {
        val a = asArray(instr.toInt())
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

    fun readParam(pos: Int, mode: IntArray) : Long {
        return when (val paramMode = mode[pos - 1]) {
            0 -> memory[memory[ic + pos].toInt()]
            1 -> memory[ic + pos]
            2 -> memory[relativeBase + memory[ic + pos].toInt()]
            else -> throw RuntimeException("Invalid mode for reading: $paramMode")
        }
    }

    fun writeParam(pos: Int, value: Long, mode: IntArray) {
        when (val paramMode = mode[pos - 1]) {
            0 -> memory[memory[ic + pos].toInt()] = value
            2 -> memory[relativeBase + memory[ic + pos].toInt()] = value
            else -> throw RuntimeException("Invalid mode for writing: $paramMode")
        }
    }

    fun add(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a + b, mode)
        return ic + 4
    }

    fun multiply(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a * b, mode)
        return ic + 4
    }

    fun input(mode: IntArray) : Int {
        val value = getNext()
        writeParam(1, value, mode)
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
        return if (a != 0L) b.toInt() else ic + 3
    }

    fun jumpIfFalse(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        return if (a == 0L) b.toInt() else ic + 3
    }

    fun lessThan(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a < b) 1 else 0, mode)
        return ic + 4
    }

    fun eqals(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a == b) 1 else 0, mode)
        return ic + 4
    }

    fun setRel(mode: IntArray) : Int {
        val a = readParam(1, mode).toInt()
        relativeBase += a
        return ic + 2
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
