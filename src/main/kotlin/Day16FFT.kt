import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.sign

fun main() {
//    val input = "12345678".map { it.toString().toInt() }.toIntArray()
//    val signal = File("day16.in").readText()
    val signal = "03036732577212944063491565474664"
    val once = signal.map { it.toString().toInt() }.toIntArray()
    val input = IntArray(10000 * once.size)
    for (i in 0.until(10000)) {
        once.copyInto(input, i * once.size)
    }
    val offset = signal.substring(0, 7).toInt()
    val basePattern = intArrayOf(0, 1, 0, -1)
    val pattern = IntArray(input.size)
    val output = IntArray(input.size)

    for (n in 1..100) {
        print(".")
        for (i in 0.until(output.size)) {
            updatePattern(i, basePattern, pattern)
            output[i] = applyPattern(input, pattern)
        }
        output.copyInto(input)
    }
    for(i in 0.until(8)) print(output[offset + i])
    println()

}

fun updatePattern(pos: Int, base: IntArray, pattern: IntArray) : IntArray {
    var i = 0
    var b = 0
    while (i <= pattern.size) {
        for (j in 0..pos) {
            val p = i + j - 1
            if (p >= 0 && p < pattern.size) pattern[p] = base[b]
        }
        b = ++b % base.size
        i += pos + 1
    }
    return pattern
}

fun applyPattern(input: IntArray, pattern: IntArray) : Int {
    var acc = 0
    for(i in 0.until(input.size)) {
        acc += input[i] * pattern[i]
    }
    return acc.absoluteValue % 10

}