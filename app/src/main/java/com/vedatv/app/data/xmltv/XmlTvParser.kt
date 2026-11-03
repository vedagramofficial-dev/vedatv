package com.vedatv.app.data.xmltv

import com.vedatv.app.data.db.EpgProgramEntity
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.Reader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object XmlTvParser {

    fun parse(reader: Reader): List<EpgProgramEntity> {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(reader)

        val programs = mutableListOf<EpgProgramEntity>()
        var currentChannelId = ""
        var currentTitle = ""
        var currentDescription = ""
        var currentStart = 0L
        var currentEnd = 0L

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "programme" -> {
                        currentChannelId = parser.getAttributeValue(null, "channel").orEmpty()
                        currentStart = parseXmlTvDate(parser.getAttributeValue(null, "start"))
                        currentEnd = parseXmlTvDate(parser.getAttributeValue(null, "stop"))
                        currentTitle = ""
                        currentDescription = ""
                    }

                    "title" -> currentTitle = parser.nextText().trim()
                    "desc" -> currentDescription = parser.nextText().trim()
                }

                XmlPullParser.END_TAG -> when (parser.name) {
                    "programme" -> {
                        if (currentChannelId.isNotBlank() && currentTitle.isNotBlank()) {
                            programs += EpgProgramEntity(
                                channelId = currentChannelId,
                                title = currentTitle,
                                description = currentDescription,
                                startTime = currentStart,
                                endTime = currentEnd
                            )
                        }
                    }
                }
            }

            event = parser.next()
        }

        return programs
    }

    private fun parseXmlTvDate(value: String?): Long {
        if (value.isNullOrBlank()) return 0L
        return try {
            val cleanValue = value.take(14)
            val format = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.parse(cleanValue)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
