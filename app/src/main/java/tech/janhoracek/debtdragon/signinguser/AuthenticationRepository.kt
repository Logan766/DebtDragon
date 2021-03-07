package tech.janhoracek.debtdragon.signinguser

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import tech.janhoracek.debtdragon.MainActivity

class AuthenticationRepository {
    private val _userMutableLiveData = MutableLiveData<FirebaseUser>()
    private val mAuth: FirebaseAuth

    init {
        Log.d("MVVM", "Inicializuji repository")
        mAuth = FirebaseAuth.getInstance()
        _userMutableLiveData.value = mAuth.currentUser
    }

    val userLiveData: LiveData<FirebaseUser> get() = _userMutableLiveData


    fun registerUser(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _userMutableLiveData.value = mAuth.currentUser
            } else {
                Log.d("MVVM", "Failed creating user in Repository")
            }
        }
    }

    fun getUserMutableLiveData(): MutableLiveData<FirebaseUser> {
        return _userMutableLiveData
    }


}