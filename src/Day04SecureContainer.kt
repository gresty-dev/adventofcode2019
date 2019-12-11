import kotlin.math.pow

val min = 128392
val max = 643281

fun main() {
    println((min..max)
        .map { asArray(it) }
        .filter { paired(it) }
        .filter { increasing(it) }
        .count())
}

fun paired(a: IntArray) : Boolean {
    var runOf = -1
    var runLength = 0
    for (i in 0.until(a.size))
        when {
            a[i] == runOf -> runLength++
            runLength == 2 -> return true
            else -> {
                runOf = a[i]; runLength = 1
            }
        }
    return runLength == 2
}

fun increasing(a: IntArray) : Boolean {
    for (i in 0.until(a.size - 1))
        if (a[i] < a[i + 1]) return false
    return true
}

fun asArray(num: Int) : IntArray {
    val a = IntArray(6)
    for (i in 0..5) {
        a[i] = num % (10.0.pow(i + 1).toInt()) / (10.0.pow(i).toInt())
    }
    return a
}