package tech.janhoracek.debtdragon.profile.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Profile view model
 *
 * @constructor Create empty Profile view model
 */
class ProfileViewModel : BaseViewModel() {

    private val _userName = MutableLiveData<String>("Loading")
    val userName: LiveData<String> get() = _userName

    private val _userImage = MutableLiveData<String>()
    val userImage: LiveData<String> get() = _userImage

    private val _logOutStatus = MutableLiveData<Boolean>(false)
    val logOutStatus: LiveData<Boolean> get() = _logOutStatus

    private val _authProviderIsPassword = MutableLiveData<Boolean>(false)
    val authProviderIsPassword: LiveData<Boolean> get() = _authProviderIsPassword

    private var docRef: ListenerRegistration

    init {
        _authProviderIsPassword.value = auth.currentUser.providerData[auth.currentUser.providerData.size - 1].providerId == "password"

        docRef = db.collection("Users").document(auth.currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("LSTNR", "Listening failed: " + e)
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.w("DATA", "Current data: ${snapshot.data}")
                    _userName.value = snapshot.data?.get("name").toString()
                } else {
                    Log.w("DATA", "Current data null")
                }


                viewModelScope.launch(IO) {
                    var url: Uri? = null
                    try {
                        url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                        _userImage.postValue(url.toString())
                    } catch (e: StorageException) {
                        Log.d("ERROR", "No image")
                    }
                }

            }


    }

    /**
     * Click logout
     *
     */
    fun clickLogout() {
        auth.signOut()
        _logOutStatus.value = true
    }

    public override fun onCleared() {
        super.onCleared()
    }

    /**
     * Saves profile image
     *
     * @param image as byte arry of profile image
     */
    fun saveProfileImage(image: ByteArray) {
        Log.d("DOMBY", "Zkusim nahrat obrzek")
        viewModelScope.launch(IO) {
            try {
                storage.reference.child("/images/${auth.currentUser.uid}/profile.jpg").putBytes(image).await()
                val url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).update("url", url.toString())
            } catch (e: Exception) {
                Log.d("STRG", "Picture upload error: " + e.message.toString())
            }
        }
    }


}

