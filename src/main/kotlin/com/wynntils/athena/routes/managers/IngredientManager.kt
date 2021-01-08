package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.utils.JSONOrderedObject
import org.json.simple.JSONObject

object IngredientManager {

    fun convertIngredient(input: JSONObject): JSONOrderedObject {
        val result = JSONOrderedObject()

        fun getStatusType(raw: String): String {
            return if (raw.contains("raw")) "INTEGER"
            else if (raw == "manaRegen" || raw == "lifeSteal" || raw == "manaSteal") "FOUR_SECONDS"
            else if (raw == "poison") "THREE_SECONDS"
            else if (raw == "attackSpeed") "TIER"
            else "PERCENTAGE"
        }

        fun ignoreZero(input: Any?): Any? {
            if (input == null) return null

            if (input is Number) return if (input.toInt() == 0) null else input
            else if (input is String) return if (input.isEmpty() || input == "0-0") return null else input
            return input
        }

        // basic info
        result["name"] = input["name"]
        result["tier"] = input["tier"]
        result["level"] = input["level"]
        result["untradeable"] = input.getOrDefault("untradeable", false)
        result["professions"] = input["skills"]

        // statuses
        val statuses = result.getOrCreate<JSONOrderedObject>("statuses")

        val identifications = input["identifications"] as JSONObject
        for (key in identifications.keys) {
            val identification = identifications[key] as JSONObject

            val translatedName = translateStatusName(key as String) ?: continue

            val status = statuses.getOrCreate<JSONOrderedObject>(translatedName)
            status["type"] = getStatusType(translatedName)
            status["minimum"] = identification["minimum"]
            status["maximum"] = identification["maximum"]
        }

        // itemModifiers
        val itemModifiers = result.getOrCreate<JSONOrderedObject>("itemModifiers")

        val itemIDs = input["itemOnlyIDs"] as JSONObject
        val consumableIDs = input.getOrDefault("consumableOnlyIDs", JSONObject()) as JSONObject

        itemModifiers["durability"] = ignoreZero(itemIDs.getOrDefault("durabilityModifier", null))
        itemModifiers["duration"] = ignoreZero(consumableIDs.getOrDefault("duration", null))
        itemModifiers["charges"] = ignoreZero(consumableIDs.getOrDefault("charges", null))

        itemModifiers["strength"] = ignoreZero(itemIDs.getOrDefault("strengthRequirement", null))
        itemModifiers["dexterity"] = ignoreZero(itemIDs.getOrDefault("dexterityRequirement", null))
        itemModifiers["intelligence"] = ignoreZero(itemIDs.getOrDefault("intelligenceRequirement", null))
        itemModifiers["defense"] = ignoreZero(itemIDs.getOrDefault("defenceRequirement", null))
        itemModifiers["agility"] = ignoreZero(itemIDs.getOrDefault("agilityRequirement", null))

        // ingredientModifiers
        val ingredientModifiers = result.getOrCreate<JSONOrderedObject>("ingredientModifiers")

        val modifiers = input["ingredientPositionModifiers"] as JSONObject

        ingredientModifiers["left"] = ignoreZero(modifiers.getOrDefault("left", null))
        ingredientModifiers["right"] = ignoreZero(modifiers.getOrDefault("right", null))
        ingredientModifiers["above"] = ignoreZero(modifiers.getOrDefault("above", null))
        ingredientModifiers["under"] = ignoreZero(modifiers.getOrDefault("under", null))
        ingredientModifiers["touching"] = ignoreZero(modifiers.getOrDefault("touching", null))
        ingredientModifiers["notTouching"] = ignoreZero(modifiers.getOrDefault("notTouching", null))

        // cleanups
        statuses.cleanNull()
        itemModifiers.cleanNull()
        ingredientModifiers.cleanNull()
        result.cleanNull()

        return result
    }

    private fun translateStatusName(raw: String): String? {
        return when(raw) {
            "STRENGTHPOINTS" -> "rawStrength"
            "DEXTERITYPOINTS" -> "rawDexterity"
            "INTELLIGENCEPOINTS" -> "rawIntelligence"
            "DEFENSEPOINTS" -> "rawDefence"
            "AGILITYPOINTS" -> "rawAgility"
            "DAMAGEBONUS" -> "mainAttackDamage"
            "DAMAGEBONUSRAW" -> "rawMainAttackNeutralDamage"
            "SPELLDAMAGE" -> "spellDamage"
            "SPELLDAMAGERAW" -> "rawNeutralSpellDamage"
            "HEALTHREGEN" -> "healthRegen"
            "HEALTHREGENRAW" -> "rawHealthRegen"
            "HEALTHBONUS" -> "rawHealth"
            "POISON" -> "poison"
            "LIFESTEAL" -> "lifeSteal"
            "MANAREGEN" -> "manaRegen"
            "MANASTEAL" -> "manaSteal"
            "SPELL_COST_PCT_1" -> "1stSpellCost"
            "SPELL_COST_RAW_1" -> "raw1stSpellCost"
            "SPELL_COST_PCT_2" -> "2ndSpellCost"
            "SPELL_COST_RAW_2" -> "raw2ndSpellCost"
            "SPELL_COST_PCT_3" -> "3rdSpellCost"
            "SPELL_COST_RAW_3" -> "raw3rdSpellCost"
            "SPELL_COST_PCT_4" -> "4thSpellCost"
            "SPELL_COST_RAW_4" -> "raw4thSpellCost"
            "THORNS" -> "thorns"
            "REFLECTION" -> "reflection"
            "ATTACKSPEED" -> "attackSpeed"
            "SPEED" -> "walkSpeed"
            "EXPLODING" -> "exploding"
            "SOULPOINTS" -> "soulPointRegen"
            "STAMINA" -> "sprint"
            "STAMINA_REGEN" -> "sprintRegen"
            "JUMP_HEIGHT" -> "rawJumpHeight"
            "XPBONUS" -> "xpBonus"
            "LOOTBONUS" -> "lootBonus"
            "LOOT_QUALITY" -> "lootQuality"
            "EMERALDSTEALING" -> "stealing"
            "EARTHDAMAGEBONUS" -> "earthDamage"
            "THUNDERDAMAGEBONUS" -> "thunderDamage"
            "WATERDAMAGEBONUS" -> "waterDamage"
            "FIREDAMAGEBONUS" -> "fireDamage"
            "AIRDAMAGEBONUS" -> "airDamage"
            "EARTHDEFENSE" -> "earthDefence"
            "THUNDERDEFENSE" -> "thunderDefence"
            "WATERDEFENSE" -> "waterDefence"
            "FIREDEFENSE" -> "fireDefence"
            "AIRDEFENSE" -> "airDefence"
            else -> null
        }
    }

}
