package de.ovgu.ikus.utils

/**
 * moves the [item] towards the head of the list
 * @param item the item which should be moved
 * @param equals the function to compare two items
 * @param setIndex the function to set the index of one item, applied to all items in the list
 * @return the new list if changed, null otherwise
 */
fun<T> List<T>.moveUpItem(item: T, equals: (a: T, b: T) -> Boolean, setIndex: (item: T, index: Int) -> Unit): List<T>? {
    val position = this.indexOfFirst { el -> equals(el, item) }

    if (position != 0) {
        val result = this.toMutableList()
        result.removeAt(position)
        result.add(position - 1, item)
        result.forEachIndexed { index, currItem -> setIndex(currItem, index) }
        return result
    }

    // nothing changed
    return null
}

/**
 * moves the [item] towards the tail of the list
 * @param item the item which should be moved
 * @param equals the function to compare two items
 * @param setIndex the function to set the index of one item, applied to all items in the list
 * @return the new list if changed, null otherwise
 */
fun<T> List<T>.moveDownItem(item: T, equals: (a: T, b: T) -> Boolean, setIndex: (item: T, index: Int) -> Unit): List<T>? {
    val position = this.indexOfFirst { el -> equals(el, item) }

    if (position != this.size - 1) {
        val result = this.toMutableList()
        result.removeAt(position)
        result.add(position + 1, item)
        result.forEachIndexed { index, currItem -> setIndex(currItem, index) }
        return result
    }

    // nothing changed
    return null
}