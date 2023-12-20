package com.salesrep.app.util

import android.content.Context
import android.text.format.DateUtils
import com.salesrep.app.util.DateFormat.DELIVERY_DATE_FORMAT

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by Vipin.
 */

object DateUtils {

    val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun getCurrentDate(): String {

        val c = Calendar.getInstance()

        utcFormat.timeZone = TimeZone.getTimeZone("UTC")

        return utcFormat.format(c.time)
    }

    fun getCurrentDateWithFormat(format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())

        val c = Calendar.getInstance()

//        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        return dateFormat.format(c.time)
    }

    fun isValidDiscount(startdate: String?, enddate: String?): Boolean {
        val bool= if (!startdate.isNullOrEmpty() && !enddate.isNullOrEmpty()) {

            (
                    !compareDateWithFormat(DateFormat.DATE_FORMAT_RENEW, startdate)
                            &&
                           compareDateWithFormat(
                                DateFormat.DATE_FORMAT_RENEW,
                                enddate
                            )
                    )

        } else if (!startdate.isNullOrEmpty()) {
            !compareDateWithFormat(DateFormat.DATE_FORMAT_RENEW, startdate)

        } else if (!enddate.isNullOrEmpty()) {
            compareDateWithFormat(
                DateFormat.DATE_FORMAT_RENEW,
                enddate
            )
        } else
            true

        return bool
    }

    fun compareDateWithFormat(format: String, compareDate: String): Boolean {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())

        val c = Calendar.getInstance()

        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date= dateFormat.parse(compareDate)

        val bool= date?.after(c.time) ?: false

        return bool
    }

    fun compareDateWithTodayFormat(format: String, compareDate: String): Boolean {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())

        val c = Calendar.getInstance()
        val todayString = dateFormat.format(c.time)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = dateFormat.parse(compareDate)
        val today = dateFormat.parse(todayString)

        return date?.equals(today) ?: false
    }


    fun getTimeDifferenceInMins(format: String, startTime: String?, endTime: String?): Long {
        return try {
            val simpleDateFormat = SimpleDateFormat(format)
            val startDate = simpleDateFormat.parse(startTime)
            val endDate = simpleDateFormat.parse(endTime)

            val difference = endDate.time - startDate.time
            val mins = difference / 60000
             mins
        }  catch (e: Exception) {
                e.printStackTrace()
                0
        }
    }

    fun toDate(timestamp: Long): String {
        val date = Date(timestamp)
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(date)
    }

    fun toDateTime(timestamp: Long): Date {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd, hh:mm", Locale.ENGLISH)
        val dateString = dateFormat.format(date)
        return dateFormat.parse(dateString)
    }

    fun toTimeStamp(date: String): Long {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date).time
    }

    fun dateFormatFromMillis(format: String, timeInMillis: Long?): String {
        val fmt = SimpleDateFormat(format, Locale.ENGLISH)
        return if (timeInMillis == null || timeInMillis == 0L)
            ""
        else
            fmt.format(timeInMillis)
    }

    fun dateFormatChange(formatFrom: String, formatTo: String, value: String?): String {
        return try {

            val originalFormat = SimpleDateFormat(formatFrom, Locale.ENGLISH)
            val targetFormat = SimpleDateFormat(formatTo, Locale.ENGLISH)
            val date = originalFormat.parse(value)
            val formattedDate = targetFormat.format(date)
            formattedDate
        }catch (e: Exception){
            e.printStackTrace()
            ""
        }
    }

    fun getMilliFromDate(dateFormat: String, format: String): Long {
        var date = Date()
        val formatter = SimpleDateFormat(format, Locale.ENGLISH)
        try {
            date = formatter.parse(dateFormat)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return date.time
    }

    fun getTimeAgo(createdAt: String?, removeAgo: String): String {
        utcFormat.timeZone = TimeZone.getTimeZone("GMT")

        var agoString = ""

        createdAt?.let {
            val time = utcFormat.parse(it).time
            val now = System.currentTimeMillis()

            val ago = DateUtils.getRelativeTimeSpanString(
                time, now, DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )

            agoString = ago.toString().removeSuffix(removeAgo)
        }

        return agoString
    }

    fun getTimeAgoForMillis(millis: Long): String {

        val now = System.currentTimeMillis()

        return DateUtils.getRelativeTimeSpanString(
            millis, now, DateUtils.SECOND_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    fun getLocalTimeAgo(timeString: Long?, removeAgo: String): String {
        var agoString = ""

        timeString?.let {
            val now = System.currentTimeMillis()

            val ago = DateUtils.getRelativeTimeSpanString(
                timeString,
                now,
                DateUtils.SECOND_IN_MILLIS,
                DateUtils.FORMAT_SHOW_TIME
            )

            agoString = ago.toString()
        }

        return agoString
    }

    fun getMsgTime(currentTimeStamp: Long, previousTimeStamp: Long, context: Context): String {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()

        cal1.timeInMillis = currentTimeStamp

        if (previousTimeStamp != 0L) {
            cal2.timeInMillis = previousTimeStamp
        }

//        return when {
//            DateUtils.isToday(currentTimeStamp) -> context.getString(R.string.today)
//            isYesterday(cal1.time) -> context.getString(R.string.yesterday)
//            else -> SimpleDateFormat(MON_DAY_YEAR, Locale.ENGLISH).format(currentTimeStamp)
//        }
        return ""
    }

    fun isYesterday(d: Date): Boolean {
        return DateUtils.isToday(d.time + DateUtils.DAY_IN_MILLIS)
    }

    fun dateFormatFromUTC(createdDate: String?): String {
        return if (createdDate == null || createdDate.isEmpty())
            ""
        else {
            utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

            val fmt = SimpleDateFormat(DELIVERY_DATE_FORMAT, Locale.getDefault())
            fmt.format(utcFormat.parse(createdDate))
        }
    }

    fun dateTimeFormatFromUTC(createdDate: String?): String {
        return if (createdDate == null || createdDate.isEmpty())
            ""
        else {
            utcFormat.timeZone = TimeZone.getTimeZone("Etc/UTC")

            val fmt = SimpleDateFormat(DateFormat.DATE_TIME_FORMAT, Locale.getDefault())
            fmt.format(utcFormat.parse(createdDate))
        }
    }

    private fun getTimeDifferenceInHours(timestamp: Long): Long {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        return currentTimeStamp - timestamp
    }

    fun getRequestRemainingTime(chatExpiresTime: Long): Long {
        val currentTimeStamp = Calendar.getInstance().timeInMillis
        return chatExpiresTime - currentTimeStamp
    }
}
