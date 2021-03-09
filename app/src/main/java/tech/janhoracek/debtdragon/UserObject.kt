package tech.janhoracek.debtdragon

import com.google.firebase.firestore.Exclude
import java.io.Serializable

class UserObject : Serializable {
    var userID: String? = null
    var userName: String? = null
    var userEmail: String? = null

    @Exclude
    var isAuthenticated = false
    @Exclude
    var isNew = false
    @Exclude
    var isCreated = false
}