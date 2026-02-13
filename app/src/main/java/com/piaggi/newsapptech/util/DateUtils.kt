package com.piaggi.newsapptech.util

import java.util.Date

object DateUtils {

    private const val MILLIS_PER_HOUR = 1000 * 60 * 60
    private const val MILLIS_PER_DAY = MILLIS_PER_HOUR * 24

    fun formatRelativeTime(date: Date?): String {
        if (date == null) return ""

        val now = System.currentTimeMillis()
        val diff = now - date.time
        val hours = diff / MILLIS_PER_HOUR

        return when {
            hours < 1 -> "Just now"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            hours < 48 -> "Yesterday"
            else -> "${hours / 24} days ago"
        }
    }
}
