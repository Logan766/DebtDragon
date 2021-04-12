package tech.janhoracek.debtdragon.utility

import com.google.firebase.firestore.Exclude
import java.io.Serializable

/**
 * User object of current user
 *
 * @constructor Create empty User object
 */
object UserObject {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var url: String? = null
    var account: String? = null
}