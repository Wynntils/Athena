package com.wynntils.athena.database.enums

enum class ProfessionType(

    val icon: String,
    val leaderboard: String

) {

    // gathering
    WOODCUTTING("Ⓒ", "solo/woodcutting"),
    MINING("Ⓑ", "solo/mining"),
    FISHING("Ⓚ", "solo/fishing"),
    FARMING("Ⓙ", "solo/farming"),

    // crafting
    ALCHEMISM("Ⓛ", "solo/alchemism"),
    ARMOURING("Ⓗ", "solo/armouring"),
    COOKING("Ⓐ", "solo/cooking"),
    JEWELING("Ⓓ", "solo/jeweling"),
    SCRIBING("Ⓔ", "solo/scribing"),
    TAILORING("Ⓕ", "solo/tailoring"),
    WEAPONSMITHING("Ⓖ", "solo/weaponsmithing"),
    WOODWORKING("Ⓘ", "solo/woodworking"),

    OVERALL("", "overall/all")


}