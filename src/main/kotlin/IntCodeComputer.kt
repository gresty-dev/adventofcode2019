import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import kotlin.math.pow


class IntCodeComputer (val name: String = "Computer"){

    companion object {
        private const val MEM_SIZE = 4096
        private val log = LoggerFactory.getLogger(IntCodeComputer.javaClass)
    }

    private var memory = LongArray(MEM_SIZE) { 0 }
    private var ic = 0
    private var relativeBase = 0

    private var running = false

    val output = Channel<Long>(50)
    val input = Channel<Long>(50)

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

    suspend fun execute() {
        running = true
        while(running) {
            val (opcode, mode) = decodeInstruction(memory[ic])
//            log.info("[$name] Executing opcode $opcode")
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

    fun readInputFromStdin() {
        GlobalScope.launch {
            while (true) {
                input.send(readLine()!!.toLong())
            }
        }
    }

    fun writeOutputToStdOut() {
        GlobalScope.launch {
            while (true) {
                println(output.receive())
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

    private fun value(a: IntArray, i: Int) = if (i < a.size) a[i] else 0

    private fun readParam(pos: Int, mode: IntArray) : Long {
        return when (val paramMode = mode[pos - 1]) {
            0 -> memory[memory[ic + pos].toInt()]
            1 -> memory[ic + pos]
            2 -> memory[relativeBase + memory[ic + pos].toInt()]
            else -> throw RuntimeException("Invalid mode for reading: $paramMode")
        }
    }

    private fun writeParam(pos: Int, value: Long, mode: IntArray) {
        when (val paramMode = mode[pos - 1]) {
            0 -> memory[memory[ic + pos].toInt()] = value
            2 -> memory[relativeBase + memory[ic + pos].toInt()] = value
            else -> throw RuntimeException("Invalid mode for writing: $paramMode")
        }
    }

    private fun add(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a + b, mode)
        return ic + 4
    }

    private fun multiply(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, a * b, mode)
        return ic + 4
    }

    private suspend fun input(mode: IntArray) : Int {
        val value = input.receive()
        log.info("[$name] received $value")
        writeParam(1, value, mode)
        return ic + 2
    }

    private suspend fun output(mode: IntArray) : Int {
        val value = readParam(1, mode)
        output.send(value)
        log.info("[$name] wrote $value")
        return ic + 2
    }

    private fun jumpIfTrue(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        return if (a != 0L) b.toInt() else ic + 3
    }

    private fun jumpIfFalse(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        return if (a == 0L) b.toInt() else ic + 3
    }

    private fun lessThan(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a < b) 1 else 0, mode)
        return ic + 4
    }

    private fun eqals(mode: IntArray) : Int {
        val a = readParam(1, mode)
        val b = readParam(2, mode)
        writeParam(3, if (a == b) 1 else 0, mode)
        return ic + 4
    }

    private fun setRel(mode: IntArray) : Int {
        val a = readParam(1, mode).toInt()
        relativeBase += a
        return ic + 2
    }

    private fun halt() : Int {
        running = false
        output.close()
        return ic + 1
    }

    private fun error(opcode: Int) : Int {
        println("Error: Unknown opcode $opcode")
        running = false
        return ic + 1
    }
}
