package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.utils.JSONOrderedObject
import com.wynntils.athena.database.enums.MajorIdentification
import org.json.simple.JSONArray
import org.json.simple.JSONObject

object ItemManager {

    fun convertItem(input: JSONObject): JSONOrderedObject {
        val result = JSONOrderedObject()

        fun isFixed(raw: String): Boolean {
            if (input["identified"] == true) return true

            return when (raw) {
                "rawStrength" -> true
                "rawDexterity" -> true
                "rawIntelligence" -> true
                "rawDefence" -> true
                "rawAgility" -> true
                else -> false
            }
        }

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

        // outside tags
        result["displayName"] =
            if (input.containsKey("displayName")) input["displayName"] else (input["name"] as String).replace(
                "֎",
                ""
            ) // cancer has ֎ in it name for a random reason
        result["tier"] = (input["tier"] as String).toUpperCase()
        result["identified"] = input.getOrDefault("identified", false)
        result["powderAmount"] = input["sockets"]
        result["attackSpeed"] = input["attackSpeed"]

        // item info
        val itemInfo = result.getOrCreate<JSONOrderedObject>("itemInfo")
        itemInfo["type"] = (input.getOrDefault("type", input["accessoryType"]) as String).toUpperCase()
        itemInfo["set"] = input["set"]
        itemInfo["dropType"] = (input["dropType"] as String).toUpperCase()
        itemInfo["armorColor"] = if (input["armorColor"] == "160,101,64") null else input["armorColor"]

        // requirements
        val requirements = result.getOrCreate<JSONOrderedObject>("requirements")
        requirements["quest"] = input.getOrDefault("quest", null)
        requirements["classType"] = if (input.containsKey("input") && input["classRequirement"] != null) {
            (input["classRequirement"] as String).toUpperCase()
        } else null
        requirements["level"] = input.getOrDefault("level", null)
        requirements["strength"] = input.getOrDefault("strength", null)
        requirements["dexterity"] = input.getOrDefault("dexterity", null)
        requirements["intelligence"] = input.getOrDefault("intelligence", null)
        requirements["defense"] = input.getOrDefault("defense", null)
        requirements["agility"] = input.getOrDefault("agility", null)

        // damageTypes
        val damageTypes = result.getOrCreate<JSONOrderedObject>("damageTypes")
        damageTypes["neutral"] = ignoreZero(input.getOrDefault("damage", null))
        damageTypes["earth"] = ignoreZero(input.getOrDefault("earthDamage", null))
        damageTypes["thunder"] = ignoreZero(input.getOrDefault("thunderDamage", null))
        damageTypes["water"] = ignoreZero(input.getOrDefault("waterDamage", null))
        damageTypes["fire"] = ignoreZero(input.getOrDefault("fireDamage", null))
        damageTypes["air"] = ignoreZero(input.getOrDefault("airDamage", null))

        // defenseTypes
        val defenseTypes = result.getOrCreate<JSONOrderedObject>("defenseTypes")
        defenseTypes["health"] = ignoreZero(input.getOrDefault("health", null))
        defenseTypes["earth"] = ignoreZero(input.getOrDefault("earthDefense", null))
        defenseTypes["thunder"] = ignoreZero(input.getOrDefault("thunderDefense", null))
        defenseTypes["water"] = ignoreZero(input.getOrDefault("waterDefense", null))
        defenseTypes["fire"] = ignoreZero(input.getOrDefault("fireDefense", null))
        defenseTypes["air"] = ignoreZero(input.getOrDefault("airDefense", null))

        // Pure Copy
        val statuses = result.getOrCreate<JSONOrderedObject>("statuses")

        result["majorIds"] = input.getOrDefault("majorIds", null)
        result["restriction"] = input["restrictions"]
        result["lore"] = input["addedLore"]

        for (key in input.keys) {
            val value = input[key]

            if (key == "armorType") {
                itemInfo["material"] =
                    "minecraft:${value}_${itemInfo["type"]}".toLowerCase().replace("chain", "chainmail")
                continue
            }
            if (key == "material" && value != null) {
                itemInfo["material"] = value
                continue
            }

            if (value !is Long || value == 0L) continue
            val translatedName = translateStatusName(key as String) ?: continue

            val status = statuses.getOrCreate<JSONOrderedObject>(translatedName)
            status["type"] = getStatusType(translatedName)
            status["isFixed"] = isFixed(translatedName)
            status["baseValue"] = value
        }

        // cleanups
        itemInfo.cleanNull()
        requirements.cleanNull()
        damageTypes.cleanNull()
        defenseTypes.cleanNull()
        statuses.cleanNull()
        result.cleanNull()

        return result
    }

