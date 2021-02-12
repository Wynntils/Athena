package com.wynntils.athena.core.profiler

import com.wynntils.athena.core.nanoTime
import com.wynntils.athena.core.profiler.data.Section
import com.wynntils.athena.core.utils.ExternalNotifications
import com.wynntils.athena.generalLog
import java.util.*

private val sections = WeakHashMap<String, Section>()

fun <T> profile(name: String,notification: Boolean = true, runnable: () -> T): T {
    val time = nanoTime()
    val result = runnable()
    sections[name] = Section(name, nanoTime() - time)

    val difference = (nanoTime() - time)
    if (difference >= 4000000000) {
        generalLog.warn("${name.replace("-", " -> ")} took more than 200ms to proceed!")

        if (!notification) return result
        ExternalNotifications.sendMessage(
            title = null,
            description = "``${name.replace("-", " -> ")}`` took ${difference/1000000} ms to proceed!",
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