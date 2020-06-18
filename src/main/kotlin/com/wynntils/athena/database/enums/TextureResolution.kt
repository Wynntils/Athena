package com.wynntils.athena.database.enums

enum class TextureResolution(

    private val order: Int,
    val width: Int,
    val height: Int

) {

    R_64_32(0, 64, 32),
    R_128_64(1, 128, 64),
    R_256_128(2, 256, 128),
    R_512_256(3, 512, 256),
    R_1024_512(4, 1024, 512),
    R_2048_1024(5, 2048, 1024);

    fun isLowerOrEqual(compare: TextureResolution): Boolean {
        return compare.order <= this.order
    }

    companion object {

        fun fromWidth(width: Int): TextureResolution {
            for (res in values()) {
                if (res.width != width) continue

                return res
            }

            return R_64_32
        }

    }
}