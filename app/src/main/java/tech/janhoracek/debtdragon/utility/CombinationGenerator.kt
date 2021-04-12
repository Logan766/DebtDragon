package tech.janhoracek.debtdragon.utility

/**
 * Combination generator
 *
 * @param T as any
 * @property items as list of items
 * @constructor
 *
 * @param choose combination number
 */
class CombinationGenerator<T>(private val items: List<T>, choose: Int = 1) : Iterator<List<T>>, Iterable<List<T>> {
    private val indices = Array(choose) { it }
    private var first = true

    init {
        if (items.isEmpty() || choose > items.size || choose < 1)
            error("list must have more than 'choose' items and 'choose' min is 1")
    }

    override fun hasNext(): Boolean = indices.filterIndexed { index, it ->
        when (index) {
            indices.lastIndex -> items.lastIndex > it
            else -> indices[index + 1] - 1 > it
        }
    }.any()

    override fun next(): List<T> {
        if (!hasNext()) error("No more")
        if (!first) {
            incrementAndCarry()
        } else
            first = false
        return List(indices.size) { items[indices[it]] }
    }

    /**
     * Increment and carry
     *
     */
    private fun incrementAndCarry() {
        var carry = false
        var place = indices.lastIndex
        do {
            carry = if ((place == indices.lastIndex && indices[place] < items.lastIndex)
                || (place != indices.lastIndex && indices[place] < indices[place + 1] - 1)) {
                indices[place]++
                (place + 1..indices.lastIndex).forEachIndexed { index, i ->
                    indices[i] = indices[place] + index + 1
                }
                false
            } else
                true
            place--
        } while (carry && place > -1)
    }

    override fun iterator(): Iterator<List<T>> = this
}

fun main() {
    val combGen = CombinationGenerator(listOf(1, 2, 3, 4), 3)
    combGen.map { println(it.joinToString()) }

}