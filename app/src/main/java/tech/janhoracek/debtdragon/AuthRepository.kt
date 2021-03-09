package tech.janhoracek.debtdragon

import android.R.attr
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext


object AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollectionRef = db.collection("Users")
    private val user: UserObject = UserObject()

    private val _registerResult = MutableLiveData<String>()
    val registerResult: LiveData<String> get() = _registerResult

    suspend fun ahoj(email: String, password: String) = withContext(Dispatchers.IO) {
        Log.d("AHOJ", "NECO")
        async { auth.createUserWithEmailAndPassword(email, password) }.await()
    }

    suspend fun registerUser2(name: String, email: String, password: String) =
        withContext(Dispatchers.IO) {
            val result = async { auth.createUserWithEmailAndPassword(email, password) }.await()
            val result1 = async { createUser(auth.currentUser.uid, name, email) }.await()

        }

    fun registerUser(name: String, email: String, password: String) {
        Log.d("REBORN", "Trying to register with email: " + email)
        Log.d("REBORN", "Trying to register with password: " + password)
        //val isUserAuthenticatedInFirebaseMutableLiveData = MutableLiveData<UserObject>()
        //auth.createUserWithEmailAndPassword(email, password)


        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                createUser(auth.currentUser.uid, name, email)
                Log.d("TIGER", "Success creating user in Auth")
            } else {
                //_registerResult.value = task.exception!!.message!!.toString()
                Log.d("TIGER", task.exception!!.message.toString())
            }
        }
    }

    fun createUser(uid: String, name: String, email: String) {
        val user = HashMap<String, String>()
        user["name"] = name
        user["email"] = email
        db.collection("Users").document(uid).set(user)
            .addOnSuccessListener {
                _registerResult.value = "Registrace úspěšná"
                Log.d("TIGER", "Success creating user in database")
            }
            .addOnFailureListener {
                _registerResult.value = "Registrace neúspěšná"
                auth.currentUser.delete()
                Log.d("TIGER", "Failure creating user in database")
            }
    }


}