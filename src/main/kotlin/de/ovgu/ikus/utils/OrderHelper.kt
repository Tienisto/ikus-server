package de.ovgu.ikus.utils


fun<T> List<T>.moveUpItem(item: T, getId: (item: T) -> Int, setIndex: (item: T, index: Int) -> Unit): List<T>? {
    val result = this.toMutableList()
    val position = result.indexOfFirst { el -> getId(el) == getId(item) }

    if (position != 0) {
        result.removeAt(position)
        result.add(position - 1, item)
        result.forEachIndexed { index, currItem -> setIndex(currItem, index) }
        return result
    }

    // nothing changed
    return null
}

fun<T> List<T>.moveDownItem(item: T, getId: (item: T) -> Int, setIndex: (item: T, index: Int) -> Unit): List<T>? {
    val result = this.toMutableList()
    val position = result.indexOfFirst { el -> getId(el) == getId(item) }

    if (position != result.size - 1) {
        result.removeAt(position)
        result.add(position + 1, item)
        result.forEachIndexed { index, currItem -> setIndex(currItem, index) }
        return result
    }

    // nothing changed
    return null
}