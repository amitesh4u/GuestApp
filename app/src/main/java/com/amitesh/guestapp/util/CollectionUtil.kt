package com.amitesh.guestapp.util

import java.util.*

fun <T> linkedListOf(vararg elements: T): LinkedList<T> {
    return LinkedList(elements.toList())
}
