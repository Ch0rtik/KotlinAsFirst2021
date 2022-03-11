@file:Suppress("UNUSED_PARAMETER")

package lesson11.task1

import kotlin.math.pow

/**
 * Класс "полином с вещественными коэффициентами".
 *
 * Общая сложность задания -- средняя, общая ценность в баллах -- 16.
 * Объект класса -- полином от одной переменной (x) вида 7x^4+3x^3-6x^2+x-8.
 * Количество слагаемых неограничено.
 *
 * Полиномы можно складывать -- (x^2+3x+2) + (x^3-2x^2-x+4) = x^3-x^2+2x+6,
 * вычитать -- (x^3-2x^2-x+4) - (x^2+3x+2) = x^3-3x^2-4x+2,
 * умножать -- (x^2+3x+2) * (x^3-2x^2-x+4) = x^5+x^4-5x^3-3x^2+10x+8,
 * делить с остатком -- (x^3-2x^2-x+4) / (x^2+3x+2) = x-5, остаток 12x+16
 * вычислять значение при заданном x: при x=5 (x^2+3x+2) = 42.
 *
 * В конструктор полинома передаются его коэффициенты, начиная со старшего.
 * Нули в середине и в конце пропускаться не должны, например: x^3+2x+1 --> Polynom(1.0, 2.0, 0.0, 1.0)
 * Старшие коэффициенты, равные нулю, игнорировать, например Polynom(0.0, 0.0, 5.0, 3.0) соответствует 5x+3
 */
class Polynom(vararg coeffs: Double) {
    private val coeffs: DoubleArray

    init {
        var zeroes = 0
        while (zeroes < coeffs.size && coeffs[zeroes] == 0.0) {
            zeroes++
        }
        if (zeroes == coeffs.size) {
            this.coeffs = DoubleArray(1) { 0.0 }
        } else {
            val result = DoubleArray(coeffs.size - zeroes)
            for (i in zeroes until coeffs.size) {
                result[i - zeroes] = coeffs[coeffs.size - 1 - i + zeroes]
            }
            this.coeffs = result
        }

    }

    /**
     * Геттер: вернуть значение коэффициента при x^i
     */
    fun coeff(i: Int): Double {
        if (i > degree()) {
            return 0.0
        }
        return coeffs[i]
    }

    /**
     * Расчёт значения при заданном x
     */
    fun getValue(x: Double): Double {
        var sum = 0.0
        for (i in coeffs.indices) {
            sum += x.pow(i) * coeffs[i]
        }
        return sum
    }

    /**
     * Степень (максимальная степень x при ненулевом слагаемом, например 2 для x^2+x+1).
     *
     * Степень полинома с нулевыми коэффициентами считать равной 0.
     * Слагаемые с нулевыми коэффициентами игнорировать, т.е.
     * степень 0x^2+0x+2 также равна 0.
     */
    fun degree(): Int = coeffs.size - 1

    /**
     * Сложение
     */

    private fun plusMinus(other: Polynom, type: Int): Polynom {
        val newDegree = maxOf(this.degree(), other.degree())
        val thisCoeff: (Int) -> Double
        val otherCoeff: (Int) -> Double
        if (newDegree == this.degree()) {
            thisCoeff = { i -> this.coeffs[i] }
            otherCoeff = { i -> other.coeff(i) }
        } else {
            thisCoeff = { i -> this.coeff(i) }
            otherCoeff = { i -> other.coeffs[i] }
        }
        val newCoeffs = DoubleArray(newDegree + 1)


        for (i in 0..newDegree) {
            newCoeffs[newDegree - i] = thisCoeff(i) + type * otherCoeff(i)
        }

        return Polynom(*newCoeffs)
    }

    operator fun plus(other: Polynom): Polynom = plusMinus(other, 1)

    /**
     * Смена знака (при всех слагаемых)
     */
    operator fun unaryMinus(): Polynom {
        val degree = this.degree()
        val newCoeffs = DoubleArray(degree + 1)
        for (i in newCoeffs.indices) {
            newCoeffs[degree - i] = -coeffs[i]
        }
        return Polynom(*newCoeffs)
    }

    /**
     * Вычитание
     */
    operator fun minus(other: Polynom): Polynom = plusMinus(other, -1)

    /**
     * Умножение
     */
    operator fun times(other: Polynom): Polynom {
        val newDegree = this.degree() + other.degree()
        val newCoeffs = DoubleArray(newDegree + 1)
        for (i in 0..this.degree()) {
            for (j in 0..other.degree()) {
                newCoeffs[newDegree - i - j] += this.coeff(i) * other.coeff(j)
            }
        }
        return Polynom(*newCoeffs)
    }

    /**
     * Деление
     *
     * Про операции деления и взятия остатка см. статью Википедии
     * "Деление многочленов столбиком". Основные свойства:
     *
     * Если A / B = C и A % B = D, то A = B * C + D и степень D меньше степени B
     */

    private fun division(other: Polynom): Pair<List<Double>, DoubleArray> {
        val dividendCoeffs = coeffs.reversed().toDoubleArray()
        val otherDegree = other.degree()
        val thisDegree = this.degree()
        if (otherDegree > thisDegree) {
            return Pair(listOf(0.0), coeffs)
        }
        val result = mutableListOf<Double>()
        for (position in 0..(thisDegree - otherDegree)) {
            val currentFraction = dividendCoeffs[position] / other.coeff(otherDegree)
            result.add(currentFraction)

            val currentSubtrahend = DoubleArray(otherDegree + 1)
            for (i in currentSubtrahend.indices) {
                currentSubtrahend[i] = other.coeffs[otherDegree - i] * currentFraction
            }
            for (i in 0..otherDegree) {
                dividendCoeffs[i + position] -= currentSubtrahend[i]
            }
        }
        return Pair(result, dividendCoeffs)
    }

    operator fun div(other: Polynom): Polynom = Polynom(*division(other).first.toDoubleArray())

    /**
     * Взятие остатка
     */
    operator fun rem(other: Polynom): Polynom = Polynom(*division(other).second)

    /**
     * Сравнение на равенство
     */

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Polynom) return false
        if (this.degree() != other.degree()) return false
        if (this.coeffs.contentEquals(other.coeffs)) return true
        return false
    }

    /**
     * Получение хеш-кода
     */
    override fun hashCode(): Int {
        var result = 13
        for (coeff in coeffs) {
            result = (result * 31) + coeff.hashCode()
        }
        return result
    }
}
