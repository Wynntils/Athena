package com.wynntils.athena.routes.managers

import com.wynntils.athena.core.utils.JSONOrderedObject
import org.json.simple.JSONArray
import org.json.simple.JSONObject

object ItemManager {

    fun convertItem(input: JSONObject): JSONOrderedObject {
        val result = JSONOrderedObject()

        fun isFixed(raw: String): Boolean {
            if (input["identified"] == true) return true

            return when(raw) {
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

        fun ignoreZero(input: Int?): Int? {
            return if (input == null || input == 0) null else input
        }

        // outside tags
        result["displayName"] = input.getOrDefault("displayName", input["name"]).toString().replace("֎", "") // cancer has ֎ in it name for a random reason
        result["tier"] = (input["tier"] as String).toUpperCase()
        result["identified"] = input.getOrDefault("identified", false)
        result["powderAmount"] =  input["sockets"]
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
        damageTypes["neutral"] = ignoreZero(input.getOrDefault("damage", null) as Int)
        damageTypes["earth"] = ignoreZero(input.getOrDefault("earthDamage", null) as Int)
        damageTypes["thunder"] = ignoreZero(input.getOrDefault("thunderDamage", null) as Int)
        damageTypes["water"] = ignoreZero(input.getOrDefault("waterDamage", null) as Int)
        damageTypes["fire"] = ignoreZero(input.getOrDefault("fireDamage", null) as Int)
        damageTypes["air"] = ignoreZero(input.getOrDefault("airDamage", null) as Int)

        // defenseTypes
        val defenseTypes = result.getOrCreate<JSONOrderedObject>("defenseTypes")
        defenseTypes["health"] = ignoreZero(input.getOrDefault("health", null) as Int)
        defenseTypes["earth"] = ignoreZero(input.getOrDefault("earthDefense", null) as Int)
        defenseTypes["thunder"] = ignoreZero(input.getOrDefault("thunderDefense", null) as Int)
        defenseTypes["water"] = ignoreZero(input.getOrDefault("waterDefense", null) as Int)
        defenseTypes["fire"] = ignoreZero(input.getOrDefault("fireDefense", null) as Int)
        defenseTypes["air"] = ignoreZero(input.getOrDefault("airDefense", null) as Int)

        // Pure Copy
        val statuses = result.getOrCreate<JSONOrderedObject>("statuses")

        result["majorIds"] = input.getOrDefault("majorIds", null)
        result["restriction"] = input["restrictions"]
        result["lore"] = input["addedLore"]

        for (key in input.keys) {
            val value = input[key]

            if (key == "armorType") {
                itemInfo["material"] = "minecraft:${value}_${itemInfo["type"]}".toLowerCase().replace("chain", "chainmail")
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

    private fun translateStatusName(raw: String): String? {
        return when(raw) {
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
            "gatherXpBonus" -> "gatherXPBonus"
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