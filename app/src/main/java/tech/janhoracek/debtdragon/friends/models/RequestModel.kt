package tech.janhoracek.debtdragon.friends.models

/**
 * Request model
 *
 * @property type as type of request (send/recieved)
 * @property authorId as ID of request owner
 * @constructor Create empty Request model
 */
data class RequestModel (
    //val author: String = "",
    val type: String = "",
    val authorId: String = ""
)