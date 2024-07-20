package il.co.erg.mykumve.util

import il.co.erg.mykumve.R

enum class DifficultyLevel(val id: Int, val stringResId: Int) {
    UNSET(0, R.string.difficulty_unset),
    EASY(1, R.string.difficulty_easy),
    MEDIUM(2, R.string.difficulty_medium),
    HARD(3, R.string.difficulty_hard);

    fun prettyString() = stringResId

    companion object {
        fun fromId(id: Int): DifficultyLevel {
            return values().find { it.id == id } ?: UNSET
        }
    }
}

object PATTERNS {
    const val PHONE = "^\\+(\\d{1,4})\\s?(0\\d{9}|\\d{9})$"
}


enum class TripInvitationStatus(val value: Int) {
    UNSENT(-1),
    PENDING(0),
    APPROVED(1),
    REJECTED(2);

    companion object {
        fun fromInt(value: Int) = TripInvitationStatus.values().first { it.value == value }
    }

    override fun toString(): String {
        return when (this) {
            UNSENT -> "Unsent"
            PENDING -> "Pending"
            APPROVED -> "Approved"
            REJECTED -> "Rejected"
        }
    }
}

enum class ShareLevel(val value: Int) {
    PUBLIC(1),
    PRIVATE(0);

    companion object {
        fun fromInt(value: Int) = ShareLevel.values().first { it.value == value }
    }
}

data class Result(
    val success: Boolean,
    val reason: String,
    val data: Map<String, Any?>? = emptyMap()
)

enum class NavigationArgs(val key: String) {
    IS_CREATING_NEW_TRIP("isCreatingNewTrip")
}