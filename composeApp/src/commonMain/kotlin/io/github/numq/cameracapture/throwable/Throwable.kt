package io.github.numq.cameracapture.throwable

val Throwable.exception: Exception
    get() = when (this) {
        is Exception -> this

        else -> Exception(this)
    }