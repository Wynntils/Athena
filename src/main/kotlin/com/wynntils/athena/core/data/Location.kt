package com.wynntils.athena.core.data

data class Location(

    val x: Int,
    val y: Int,
    val z: Int

) {

    override fun toString(): String {
        return "${x}:${y}:${z}"
    }

    companion object {

        /**
         * Tries to generate a Location object based on the input string
         * @return Location if possible otherwise null
         */
        fun fromString(input: String): Location? {
            if (input.isEmpty() || !input.contains(':')) return null

            // this is faster than calculating a regex
            val split = input.split(':')
            if (split.size < 3) return null

            return Location(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }

    }
}