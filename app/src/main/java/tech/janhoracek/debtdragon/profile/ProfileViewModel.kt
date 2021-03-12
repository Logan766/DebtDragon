package tech.janhoracek.debtdragon.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _userName = MutableLiveData<String>("Loading")
    val userName: LiveData<String> get() = _userName

    init {
        val docRef = db.collection("Users").document(auth.currentUser.uid).addSnapshotListener{snapshot, e ->
            if(e != null) {
                Log.w("OHEN", "Listening failed: " + e)
            }

            if(snapshot != null && snapshot.exists()) {
                Log.w("OHEN", "Current data: ${snapshot.data}")
            } else {
                Log.w("OHEN", "Current data null")
            }

        }
        docRef
    }


}