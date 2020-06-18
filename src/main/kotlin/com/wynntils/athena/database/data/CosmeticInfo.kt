package com.wynntils.athena.database.data

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wynntils.athena.database.enums.TextureResolution
import com.wynntils.athena.routes.managers.CapeManager

data class CosmeticInfo(

    var capeTexture: String = "",
    var elytraEnabled: Boolean = false,

    var parts: HashMap<String, Boolean> = HashMap(),

    var maxResolution: TextureResolution = TextureResolution.R_128_64,
    var allowAnimated: Boolean = false

) {

    private fun isTextureValid(): Boolean {
        return capeTexture.isNotEmpty() && CapeManager.isApproved(capeTexture)
    }

    @JsonIgnore
    fun getFormattedTexture(): String {
        if (isTextureValid()) return capeTexture
        return "defaultCape"
    }

    fun hasPart(part: String): Boolean {
        return parts.getOrDefault(part, false)
    }

    fun hasCape(): Boolean {
        return !elytraEnabled && isTextureValid()
    }

    fun hasElytra(): Boolean {
        return elytraEnabled && isTextureValid()
    }

}