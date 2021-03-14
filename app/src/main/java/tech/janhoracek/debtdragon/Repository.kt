package tech.janhoracek.debtdragon

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class Repository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    companion object {
        private val _userID = MutableLiveData<String>()
        val userID: LiveData<String> get() = _userID

        private val _userImage = MutableLiveData<String>()
        val userImage: LiveData<String> get() = _userImage

        private val _userName = MutableLiveData<String>("Loading")
        val userName: LiveData<String> get() = _userName
    }


    init {
        db.collection("Users").document(auth.currentUser.uid)
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
            }

        GlobalScope.launch(Dispatchers.IO) {
            async {
                var url: Uri? = null
                try {
                    url =
                        storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                    _userImage.postValue(url.toString())
                } catch (e: StorageException) {
                    Log.d("OHEN", "Nemame obrazek")
                }
                Log.d("OHEN", "Posilam do userImage: " + url.toString().isBlank())
                //_userImage.postValue(url.toString()) }

            }
        }
    }
}