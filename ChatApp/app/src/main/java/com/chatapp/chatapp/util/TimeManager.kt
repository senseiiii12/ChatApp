package com.chatapp.chatapp.util

import com.chatapp.chatapp.domain.models.Message
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class TimeManager {
     fun getTimeSinceLastMessage(lastMessage: Message?): String {
        if (lastMessage == null) {
            return "non message"
        }

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastMessage.timestamp

        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
        val weeks = days / 7

         val calendar = Calendar.getInstance().apply { time = Date(lastMessage.timestamp) }
         val currentCalendar = Calendar.getInstance()

         val years = currentCalendar.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
         val months = (currentCalendar.get(Calendar.MONTH) - calendar.get(Calendar.MONTH)) +
                 12 * years

         return when {
             years > 0 -> "$years ${pluralize(years.toLong(), "y", "y", "y")} ago"
             months > 0 -> "${calendar.get(Calendar.DAY_OF_MONTH)} ${getMonthName(calendar.get(Calendar.MONTH))}"
             weeks > 0 -> "$weeks ${pluralize(weeks, "w", "w", "w")} ago"
             days > 0 -> "$days ${pluralize(days, "d", "d", "d")} ago"
             hours > 0 -> "$hours ${pluralize(hours, "h", "h", "h")} ago"
             minutes > 0 -> "$minutes ${pluralize(minutes, "min", "min", "min")} ago"
             else -> if(seconds < 0){
                 "just now"
             } else "$seconds ${pluralize(seconds, "sec", "sec", "sec")} ago"

         }
    }

    fun pluralize(number: Long, one: String, few: String, many: String): String {
        val n = number % 100
        return when {
            n in 11..19 -> many
            else -> when (n % 10) {
                1L -> one
                in 2L..4L -> few
                else -> many
            }
        }
    }

    fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return months[month]
    }


    fun formatLastSeenDate(lastSeen: Date): String {
        // Форматирование для времени
        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        timeFormat.timeZone = TimeZone.getDefault()

        // Форматирование для даты и времени
        val dateTimeFormat = SimpleDateFormat("d MMMM yyyy 'в' HH:mm", Locale.ENGLISH)
        dateTimeFormat.timeZone = TimeZone.getDefault()

        // Форматирование для даты
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        dateFormat.timeZone = TimeZone.getDefault()

        val currentDate = Date()
        val lastSeenDateString = dateFormat.format(lastSeen)
        val currentDateString = dateFormat.format(currentDate)

        return if (lastSeenDateString == currentDateString) {
            "Last seen today ${timeFormat.format(lastSeen)}"
        } else {
            "Last seen ${dateTimeFormat.format(lastSeen)}"
        }
    }


    fun showDateSeparator(previousMessage: Message?, currentMessage: Message): Boolean {
        if (previousMessage == null) return true

        val previousDate = Date(previousMessage.timestamp).toStartOfDay()
        val currentDate = Date(currentMessage.timestamp).toStartOfDay()

        return previousDate != currentDate
    }

    fun Date.toStartOfDay(): Date {
        val calendar = Calendar.getInstance()
        calendar.time = this
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}