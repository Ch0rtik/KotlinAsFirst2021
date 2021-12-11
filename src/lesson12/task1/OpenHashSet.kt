@file:Suppress("UNUSED_PARAMETER")

package lesson12.task1

/**
 * Класс "хеш-таблица с открытой адресацией"
 *
 * Общая сложность задания -- сложная, общая ценность в баллах -- 20.
 * Объект класса хранит данные типа T в виде хеш-таблицы.
 * Хеш-таблица не может содержать равные по equals элементы.
 * Подробности по организации см. статью википедии "Хеш-таблица", раздел "Открытая адресация".
 * Методы: добавление элемента, проверка вхождения элемента, сравнение двух таблиц на равенство.
 * В этом задании не разрешается использовать библиотечные классы HashSet, HashMap и им подобные,
 * а также любые функции, создающие множества (mutableSetOf и пр.).
 *
 * В конструктор хеш-таблицы передаётся её вместимость (максимальное количество элементов)
 */
class OpenHashSet<T>(val capacity: Int) {

    private var added = 0

    /**
     * Массив для хранения элементов хеш-таблицы
     */
    internal val elements: Array<T?> = Array<Any?>(capacity) { null } as Array<T?>
    // "Cannot use 'T' as reified type parameter. Use a class instead." ???

    /**
     * Число элементов в хеш-таблице
     */
    val size: Int get() = added

    /**
     * Признак пустоты
     */
    fun isEmpty(): Boolean = size == 0

    /**
     * Добавление элемента.
     * Вернуть true, если элемент был успешно добавлен,
     * или false, если такой элемент уже был в таблице, или превышена вместимость таблицы.
     */
    // Линейное пробирование
    private fun findKey(element: T): Int {
        var key = element.hashCode() % capacity
        if (key < 0) {
            key += capacity
        }
        if (elements[key] == null || elements[key] == element) return key
        val start = key
        key = (key + 1) % capacity
        while (elements[key] != null && elements[key] != element && key != start) {
            key = (key + 1) % capacity
        }
        return key
    }

    fun add(element: T): Boolean {
        if (size == capacity) return false
        val key = findKey(element)
        if (elements[key] != null) return false
        elements[key] = element
        added += 1
        return true
    }


    // Немного самодеятельности
    // P. S. "Ленивый алгоритм" с маркировкой мест удаления - для неудачников

    fun delete(element: T): Boolean {
        var key = findKey(element)

        if (elements[key] == null) return false

        var tail = (key + 1) % capacity

        while (elements[tail] != null && tail != key) {
            var currentFirstKey = elements[tail].hashCode() % capacity
            if (currentFirstKey < 0) {
                currentFirstKey += capacity
            }

            if ((tail < key && (currentFirstKey in (tail + 1)..key)) || (tail > key && (currentFirstKey <= key || currentFirstKey > tail))){
                elements[key] = elements[tail]
                key = tail
            }
            tail = (tail + 1) % capacity
        }
        elements[key] = null

        added--
        return true
    }

    /**
     * Проверка, входит ли заданный элемент в хеш-таблицу
     */
    operator fun contains(element: T): Boolean {
        val key = findKey(element)
        if (elements[key] == element) return true
        return false
    }

    /**
     * Таблицы равны, если в них одинаковое количество элементов,
     * и любой элемент из второй таблицы входит также и в первую
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OpenHashSet<T>
        if (other.size != size) {
            return false
        }
        for (element in elements) {
            if (element != null && !other.contains(element)) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var result = 13

        val elementsForHash = Array<Any?>(added) { null } as Array<T>
        var i = 0

        for (element in elements) {
            if (element != null) {
                elementsForHash[i] = element
                i++
            }
        }

        elementsForHash.sort()

        for (element in elementsForHash) {
            result = (result * 31) + element.hashCode()
        }

        return result
    }
}