    fun getIdentificationOrder(): JSONOrderedObject {
        val result = JSONOrderedObject()

        val order = result.getOrCreate<JSONOrderedObject>("order")
        // first group {SP stuff}
        order["rawStrength"] = 1
        order["rawDexterity"] = 2
        order["rawIntelligence"] = 3
        order["rawDefence"] = 4
        order["rawAgility"] = 5
        //second group {attack stuff}
        order["attackSpeed"] = 6
        order["rawMainAttackNeutralDamage"] = 7
        order["mainAttackDamage"] = 8
        order["rawNeutralSpellDamage"] = 9
        order["rawSpellDamage"] = 10
        order["spellDamage"] = 11
        //third group {health/mana stuff}
        order["rawHealth"] = 12
        order["rawHealthRegen"] = 13
        order["healthRegen"] = 14
        order["lifeSteal"] = 15
        order["manaRegen"] = 16
        order["manaSteal"] = 17
        //fourth group {damage stuff}
        order["earthDamage"] = 18
        order["thunderDamage"] = 19
        order["waterDamage"] = 20
        order["fireDamage"] = 21
        order["airDamage"] = 22
        //fifth group {defence stuff}
        order["earthDefence"] = 23
        order["thunderDefence"] = 24
        order["waterDefence"] = 25
        order["fireDefence"] = 26
        order["airDefence"] = 27
        //sixth group {passive damage}
        order["exploding"] = 28
        order["poison"] = 29
        order["thorns"] = 30
        order["reflection"] = 31
        //seventh group {movement stuff}
        order["walkSpeed"] = 32
        order["sprint"] = 33
        order["sprintRegen"] = 34
        order["rawJumpHeight"] = 35
        //eigth group {XP/Gathering stuff}
        order["soulPointRegen"] = 36
        order["lootBonus"] = 37
        order["lootQuality"] = 38
        order["emeraldStealing"] = 39
        order["xpBonus"] = 40
        order["gatherXPBonus"] = 41
        order["gatherSpeed"] = 42
        //ninth group {spell stuff}
        order["raw1stSpellCost"] = 43
        order["1stSpellCost"] = 44
        order["raw2ndSpellCost"] = 45
        order["2ndSpellCost"] = 46
        order["raw3rdSpellCost"] = 47
        order["3rdSpellCost"] = 48
        order["raw4thSpellCost"] = 49
        order["4thSpellCost"] = 50

        val groups = result.getOrCreate<JSONArray>("groups")
        groups += "1-5"
        groups += "6-11"
        groups += "12-17"
        groups += "18-22"
        groups += "23-27"
        groups += "28-31"
        groups += "32-35"
        groups += "36-42"
        groups += "43-50"

        val inverted = result.getOrCreate<JSONArray>("inverted")
        inverted += "1stSpellCost"
        inverted += "2ndSpellCost"
        inverted += "3rdSpellCost"
        inverted += "4thSpellCost"
        inverted += "raw1stSpellCost"
        inverted += "raw2ndSpellCost"
        inverted += "raw3rdSpellCost"
        inverted += "raw4thSpellCost"

        return result
    }

    fun getInternalIdentifications(): JSONOrderedObject {
        val result = JSONOrderedObject()

        result["STRENGTHPOINTS"] = "rawStrength"
        result["DEXTERITYPOINTS"] = "rawDexterity"
        result["INTELLIGENCEPOINTS"] = "rawIntelligence"
        result["DEFENSEPOINTS"] = "rawDefence"
        result["AGILITYPOINTS"] = "rawAgility"
        result["DAMAGEBONUS"] = "mainAttackDamage"
        result["DAMAGEBONUSRAW"] = "rawMainAttackNeutralDamage"
        result["SPELLDAMAGE"] = "spellDamage"
        result["SPELLDAMAGERAW"] = "rawNeutralSpellDamage"
        result["HEALTHREGEN"] = "healthRegen"
        result["HEALTHREGENRAW"] = "rawHealthRegen"
        result["HEALTHBONUS"] = "rawHealth"
        result["POISON"] = "poison"
        result["LIFESTEAL"] = "lifeSteal"
        result["MANAREGEN"] = "manaRegen"
        result["MANASTEAL"] = "manaSteal"
        result["SPELL_COST_PCT_1"] = "1stSpellCost"
        result["SPELL_COST_RAW_1"] = "raw1stSpellCost"
        result["SPELL_COST_PCT_2"] = "2ndSpellCost"
        result["SPELL_COST_RAW_2"] = "raw2ndSpellCost"
        result["SPELL_COST_PCT_3"] = "3rdSpellCost"
        result["SPELL_COST_RAW_3"] = "raw3rdSpellCost"
        result["SPELL_COST_PCT_4"] = "4thSpellCost"
        result["SPELL_COST_RAW_4"] = "raw4thSpellCost"
        result["THORNS"] = "thorns"
        result["REFLECTION"] = "reflection"
        result["ATTACKSPEED"] = "attackSpeed"
        result["SPEED"] = "walkSpeed"
        result["EXPLODING"] = "exploding"
        result["SOULPOINTS"] = "soulPointRegen"
        result["STAMINA"] = "sprint"
        result["STAMINA_REGEN"] = "sprintRegen"
        result["JUMP_HEIGHT"] = "rawJumpHeight"
        result["XPBONUS"] = "xpBonus"
        result["LOOTBONUS"] = "lootBonus"
        result["EMERALDSTEALING"] = "stealing"
        result["EARTHDAMAGEBONUS"] = "earthDamage"
        result["THUNDERDAMAGEBONUS"] = "thunderDamage"
        result["WATERDAMAGEBONUS"] = "waterDamage"
        result["FIREDAMAGEBONUS"] = "fireDamage"
        result["AIRDAMAGEBONUS"] = "airDamage"
        result["EARTHDEFENSE"] = "earthDefence"
        result["THUNDERDEFENSE"] = "thunderDefence"
        result["WATERDEFENSE"] = "waterDefence"
        result["FIREDEFENSE"] = "fireDefence"
        result["AIRDEFENSE"] = "airDefence"

        return result
    }

