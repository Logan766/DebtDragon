package tech.janhoracek.debtdragon.friends.models

/**
 * Friend detail model
 *
 * @property email as friend email
 * @property name as friend full name
 * @property uid as friend ID
 * @property url as friend image URL
 * @property account as friend IBAN account
 * @constructor Create empty Friend detail model
 */
data class FriendDetailModel (
    val email: String = "",
    val name: String = "",
    val uid: String = "",
    val url: String = "",
    val account: String = ""
        )