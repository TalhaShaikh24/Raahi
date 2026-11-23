package com.example.core

object Formatters {
    fun formatPkr(amount: Int): String {
        val raw = amount.toString()
        if (raw.length <= 3) return "PKR $raw"
        val sb = StringBuilder(raw.length + 6)
        sb.append("PKR ")
        var i = 0
        if (raw[0] == '-') {
            sb.append('-')
            i = 1
        }
        val digits = raw.length - i
        var rem = digits % 3
        if (rem == 0) rem = 3
        sb.append(raw, i, i + rem)
        var idx = i + rem
        while (idx < raw.length) {
            sb.append(',')
            sb.append(raw, idx, idx + 3)
            idx += 3
        }
        return sb.toString()
    }

    fun formatDuration(minutes: Int): String {
        val hrs = minutes / 60
        val mins = minutes % 60
        return when {
            hrs > 0 && mins > 0 -> "${hrs}h ${mins}m"
            hrs > 0 -> "${hrs}h"
            else -> "${mins}m"
        }
    }

    fun formatSeats(count: Int): String {
        return if (count == 1) "1 seat" else "$count seats"
    }
}
