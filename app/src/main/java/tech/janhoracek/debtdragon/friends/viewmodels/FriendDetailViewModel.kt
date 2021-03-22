package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants


class FriendDetailViewModel : BaseViewModel() {

    private val _friendData = MutableLiveData<FriendDetailModel>()
    val friendData: LiveData<FriendDetailModel> get() = _friendData

    /*private val _userName = MutableLiveData<String>("Nazdar")
    val userName: LiveData<String> get() = _userId*/

    fun setData(friendshipID: String) {
        lateinit var friendID: String
        GlobalScope.launch(IO) {
            val friendshipDocument =
                db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).get().await()
            friendID = if (friendshipDocument.get("member1") == auth.currentUser.uid) {
                friendshipDocument.get("member2").toString()
            } else {
                friendshipDocument.get("member1").toString()
            }


            db.collection(Constants.DATABASE_USERS).document(friendID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _friendData.value = snapshot.toObject(FriendDetailModel::class.java)
                    } else {
                        Log.w("DATA", "Current data null")
                    }

                    /*db.collection(Constants.DATABASE_USERS).document(friendID).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _friendData.value = task.result!!.toObject(FriendDetailModel::class.java)
                    } else {
                        Log.d("KIWITKO", "Chyba nacitani useru")
                    }
                }*/
                }


        }


    }

}
