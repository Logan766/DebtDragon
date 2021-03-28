package tech.janhoracek.debtdragon

import com.google.firebase.firestore.Exclude
import java.io.Serializable

object UserObject {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var url: String? = null
    var account: String? = null
}