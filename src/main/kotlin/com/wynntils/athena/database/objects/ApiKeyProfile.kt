package com.wynntils.athena.database.objects

import com.wynntils.athena.core.logDateFormat
import com.wynntils.athena.database.interfaces.RethinkObject
import java.util.*
import kotlin.collections.HashMap

data class ApiKeyProfile(
    override val id: String,

    var name: String,
    var description: String,
    var adminContact: List<String>,

    var maxLimit: Int,

    var dailyRequests: HashMap<String, Int> = HashMap(),

    override val table: String = "apiKeys"
): RethinkObject {

    fun addRequest() {
        val date = logDateFormat.format(Date());
        dailyRequests[date] = dailyRequests.getOrPut(date, { 0 }) + 1

        asyncSave()
    }

    fun sendRateLimitWarning() {
        // TODO via email!
    }

}