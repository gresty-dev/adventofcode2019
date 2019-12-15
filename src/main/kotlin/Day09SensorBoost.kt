import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.RuntimeException
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.pow

val test1 = "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
val test2 = "1102,34915192,34915192,7,4,7,99,0"
val test3 = "104,1125899906842624,99"

fun main() = runBlocking {
    val computer = IntCodeComputer()
    computer.writeOutputToStdOut()
    computer.load(File("day09.in").readText())
    computer.input.send(2L)
    computer.execute()
}
