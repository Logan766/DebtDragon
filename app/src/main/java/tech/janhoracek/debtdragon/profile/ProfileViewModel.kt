package tech.janhoracek.debtdragon.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel

class ProfileViewModel : BaseViewModel() {

    private val _userName = MutableLiveData<String>("Loading")
    val userName: LiveData<String> get() = _userName

    private val _userImage = MutableLiveData<String>()
    val userImage: LiveData<String> get() = _userImage

    private val _logOutStatus = MutableLiveData<Boolean>(false)
    val logOutStatus: LiveData<Boolean> get() = _logOutStatus

    private var docRef: ListenerRegistration

    init {
        docRef = db.collection("Users").document(auth.currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("OHEN", "Listening failed: " + e)
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.w("OHEN", "Current data: ${snapshot.data}")
                    _userName.value = snapshot.data?.get("name").toString()
                } else {
                    Log.w("OHEN", "Current data null")
                }


                viewModelScope.launch(IO) {
                    var url: Uri? = null
                    try {
                        url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                        Log.d("OHEN", "URL obrazku je: " + url.toString())
                        _userImage.postValue(url.toString())
                    } catch (e: StorageException) {
                        Log.d("OHEN", "Nemame obrazek")
                    }
                    Log.d("OHEN", "Posilam do userImage: " + url.toString().isBlank())
                    //_userImage.postValue(url.toString())
                }


            }
    }

    fun clickLogout() {
        //onCleared()
        auth.signOut()
        _logOutStatus.value = true
    }

    public override fun onCleared() {
        Log.d("PIRAT", "JSEM ZNICENEJ!")
        //docRef.remove()
        super.onCleared()
    }

}

