package com.wynntils.athena.core.enums

enum class AccountType {

    NORMAL,
    BANNED,
    DONATOR,
    CREATIVE_TEAM,
    HELPER,
    MODERATOR,
    DEVELOPER,
    WEB_ADMINISTRATOR,
    ADMINISTRATOR;

    companion object {

        fun valueOr(input: String, or: AccountType = NORMAL): AccountType {
            return try {
                valueOf(input)
            } catch (ex: Exception) {
                or
            }
        }

    }

}
