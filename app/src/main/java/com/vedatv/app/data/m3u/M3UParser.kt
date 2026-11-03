package com.vedatv.app.data.m3u

import com.vedatv.app.data.db.ChannelEntity

object M3UParser {

    fun parse(text: String, playlistId: Long): List<ChannelEntity> {
        val channels = mutableListOf<ChannelEntity>()
        var currentName = ""
        var currentGroup = ""
        var currentLogo = ""

        text.lines().forEach { rawLine ->
            val line = rawLine.trim()

            when {
                line.startsWith("#EXTINF") -> {
                    currentName = extractTitle(line)
                    currentGroup = extractAttribute(line, "group-title")
                    currentLogo = extractAttribute(line, "tvg-logo")
                }

                line.isNotEmpty() && !line.startsWith("#") -> {
                    if (currentName.isNotBlank()) {
                        channels += ChannelEntity(
                            playlistId = playlistId,
                            name = currentName,
                            group = currentGroup.ifBlank { "Без категории" },
                            logo = currentLogo,
                            url = line
                        )
                    }
                    currentName = ""
                    currentGroup = ""
                    currentLogo = ""
                }
            }
        }

        return channels
    }

    private fun extractTitle(line: String): String {
        val afterComma = line.substringAfterLast(',')
        return afterComma.trim().ifBlank { "Без названия" }
    }

    private fun extractAttribute(line: String, key: String): String {
        val marker = "$key=\""
        val start = line.indexOf(marker)
        if (start == -1) return ""
        val valueStart = start + marker.length
        val end = line.indexOf('"', valueStart)
        return if (end == -1) "" else line.substring(valueStart, end)
    }
}
