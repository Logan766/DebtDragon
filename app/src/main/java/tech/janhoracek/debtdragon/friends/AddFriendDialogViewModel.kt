package tech.janhoracek.debtdragon.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.UserObject
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import java.lang.Exception

class AddFriendDialogViewModel : BaseViewModel() {
    val friendError = MutableLiveData<String>("")
    //val frienderror: LiveData<String> get() = _friendError

    val friendNameContent = MutableLiveData<String>("")

    private var friendId: String? = ""
    private var currentFriennId: String? = ""
    private lateinit var currentFriendRef: DocumentReference

    sealed class Event {
        object NavigateBack : Event()
        data class ShowToast(val text: String) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun onAddFriendClick() {
        if (validateFriendName()) {
            Log.d("PUVA", friendNameContent.value.toString())
            val friendIdGeting = GlobalScope.launch(IO) {
                try {
                    val friendIdQuery = db.collection("Users").whereEqualTo("email", friendNameContent.value).get().await()
                    friendId = friendIdQuery.documents[0].data!!["uid"].toString()
                    db.collection("Users").document(auth.currentUser.uid).collection("Friendships").document(friendId!!).get().addOnCompleteListener {
                        //if(it.result.exists())
                    }
                    Log.d("PUVA", "User id jest: " + friendId)
                } catch (e: Exception) {
                    Log.d("PUVA", "Error: " + e.message.toString())
                    friendId = null
                }
            }

            GlobalScope.launch(Main) {
                friendIdGeting.join()
                Log.d("PUVA", "current user id jest: " + auth.currentUser.uid)

                if (friendId == null) {
                    friendError.value = "Uživatel nenalezen"
                } else if (friendId == auth.currentUser.uid) {
                    friendError.value = "Nelze přidat sám sebe"
                } else if (false) {
                    friendError.value = "Tento uživatel již v přátelích je"
                } else {
                    Log.d("PUVA", "Uzivatel je ready na pridani")
                    //val requestCurrentUser = RequestModel(auth.currentUser.uid, "sent")
                    db.collection("Users").document(auth.currentUser.uid).collection("Requests")
                        .document(friendId!!).set(createRequest("sent", auth.currentUser.uid))
                    db.collection("Users").document(friendId!!).collection("Requests")
                        .document(auth.currentUser.uid)
                        .set(createRequest("request", auth.currentUser.uid))
                    Log.d("PUVA", "Vse proslo, odesilam te zpet")
                    eventChannel.send(Event.NavigateBack)
                }

            }

        } else {
            Log.d("PUVA", "ses laska")
        }

    }

    private fun validateFriendName(): Boolean {
        return if (friendNameContent.value?.isEmpty()!!) {
            friendError.value = localized(R.string.addFriend_name_cant_be_empty)
            false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(friendNameContent.value)
                .matches()
        ) {
            friendError.value = localized(R.string.mail_is_not_in_form)
            false
        } else {
            friendError.value = ""
            true
        }
    }

    private suspend fun createRequest(requestType: String, authorId: String): RequestModel {
        return RequestModel(requestType, authorId)
    }


}