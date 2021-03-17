package tech.janhoracek.debtdragon.friends

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import java.lang.Exception

class AddFriendDialogViewModel: BaseViewModel() {
    val friendError = MutableLiveData<String>("")
    //val frienderror: LiveData<String> get() = _friendError

    val friendNameContent = MutableLiveData<String>("")

    private var friendId: String? = ""

    fun onAddFriendClick() {
        if(validateFriendName()) {
            Log.d("PUVA", friendNameContent.value.toString())
            val friendIdGeting = GlobalScope.launch(IO) {
                try {
                    val friendIdQuery = db.collection("Users").whereEqualTo("email", friendNameContent.value).get().await()
                    friendId = friendIdQuery.documents[0].data!!["uid"].toString()
                    Log.d("PUVA", "User id jest: " + friendId)
                } catch (e: Exception) {
                    Log.d("PUVA", "Error: " + e.message.toString())
                    friendId = null
                }
            }

            GlobalScope.launch(Main) {
                friendIdGeting.join()
                Log.d("PUVA", "current user id jest: " + auth.currentUser.uid)

                if(friendId == null) {
                    friendError.value = "Uživatel nenalezen"
                } else if(friendId == auth.currentUser.uid) {
                    friendError.value = "Nelze přidat sám sebe"
                } else {
                    Log.d("PUVA", "Uzivatel je ready na pridani")
                }
            }


        } else {
            Log.d("PUVA", "ses laska")
        }

    }

    private fun validateFriendName(): Boolean {
        return if(friendNameContent.value?.isEmpty()!!) {
            friendError.value = localized(R.string.addFriend_name_cant_be_empty)
            false
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(friendNameContent.value).matches()) {
            friendError.value = localized(R.string.mail_is_not_in_form)
            false
        } else {
            friendError.value = ""
            true
        }
    }

    private suspend fun loadFriendId(): Task<QuerySnapshot> {
        return db.collection("Users").whereEqualTo("email", friendNameContent.value.toString()).get()
    }





}