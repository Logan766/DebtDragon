package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.utility.BaseViewModel

class FriendslistViewModel : BaseViewModel() {
    private var friendsList: List<FriendModel> = ArrayList()


    fun loadFriends() {
        db.collection("Users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                friendsList = task.result!!.toObjects(FriendModel::class.java)
            } else {
                Log.d("KIWITKO", "Chyba nacitani useru")
            }
        }
    }
}