    fun getMajorIdentifications(): JSONOrderedObject {
        val result = JSONOrderedObject()

        for (id in MajorIdentification.values()) {
            val entry = result.getOrCreate<JSONOrderedObject>(id.name)
            entry["name"] = id.displayname
            entry["description"] = id.description
        }

        return result
    }

    private fun translateStatusName(raw: String): String? {
        return when (raw) {
            "spellCostPct1" -> "1stSpellCost"
            "spellCostPct2" -> "2ndSpellCost"
            "spellCostPct3" -> "3rdSpellCost"
            "spellCostPct4" -> "4thSpellCost"
            "spellCost1Pct" -> "1stSpellCost" // Only Demon Tide uses this format for spell cost
            "spellCost2Pct" -> "2ndSpellCost"
            "spellCost3Pct" -> "3rdSpellCost"
            "spellCost4Pct" -> "4thSpellCost"
            "spellCostRaw1" -> "raw1stSpellCost"
            "spellCostRaw2" -> "raw2ndSpellCost"
            "spellCostRaw3" -> "raw3rdSpellCost"
            "spellCostRaw4" -> "raw4thSpellCost"
            "spellDamageRaw" -> "rawNeutralSpellDamage"
            "damageBonusRaw" -> "rawMainAttackNeutralDamage"
            "damageBonus" -> "mainAttackDamage"
            "healthRegenRaw" -> "rawHealthRegen"
            "healthBonus" -> "rawHealth"
            "speed" -> "walkSpeed"
            "soulPoints" -> "soulPointRegen"
            "emeraldStealing" -> "stealing"
            "strengthPoints" -> "rawStrength"
            "dexterityPoints" -> "rawDexterity"
            "intelligencePoints" -> "rawIntelligence"
            "defensePoints" -> "rawDefence"
            "agilityPoints" -> "rawAgility"
            "bonusEarthDamage" -> "earthDamage"
            "bonusThunderDamage" -> "thunderDamage"
            "bonusWaterDamage" -> "waterDamage"
            "bonusFireDamage" -> "fireDamage"
            "bonusAirDamage" -> "airDamage"
            "bonusEarthDefense" -> "earthDefence"
            "bonusThunderDefense" -> "thunderDefence"
            "bonusWaterDefense" -> "waterDefence"
            "bonusFireDefense" -> "fireDefence"
            "bonusAirDefense" -> "airDefence"
            "jumpHeight" -> "rawJumpHeight"
            "rainbowSpellDamageRaw" -> "rawSpellDamage"
            "gatherXpBonus" -> "gatherXpBonus"
            "attackSpeedBonus" -> "attackSpeed"

            //same ones
            "spellDamage" -> "spellDamage"
            "healthRegen" -> "healthRegen"
            "poison" -> "poison"
            "lifeSteal" -> "lifeSteal"
            "manaRegen" -> "manaRegen"
            "exploding" -> "exploding"
            "sprint" -> "sprint"
            "sprintRegen" -> "sprintRegen"
            "lootBonus" -> "lootBonus"
            "lootQuality" -> "lootQuality"
            "gatherSpeed" -> "gatherSpeed"
            "xpBonus" -> "xpBonus"
            "manaSteal" -> "manaSteal"
            "thorns" -> "thorns"
            "reflection" -> "reflection"
            else -> null
        }
    }

}
