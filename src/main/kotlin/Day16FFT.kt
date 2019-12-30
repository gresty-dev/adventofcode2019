import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.sign

fun main() {
//    val input = "12345678".map { it.toString().toInt() }.toIntArray()
    val signal = File("day16.in").readText()
    val times = 1000
    val offsetLen = 0
//    val signal = "03036732577212944063491565474664"

    val once = signal.map { it.toString().toInt() }.toIntArray()
    var input = IntArray(times * once.size)
    val size = input.size
    for (i in 0.until(times)) {
        once.copyInto(input, i * once.size)
    }
    val offset = if (offsetLen == 0) 0 else signal.substring(0, offsetLen).toInt()
    val basePattern = intArrayOf(0, 1, 0, -1)
    val pattern = IntArray(size)
    var output = IntArray(size)

    var pRepeatAt = 0
    var pRepeats = 0
    var pValueIndex = 0
    var pValue = 0

    var tmp: IntArray = input
    for (n in 1..100) {
//        print(".")
        for (i in 0.until(size)) {
//            updatePattern(i, basePattern, pattern)
//            output[i] = applyPattern(input, pattern)
            pRepeatAt = i + 1
            pRepeats = 1
            pValueIndex = 0
            pValue = basePattern[pValueIndex]
            output[i] = 0
            for (j in 0.until(size)) {
                if (pRepeats == pRepeatAt) {
                    pRepeats = 0
                    pValueIndex = (pValueIndex + 1) % 4
                    pValue = basePattern[pValueIndex]
                }
                if (pValue == 1) output[i] += input[j]
                else if (pValue == -1) output[i] -= input[j]
                pRepeats++
            }
            output[i] = output[i].absoluteValue % 10
        }
//        print("o")
        for(i in 0.until(8)) print(output[offset + i])
        println()

        input = output
        output = tmp
        tmp = input
    }

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