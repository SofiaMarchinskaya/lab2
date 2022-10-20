import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

fun main() {
    //имя входного файла
    val inputFileName = "file.txt"
    //имя выходного файла
    val outputFile = "out.txt"
    //список обработанных данных
    val results: MutableList<ProcessingResult?> = mutableListOf()
    val regex = "(-?\\d+\\.?\\d*)?\\s*(\\S)\\s*(-?\\d+\\.?\\d*)".toRegex()
    val base = 4
    try {
        //Создаем объект класса для ввода из файла
        val sc = Scanner(File(inputFileName))
        //пока есть данные в файле, считываем их в список
        while (sc.hasNext()) {
            val s = sc.nextLine()
            results.add(processExpression(s, regex, base))
        }
        //  Создаем объект для файлового вывода
        try {
            FileWriter(outputFile, false).use { writer ->
                //поочередно записываем каждый элемент массива в файл
                results.forEach { fileWrite(writer, it) }
            }
        } catch (e: Exception) { //обработчик исключений
            println(e.localizedMessage)
        }
    } catch (e: Exception) {
        println(e.localizedMessage)
    }
}

fun processExpression(expression: String, regex: Regex, base: Int): ProcessingResult? {
    val matchResult = regex.matchEntire(expression)
    val result = regex.find(expression)

    if (matchResult != null && result != null) {
        var (first, operand, second) = result.destructured
        val firstSign = expression[0] == '-'
        val isNegative = abs(first.toDouble()) < abs(second.toDouble())
        first = abs(first.toDouble()).toString()
        var firstConverted = processStringToAnotherSystem(first, base)
        val secondConverted = processStringToAnotherSystem(second, base)
        val sizeUntilDot = maxOf(
            firstConverted.substring(0, firstConverted.indexOf(".")).length,
            secondConverted.substring(0, secondConverted.indexOf(".")).length
        )
        val sizeAfterDot = maxOf(
            firstConverted.substring(firstConverted.indexOf(".") + 1).length,
            secondConverted.substring(secondConverted.indexOf(".") + 1).length
        )
        val firstList = BasedArray(sizeUntilDot + 1, sizeAfterDot, firstConverted)
        val secondList = BasedArray(sizeUntilDot + 1, sizeAfterDot, secondConverted)
        val resultNum: String = if (operand == "-") {
            if (firstSign) {
                "-" + convertToNum(
                    firstList.plus(secondList, sizeAfterDot, sizeUntilDot + 1, base),
                    sizeAfterDot,
                    sizeUntilDot + 1
                )
            } else {
                if (isNegative) {
                    "-"
                } else {
                    ""
                } + convertToNum(
                    firstList.minus(secondList, sizeAfterDot, sizeUntilDot + 1, base),
                    sizeAfterDot,
                    sizeUntilDot + 1
                )
            }
        } else {
            if (firstSign) {
                if (!isNegative && abs(first.toDouble()) != abs(second.toDouble())) {
                    "-"
                } else {
                    ""
                } + convertToNum(
                    secondList.minus(firstList, sizeAfterDot, sizeUntilDot + 1, base),
                    sizeAfterDot,
                    sizeUntilDot + 1
                )
            } else {
                convertToNum(
                    firstList.plus(secondList, sizeAfterDot, sizeUntilDot + 1, base),
                    sizeAfterDot,
                    sizeUntilDot + 1
                )
            }
        }
        if (firstSign) {
            firstConverted = "-$firstConverted"
            first = "-$first"
        }
        return ProcessingResult(
            resultNum = resultNum,
            firstBasedString = firstConverted,
            secondBasedString = secondConverted,
            firstConverted = firstList,
            secondConverted = secondList,
            firstInput = first,
            secondInput = second,
            operand = operand,
        )
    }
    return null
}

data class ProcessingResult(
    val operand: String,
    val firstInput: String,
    val secondInput: String,
    val resultNum: String,
    val firstBasedString: String,
    val secondBasedString: String,
    val firstConverted: BasedArray,
    val secondConverted: BasedArray,
)

fun convertToNum(num: List<Int>, sizeAfterDot: Int, sizeUntilDot: Int): String {
    var res = ""
    for (el in 0 until sizeUntilDot) {
        res += num[el]
    }
    if (sizeAfterDot > 0) {
        res += "."
    }
    for (el in sizeUntilDot until sizeAfterDot + sizeUntilDot) {
        res += num[el]
    }
    return res
}

fun BasedArray.plus(second: BasedArray, sizeAfterDot: Int, sizeUntilDot: Int, base: Int): MutableList<Int> {
    val result = MutableList(sizeAfterDot + sizeUntilDot + 1) { 0 }
    var addition = 0
    for (el in sizeAfterDot + sizeUntilDot - 1 downTo 0) {
        val n = this.array[el] + second.array[el]
        if (n < base) {
            result[el] = n + addition
            addition = 0
        } else {
            result[el] = n % base + addition
            addition = n / base
        }
    }
    if (this.array[0] + second.array[0] + addition >= base) {
        for (el in sizeAfterDot + sizeUntilDot downTo 1) {
            result[el] = result[el - 1]
        }
        result[0] = addition
    }
    return result
}

