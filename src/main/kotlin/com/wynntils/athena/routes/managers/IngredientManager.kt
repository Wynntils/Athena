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

        // material
        val sprite = input["sprite"] as JSONObject
        result["material"] = "${sprite["id"]}:${sprite["damage"]}"

        // professions
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
        return when (raw) {
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
            "GATHER_XP_BONUS" -> "gatherXPBonus"
            "GATHER_SPEED" -> "gatherSpeed"
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

    fun getHeadTextures(): JSONOrderedObject {
        val result = JSONOrderedObject()

        result["Royal Cake Slice"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjkxMzY1MTRmMzQyZTdjNTIwOGExNDIyNTA2YTg2NjE1OGVmODRkMmIyNDkyMjAxMzllOGJmNjAzMmUxOTMifX19"
        result["Dead Bee"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTQ3MzIyZjgzMWUzYzE2OGNmYmQzZTI4ZmU5MjUxNDRiMjYxZTc5ZWIzOWM3NzEzNDlmYWM1NWE4MTI2NDczIn19fQ=="
        result["Nivlan Honeycomb"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjkzYzM1MTJmYzU4ODVmY2NiYjI1ZDJkYWY3ZmRjZmFlODI2NDFlZDdlNWUzNTk3Y2RkZjczZTQxMTU5ZjI0In19fQ=="
        result["Vibrant Augment"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTM5OWU0N2U1NWMxNWQzZTI2NTI1YzVhNDcwZjI0OGUzMzVkMmZhYTNhNjM0MTBjNGRkZjQyZGEzNmFkMmYifX19"
        result["Nivlan Honey"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTZkY2M4ZjM3YWM5OWQ5NTFlY2JjNWRmNWU4NTgyMTMzZjVmMjMwN2U3NjlhZjZiNmNmZmY0MjgyMTgwNjcifX19"
        result["Burnt Skull"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk1M2I2YzY4NDQ4ZTdlNmI2YmY4ZmIyNzNkNzIwM2FjZDhlMWJlMTllODE0ODFlYWQ1MWY0NWRlNTlhOCJ9fX0="
        result["Crumbling Skull"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk1M2I2YzY4NDQ4ZTdlNmI2YmY4ZmIyNzNkNzIwM2FjZDhlMWJlMTllODE0ODFlYWQ1MWY0NWRlNTlhOCJ9fX0="
        result["Coagulated Soulmass"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWVhNmY5MzJiNDVmZGYzYjY5M2Q5ZTQ0YmQwNWJjYTM2NGViNWI5YWZmNDk3MjI2ZmRiNTJhYmIyNDM2NDIyIn19fQ=="
        result["Rotten Log"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmE5NWRkMmVkYTAzY2E0OTE3OTZmNTM4ZDYyNTU3OWE4MjhiNzE4NDNmYjYwODY5YjEyYzkxZWNlMTUxNiJ9fX0="
        result["Victim's Skull"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWFlMzg1NWY5NTJjZDRhMDNjMTQ4YTk0NmUzZjgxMmE1OTU1YWQzNWNiY2I1MjYyN2VhNGFjZDQ3ZDMwODEifX19"
        result["Native Jadeite"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2U2NThjZTdjNDI5MWVmZTUwYjIxZDEzZjk3MmU5M2MzNzJkMjcyMWVkN2Q2NTI2NjA2MGE0OTE2YTM1YiJ9fX0="
        result["Decaying Heart"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjODQ0N2E4YjZiMGUwYzdlNzYyOWM2ODk4ZWM5Yzc0OWE3YTBhMmI0NTJiOWMzODUyYzc4NDdiYjRkYzUifX19"
        result["Squid"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGYzM2UzODlkZGJlM2M4ZGQzMzJmMWI2ZmQ2MGM0MmIyMjRlOGQyOGI1YzZkZDllNTBmYWMyNGM1OTE2Zjk1MSJ9fX0="
        result["More-Pearlescent Jewel"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmE0NGUyYjgzODhkZjU5M2E4NWY3ZDU0NDQ1Y2U3ODc5NmNlZTZiMGJjYjdhNTVhY2JkZDFjYmQ4ZjM1YWVmMiJ9fX0="
        result["Elemental Crystal"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTY3OTliZmFhM2EyYzYzYWQ4NWRkMzc4ZTY2ZDU3ZDlhOTdhM2Y4NmQwZDlmNjgzYzQ5ODYzMmY0ZjVjIn19fQ=="
        result["Obelisk Core"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWYwZDczMTUwZGY0ZjVkMDc3NWI1NTQ4OWEyOGE5Nzc2NGFmNWE3NzUxYTAwZTQwNDA0OTVlOTdmZmU5NzcyZiJ9fX0="
        result["Pig's Skull"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTUyN2M0ZGE4OTIyZDM5NDVjNzFkMzFkMGFhYTY3NjE0ODlkMmU3YzVlZTJhNzFjYmJjYmMwODA2ODczZjAifX19"
        result["Cyclops Eye"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdkYjE5MjNkMDNjNGVmNGU5ZjZlODcyYzVhNmFkMjU3OGIxYWZmMmIyODFmYmMzZmZhNzQ2NmM4MjVmYjkifX19"
        result["Tempered Core"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM2ODdlMjVjNjMyYmNlOGFhNjFlMGQ2NGMyNGU2OTRjM2VlYTYyOWVhOTQ0ZjRjZjMwZGNmYjRmYmNlMDcxIn19fQ=="
        result["Pride of the Heights"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWMyZTlkODM5NWNhY2Q5OTIyODY5YzE1MzczY2Y3Y2IxNmRhMGE1Y2U1ZjNjNjMyYjE5Y2ViMzkyOWM5YTExIn19fQ=="
        result["Blighted Skull"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk1M2I2YzY4NDQ4ZTdlNmI2YmY4ZmIyNzNkNzIwM2FjZDhlMWJlMTllODE0ODFlYWQ1MWY0NWRlNTlhOCJ9fX0="
        result["Contorted Stone"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzg0MGI4N2Q1MjI3MWQyYTc1NWRlZGM4Mjg3N2UwZWQzZGY2N2RjYzQyZWE0NzllYzE0NjE3NmIwMjc3OWE1In19fQ=="
        result["Urdar's Stone"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTZjMzNiMmIzYmY1ZDNmMDU4MmMyYmUzYmNkYmYzMjQ1YmRjYzQyNjNiNjY1MjM4ZmJkMWJmNDg2YmNjIn19fQ=="
        result["Engored Oculus"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzZWNkOGUxN2EzZGFhNjY1ZGIzZGIwODIyNzJjNjI5Nzc5OGViYjUwOTY0MDI2Y2U3Zjg1NDU4YmY1NTY2YyJ9fX0="
        result["Borange Fluff"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTBmYjRlNmE2MTMyMmE0MTg1ZTdiNDFhNjQzZjZhNGNiNjlhMzE3ZWJlMzkzOTZmYjI3NjI3ZTM2MTRjNjkifX19"
        result["Vortexian Event Horizon"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWRkZWJiYjA2MmY2YTM4NWE5MWNhMDVmMThmNWMwYWNiZTMzZTJkMDZlZTllNzQxNmNlZjZlZTQzZGZlMmZiIn19fQ=="
        result["Shrieker's Head"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWJiN2M2OTYxZDdiOTRkODRkMDRmNjQxYzRjMDgzMDIwOTZiYzFmMmQwY2RiODdiMTA3MDBhOTEwYzA0N2IyOSJ9fX0="
        // 1.20
        result["Ice Cream Sandwich"] =
            "ewogICJ0aW1lc3RhbXAiIDogMTYwMjk5NTU5ODI2OCwKICAicHJvZmlsZUlkIiA6ICJhYzE2NGNkYzNkYjc0ZWQxYThiYzU1MWIxZTVlMzgwYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJuaWNrdHJlZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYWM2ZmQyNWE0N2MwOGMxMWVkNDg5YjFjYzMyZmZkM2ZiM2UxNTAzZDNkNDc2ZmMyMTMxZjdlMGE2ZmI0ODAxIgogICAgfQogIH0KfQ=="
        result["Letvus Delight"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJmNDgxYmZlMDQ1NWM3NjdkZmQ3MGYxYzlmNjQ3ZGE0ZjhhZTQ1OWU0ZmU2NGVmOTQ3OTBiZDM3NGQ0NjE0In19fQ=="
        result["Mellow Mango"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWZkMGUzMzBhNjg4ZDhiYjk1MTliZWZlMWJmYzM0MzM3YjM3MWFjNzUxNTAyMTZmZGQwMzk1OWViN2I0NCJ9fX0="
        result["Festering Face"] =
            "eyJ0aW1lc3RhbXAiOjE1NzIwMDcwNzUwMTUsInByb2ZpbGVJZCI6IjE5MmE5MzE3MDNiODQzYzZiN2ZjNDI1NjIxNzE3MDViIiwicHJvZmlsZU5hbWUiOiI4NHYiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jZTFlYTExYjczOGNmNDQ0Zjc2OTQ1ZjU4NmEwODQ1MTY0MTIxNjZiYTllMDI1MGRmMjhjZTNmNjE0MzU5ZTI0In19fQ=="
        result["Autonomous Core"] =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRhMzMyYWJkZTMzM2ExNWE2YzZmY2ZlY2E4M2YwMTU5ZWE5NGI2OGU4ZjI3NGJhZmMwNDg5MmI2ZGJmYyJ9fX0="
        result["Coalescence"] =
            "eyJ0aW1lc3RhbXAiOjE1NDQzOTU4MzA5NTUsInByb2ZpbGVJZCI6IjQ5ODU2NTcwYWUwODRlYWY5OTg4MDQwY2VhZGI5ZGQxIiwicHJvZmlsZU5hbWUiOiJTbmVycCIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWM3N2EyOTZmNjc4NzMzMjE4YzFiOWVkOWJjNmFhY2Y4NDE1MGI4MTM2OWQ5ZmUxNWUzN2IzMjk0NmY4NTEyYSJ9fX0="
        result["Gaze of Darkness"] =
            "eyJ0aW1lc3RhbXAiOjE0NDk1MDYwNTE1NTksInByb2ZpbGVJZCI6IjY2ODYxMDY1YzMzYjQ4MGNhOWQ0MWJiODlkYjcxMDhjIiwicHJvZmlsZU5hbWUiOiJEYXJrbmVzc2ZhbGwiLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzg3ZTQ3OTJmZjY1NDJkY2FiMThmOWY0OWY0OTMzM2YxM2ViMTAzOTRjNDQ4YWMxNDUzOTY3ODYyYzhmMzgifX19"

        return result
    }

}
