package tech.janhoracek.debtdragon.friends.models

/**
 * Friend model
 *
 * @property name as friend full
 * @property email as friend email
 * @constructor Create empty Friend model
 */
data class FriendModel (
    val name: String = "",
    val email: String = ""
)