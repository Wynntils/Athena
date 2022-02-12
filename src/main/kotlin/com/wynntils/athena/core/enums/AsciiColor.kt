package com.wynntils.athena.core.enums

enum class AsciiColor(

    val ascii: String

) {

    RESET("\u001B[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m");

    operator fun plus(s: String): String {
        return "$ascii$s${RESET.ascii}"
    }

    companion object {

        fun removeColors(input: String): String {
            var output = input

            for (color in values()) {
                output = output.replace(color.ascii, "")
            }

            return output
        }

    }

}
