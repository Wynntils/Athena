package com.wynntils.athena.core.enums

enum class AccountType {

    NORMAL,
    BANNED,
    DONATOR,
    CONTENT_TEAM,
    HELPER,
    MODERATOR;

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
