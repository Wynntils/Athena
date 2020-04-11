package com.wynntils.athena.core.profiler

import com.wynntils.athena.core.nanoTime
import com.wynntils.athena.core.profiler.data.Section
import com.wynntils.athena.core.utils.ExternalNotifications
import com.wynntils.athena.generalLog

private val sections = HashMap<String, Section>()

fun <T> profile(name: String, runnable: () -> T): T {
    val time = nanoTime()
    val result = runnable()
    sections[name] = Section(name, nanoTime() - time)

    if ((nanoTime() - time) >= 200000000) {
        generalLog.warn("${name.replace("-", " -> ")} took more than 200ms to proceed!")

        ExternalNotifications.sendMessage(
            title = null,
            description = "``${name.replace("-", " -> ")}`` took more than 200ms to proceed!",
            color = 16711680)
    }

    return result
}

fun getSections(): MutableCollection<Section> {
    return sections.values
}

fun getSection(section: String): Section? {
    return sections.getOrDefault(section, null)
}

fun getSectionMs(section: String): Long {
    return getSection(section)?.time?.div(1000000L) ?: 0
}