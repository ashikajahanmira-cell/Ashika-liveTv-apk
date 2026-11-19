package com.ashika.livetv

object M3uParser {
    fun parse(text: String): List<Channel> {
        val result = mutableListOf<Channel>()
        var name = ""
        var logo = ""
        var group = ""
        var country = ""
        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", true)) {
                name = line.substringAfterLast(",", "Unknown channel").trim().ifBlank { "Unknown channel" }
                logo = attr(line, "tvg-logo")
                group = attr(line, "group-title")
                country = attr(line, "tvg-country")
            } else if (line.startsWith("http://") || line.startsWith("https://")) {
                result += Channel(name.ifBlank { "Channel ${result.size + 1}" }, line, logo, group, country)
                name = ""; logo = ""; group = ""; country = ""
            }
        }
        return result.distinctBy { it.url }
    }

    private fun attr(line: String, key: String): String =
        Regex("""$key=[\"']([^\"']*)[\"']""", RegexOption.IGNORE_CASE)
            .find(line)?.groupValues?.getOrNull(1).orEmpty()
}
