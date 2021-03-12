package tech.janhoracek.debtdragon.profile

import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _userName = MutableLiveData<String>("Loading")
    val userName: LiveData<String> get() = _userName

    private val _userImage = MutableLiveData<String>("")
    val userImage: LiveData<String> get() = _userImage

    init {
        val docRef = db.collection("Users").document(auth.currentUser.uid).addSnapshotListener{snapshot, e ->
            if(e != null) {
                Log.w("OHEN", "Listening failed: " + e)
            }

            if(snapshot != null && snapshot.exists()) {
                Log.w("OHEN", "Current data: ${snapshot.data}")
                _userName.value = snapshot.data?.get("name").toString()
            } else {
                Log.w("OHEN", "Current data null")
            }

            viewModelScope.launch(IO) {
                val url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                _userImage.postValue(url.toString())
            }

        }



    }


}