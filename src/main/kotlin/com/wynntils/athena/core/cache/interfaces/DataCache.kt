package com.wynntils.athena.core.cache.interfaces

import org.json.simple.JSONAware

interface DataCache {

    /**
     * That's what the cache is, generate the data here
     */
    fun generateCache(): JSONAware

    /**
     * Used for loading persistent data through the instance.
     * Called BEFORE generateCache
     *
     * Input is null if there's no persistent data stored
     *
     * Persistent data are kept between restarts.
     */
    fun loadPersistentData(input: String?) {}

    /**
     * Use this for generating the persistent data.
     * Called AFTER generateCache
     *
     * Return null if there's no persistent data to be stored.
     *
     * Persistent data are kept between restarts.
     */
    fun generatePersistentData(): String? {
        return null
    }

}