fun BasedArray.minus(second: BasedArray, sizeAfterDot: Int, sizeUntilDot: Int, base: Int): MutableList<Int> {
    val result = MutableList(sizeAfterDot + sizeUntilDot + 1) { 0 }
    var point = 0
    for (i in sizeAfterDot + sizeUntilDot - 1 downTo 0) {
        if (this.array[i] < second.array[i] && point == 0) {
            point = 1
            result[i] = this.array[i] + base - second.array[i]
        } else if (this.array[i] <= second.array[i] && point == 1) {
            result[i] = this.array[i] - 1 + base - second.array[i]
        } else if (this.array[i] > second.array[i] && point == 1) {
            result[i] = this.array[i] - 1 - second.array[i]
            point = 0
        } else if (this.array[i] > second.array[i] && point == 0) {
            result[i] = this.array[i] - second.array[i]
        }
    }
    return result
}
//класс для предстваления числа в разрядной форме.
//maxSizeUntilDot - размер для целой части(берется максимум из двух чисел)
//maxSizeAfterDot - размер для дробной части(берется максимум из двух чисел)
class BasedArray(maxSizeUntilDot: Int, maxSizeAfterDot: Int, source: String) {
    val array: MutableList<Int> = MutableList(maxSizeAfterDot + maxSizeUntilDot) { 0 }

    init {
        val dotIndex = source.indexOf(".")
        val startIndex = maxSizeUntilDot - dotIndex
        for (el in 0 until dotIndex) {
            array[el + startIndex] = source[el].toString().toInt()
        }
        for (el in dotIndex + 1 until source.length) {
            array[el + startIndex - 1] = source[el].toString().toInt()
        }
    }
}

//Функция обработки входной строки
fun processStringToAnotherSystem(number: String, base: Int): String {
    var result = ""
    return try { //проверяем, содержит ли строка символ "."
        if (number.contains(".")) {
            //Разделяем строку на целую и дробную части по точке
            val mainPart = number.substring(0, number.indexOf("."))
            val realPart = number.substring(number.indexOf(".") + 1)
            //переводим строку в числовой формат
            val mainDouble = mainPart.toDouble()
            val realDouble = "0.$realPart".toDouble()
            //считаем точность
            val precision = countPrecision(realPart.length, base)
            //записываем результат как соединение двух строк
            if (number.contains("-")) {
                result += "-"
            }
            result += mainDouble.toAnotherSystem(base) + realDouble.toAnotherSystemReal(base, precision)
        } else {
            //если число изначально целое, то сразу переводим в числовой формат
            val n = number.toDouble()
            if (n < 0) {
                result += "-"
            }
            result += n.toAnotherSystem(base)
        }
        result
        //отлавливаем ошибки
    } catch (e: Exception) {
        result = "Неверные входные данные: $number"
        result
    }
}

//Функция для записи в файл
//Принимает поток файлового вывода и данные для вывода
@Throws(IOException::class)
fun fileWrite(writer: FileWriter, result: ProcessingResult?) {
    result?.let {
        writer.write("Входные данные: \n")
        writer.append("Исходные числа в 10-ной системе: ")
        writer.append(result.firstInput).append(" ").append(result.secondInput)
        writer.append("\n")
        writer.write("Операция: ")
        writer.append(result.operand)
        writer.append("\n")
        writer.write("Выходные данные: \n")
        writer.append("Исходные числа в 4-ной системе: ")
        writer.append(result.firstBasedString).append(" ").append(result.secondBasedString)
        writer.append("\n")
        writer.append("Исходные числа в 4-ной системе в разрядном виде:\n")
        writer.append(result.firstConverted.array.toString()).append("\n")
            .append(result.secondConverted.array.toString())
        writer.append("\n")
        writer.write("Результат: ")
        writer.append(result.resultNum)
        writer.write("\n")
        writer.write("\n")
    } ?: writer.write("Ошибка!\n")
    writer.write("\n")
}

//Функция, переводящая 10-ное число в число в заданной системе счисления(целая часть)
private fun Double.toAnotherSystem(base: Int): String {
    var source = this
    var result = ""
    while (source != 0.0) {
        result += abs((source % base).toInt())
        source = (source.toInt() / base).toDouble()
    }
    if (result == "") {
        result = "0"
    }
    return StringBuilder(result).reverse().toString()
}

//Функция, переводящая 10-ное число в число в заданной системе счисления(вещественная часть)
private fun Double.toAnotherSystemReal(base: Int, prec: Int): String {
    var source = this
    var result = "."

    var div: Int
    var p: Double
    var k = 0
    while (source != 0.0 && k < prec) {
        p = source * base
        div = p.toInt()
        source = p - div
        result += abs(div)
        k++
    }
    return result
}

//функция подсчета точности
fun countPrecision(n: Int, base: Int): Int {
    val decPrecision = 10.0.pow(n.toDouble())
    var ternaryPrecision: Long = 1
    var k = 0
    while (decPrecision >= ternaryPrecision) {
        ternaryPrecision *= base
        k++
    }
    return k
}