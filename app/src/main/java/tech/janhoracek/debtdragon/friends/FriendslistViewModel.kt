package tech.janhoracek.debtdragon.friends

import android.util.Log
import androidx.lifecycle.ViewModel
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