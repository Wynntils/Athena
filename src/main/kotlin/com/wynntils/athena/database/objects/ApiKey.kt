package com.wynntils.athena.database.objects

import com.wynntils.athena.core.email.EmailManager
import com.wynntils.athena.core.email.enums.EmailTemplate
import com.wynntils.athena.core.email.objects.EmailRecipient
import com.wynntils.athena.core.logDateFormat
import com.wynntils.athena.core.textDateFormat
import com.wynntils.athena.database.interfaces.DatabaseObject
import java.util.*

data class ApiKey(
    override val _id: String,

    var name: String,
    var description: String,
    var adminContact: List<String>,

    var maxLimit: Int,

    var dailyRequests: HashMap<String, Int> = HashMap(),

    override val table: String = "apiKey"
) : DatabaseObject {

    fun addRequest() {
        val date = logDateFormat.format(Date());
        dailyRequests[date] = dailyRequests.getOrPut(date, { 0 }) + 1

        asyncSave()
    }

    fun sendRateLimitWarning(amount: Int) {
        val recipients = ArrayList<EmailRecipient>()
        adminContact.filter { it.contains("@") }.forEach { recipients.add(EmailRecipient(name, it)) }

        EmailManager.sendEmail(
            recipients, "You're being Rate Limited", EmailTemplate.WARNING,
            "❌ You're being Rate Limited ❌",
            "https://cdn.wynntils.com/emoji_sweaty.png",
            "75",
            "Hello $name, </br></br>I've detected that your API Key is being Rate Limited, " +
                    "that means you're not going to be able to make any more calls for the next 10 minutes. " +
                    "If you think that's an issue, please contact the Wynntils Team.</br></br>" +
                    "<b>${textDateFormat.format(Date())} - $amount requests of $maxLimit</b></br></br>" +
                    "This is an automated message by Athena."
        )
    }

}