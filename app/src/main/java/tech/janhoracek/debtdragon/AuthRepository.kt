package tech.janhoracek.debtdragon

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun registerUser(email: String, password: String): Task<AuthResult> {
        Log.d("REBORN", "Trying to register with email: " + email)
        Log.d("REBORN", "Trying to register with password: " + password)
        return auth.createUserWithEmailAndPassword(email, password)
    }


}