package com.wynntils.athena.database.data

import com.wynntils.athena.routes.managers.CapeManager

data class CosmeticInfo(

    var capeTexture: String = "",
    var elytraEnabled: Boolean = false,

    var earsEnabled: Boolean = false

) {

    private fun isTextureValid(): Boolean {
        return capeTexture.isNotEmpty() && CapeManager.isApproved(capeTexture)
    }

    fun getFormattedTexture(): String {
        if (isTextureValid()) return capeTexture
        return "defaultCape"
    }

    fun hasCape(): Boolean {
        return !elytraEnabled && isTextureValid()
    }

    fun hasElytra(): Boolean {
        return elytraEnabled && isTextureValid()
    }

}