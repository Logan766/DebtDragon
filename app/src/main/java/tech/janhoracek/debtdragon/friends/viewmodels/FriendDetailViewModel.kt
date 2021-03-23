package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants


class FriendDetailViewModel : BaseViewModel() {

    private val _friendData = MutableLiveData<FriendDetailModel>()
    val friendData: LiveData<FriendDetailModel> get() = _friendData

    private val _friendshipData = MutableLiveData<FriendshipModel>()
    val friendshipData: LiveData<FriendshipModel> get() = _friendshipData


    sealed class Event {
        object NavigateBack : Event()
        object GenerateQR: Event()
        data class CreateEditDebt(val debtID: String?): Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun setData(friendshipID: String) {
        //this.friendshipID = friendshipID
        GlobalScope.launch(IO) {
            val friendshipDocument = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).get().await()
            val friendID = if (friendshipDocument.get("member1") == auth.currentUser.uid) {
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
                }
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).addSnapshotListener{snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    _friendshipData.value = snapshot.toObject(FriendshipModel::class.java)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }

        }
    }

    fun onBackPressed() {
        Log.d("RANO", "Posilam signal zpet")
        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack) }

    }

    fun onAddDebtPressed() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.CreateEditDebt(null))}
    }

}
