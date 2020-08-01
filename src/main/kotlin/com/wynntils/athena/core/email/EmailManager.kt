package com.wynntils.athena.core.email

import com.wynntils.athena.core.configs.emailConfig
import com.wynntils.athena.core.email.enums.EmailTemplate
import com.wynntils.athena.core.email.objects.EmailRecipient
import com.wynntils.athena.core.runAsync
import com.wynntils.athena.generalLog
import org.simplejavamail.email.EmailBuilder
import org.simplejavamail.mailer.MailerBuilder

object EmailManager {

    private val mailer = MailerBuilder
        .withSMTPServer(emailConfig.host, emailConfig.port, emailConfig.user, emailConfig.password)
        .withSessionTimeout(10000)
        .buildMailer()

    fun sendEmail(recipients: List<EmailRecipient>, subject: String, message: String) {
        val builder = EmailBuilder.startingBlank()

        recipients.forEach { builder.to(it.name, it.address) }
        builder.withSubject(subject)
        builder.withHTMLText(message)
        builder.from("Athena", emailConfig.user)

        runAsync {
            mailer.sendMail(builder.buildEmail())
        }

        generalLog.info("Sending E-MAIL to " + recipients.map { it.address }.toString() + " ($subject)")
    }

    fun sendEmail(recipients: List<EmailRecipient>, subject: String, template: EmailTemplate, vararg strings: String) {
        sendEmail(recipients, subject, template.getInput(*strings))
    }

}