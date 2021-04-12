package tech.janhoracek.debtdragon.profile.models

/**
 * User model
 *
 * @property email as user email
 * @property name as user name
 * @property uid as user ID
 * @property url as user img url
 * @property account as user IBAN account
 * @constructor Create empty User model
 */
data class UserModel (
        val email: String = "",
        val name: String = "",
        val uid: String = "",
        val url: String = "",
        val account: String = ""
)