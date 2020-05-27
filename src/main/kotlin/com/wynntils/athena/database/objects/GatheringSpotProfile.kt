package com.wynntils.athena.database.objects

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wynntils.athena.core.currentTimeMillis
import com.wynntils.athena.core.data.Location
import com.wynntils.athena.database.enums.GatheringMaterial
import com.wynntils.athena.database.enums.GatheringType
import com.wynntils.athena.database.interfaces.RethinkObject
import java.util.*
import kotlin.collections.HashSet

data class GatheringSpotProfile(
    override val id: String,

    val type: GatheringType,
    val material: GatheringMaterial,

    var lastSeen: Long = currentTimeMillis(),
    val users: HashSet<UUID> = HashSet(),

    override val table: String = "gathering"
): RethinkObject {

    @JsonIgnore
    private var location: Location? = null

    /**
     * Variates between 0 and 100
     * Represents how reliable is the provided spot
     *
     * @return an Integer in the range of [0, 100]
     */
    fun calculateReliability(): Int {
        // 1296000000 is 15 days in milliseconds
        return (100 * (
                (1.0 - ((currentTimeMillis() - lastSeen) / 1296000000)) // calculates scalable factor | 15 days = 0
                * (25.coerceAtLeast(users.size) / 25) // multiply the scalable factor based on the amount players | max = 25
                ))
            .toInt()
    }

    /**
     * Returns if the provided spot should be removed from the database
     * That means the last time it was seen was more than 15 days ago
     *
     * @return if it should be removed
     */
    fun shouldRemove(): Boolean {
        // 1296000000 is 15 days in milliseconds
        return ((currentTimeMillis() - lastSeen) >= 1296000000)
    }

    /**
     * Generates and returns the Location object based on the object id
     *
     * @return a Location object
     */
    fun getLocation(): Location {
        if (location == null) location = Location.fromString(id)

        return location!!
    }

}