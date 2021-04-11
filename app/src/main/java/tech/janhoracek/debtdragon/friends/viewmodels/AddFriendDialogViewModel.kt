package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.RequestModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import java.lang.Exception

/**
 * Add friend dialog view model
 *
 * @constructor Create empty Add friend dialog view model
 */
class AddFriendDialogViewModel : BaseViewModel() {
    val friendError = MutableLiveData<String>("")
    val friendNameContent = MutableLiveData<String>("")

    private var friendId: String? = ""
    private var currentFriennId: String? = ""
    private lateinit var currentFriendRef: DocumentReference
    private var alreadyFriends = false
    private var alreadyRequested = false

    // Events
    sealed class Event {
        object NavigateBack : Event()
        data class ShowToast(val text: String) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    /**
     * Add friend click resolve
     *
     */
    fun onAddFriendClick() {
        if (validateFriendName()) {
            // Try to get friend ID
            val friendIdGeting = GlobalScope.launch(IO) {
                try {
                    getFriendId()
                } catch (e: Exception) {
                    Log.d("ERROR", "Error: " + e.message.toString())
                    friendId = null
                }
            }

            GlobalScope.launch(Main) {
                friendIdGeting.join()

                // Create requests
                if (validateRequest()) {
                    db.collection("Users").document(auth.currentUser.uid).collection("Requests")
                        .document(friendId!!).set(createRequest("sent", auth.currentUser.uid))
                    db.collection("Users").document(friendId!!).collection("Requests")
                        .document(auth.currentUser.uid)
                        .set(createRequest("request", auth.currentUser.uid))
                    friendNameContent.value = ""
                    eventChannel.send(Event.NavigateBack)
                }
            }

        } else {
            //
        }

    }

    /**
     * Validate friend name
     *
     * @return true if valid
     */
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

    /**
     * Create request
     *
     * @param requestType as type of request (sent/recieved)
     * @param authorId as ID of sender
     * @return
     */
    private suspend fun createRequest(requestType: String, authorId: String): RequestModel {
        return RequestModel(requestType, authorId)
    }

    /**
     * Validate request
     *
     * @return true if valid
     */
    private suspend fun validateRequest() : Boolean {
        return when {
            friendId == null -> {
                friendError.value = localized(R.string.add_friend_user_not_found)
                false
            }
            friendId == auth.currentUser.uid -> {
                friendError.value = localized(R.string.add_friend_cannot_add_yourself)
                false
            }
            alreadyFriends -> {
                friendError.value = localized(R.string.add_friend_user_already_in_friends)
                false
            }
            alreadyRequested -> {
                friendError.value = localized(R.string.add_friend_request_already_sent)
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * Get friend id
     *
     */
    private suspend fun getFriendId() {
        val friendIdQuery = db.collection("Users").whereEqualTo("email", friendNameContent.value).get().await()
        friendId = friendIdQuery.documents[0].data!!["uid"].toString()
        val friendCheck = db.collection("Users").document(auth.currentUser.uid).collection("Friendships").document(friendId!!).get().await()
        alreadyFriends = friendCheck.exists()
        val requestCheck = db.collection("Users").document(auth.currentUser.uid).collection("Requests").document(friendId!!).get().await()
        alreadyRequested = requestCheck.exists()
    }


}