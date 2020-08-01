package com.wynntils.athena.core.email.enums

import com.wynntils.athena.templateDatabase

enum class EmailTemplate {

    WARNING("warning-template.html", "title", "banner", "size", "message");

    val content: String
    private val args: List<String>

    constructor(fileName: String, vararg replace: String) {
        val table = templateDatabase.getOrCreateTable("email")
        if (!table.hasFile(fileName)) {
            content = "Invalid Template File"
            args = emptyList()
            return
        }

        content = table.getFile(fileName)!!.asString()
        args = replace.toList()
    }

    fun getInput(vararg strings: String): String {
        var result = content

        for (i in args.indices) {
            result = result.replace("%${args[i]}%", strings.getOrElse(i) { "Not Set" })
        }

        return result
    }

}