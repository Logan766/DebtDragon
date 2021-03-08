package tech.janhoracek.debtdragon

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

object ApplicationRepository {
    private val _userMutableLiveData = MutableLiveData<FirebaseUser>()
    private val mAuth: FirebaseAuth

    init {
        Log.d("FRANTA", "Ready na zapis")
        mAuth = FirebaseAuth.getInstance()
        _userMutableLiveData.value = mAuth.currentUser
    }

    val userLiveData: LiveData<FirebaseUser> get() = _userMutableLiveData


    fun registerUser(name: String, email: String, password: String) {
        Log.d("JIRKA", "Zkusime to")
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task->
            if(task.isSuccessful) {
                //addUserDataAfterRegistration(name)
                Log.d("JIRKA", "Dobry!")
            } else {

            }
        }
    }

    fun getUserMutableLiveData(): MutableLiveData<FirebaseUser> {
        return _userMutableLiveData
    }

    fun addUserDataAfterRegistration(name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name).build()

        mAuth.currentUser!!.updateProfile(profileUpdates)
        val user = HashMap<String, String>()
        user["name"] = mAuth.currentUser!!.displayName!!
        user["email"] = mAuth.currentUser!!.email!!
        FirebaseFirestore.getInstance().collection("Users").document(mAuth.currentUser!!.uid).set(user)
    }




}