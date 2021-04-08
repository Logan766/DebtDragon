package tech.janhoracek.debtdragon.signinguser

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import java.io.ByteArrayOutputStream

class   LoginViewModel : BaseViewModel() {

    val emailContent = MutableLiveData<String>("")
    val passwordContent = MutableLiveData<String>("")

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordError = MutableLiveData<String>()
    val passwordError: LiveData<String> get() = _passwordError

    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> get() = _loginResult

    private val _status = MutableLiveData<String>()
    val status: LiveData<String> get() = _status

    fun onLoginClick() {
        if(validToLogin()) {
            auth.signInWithEmailAndPassword(emailContent.value, passwordContent.value).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _loginResult.value = localized(R.string.log_in_successful)
                } else {
                    _loginResult.value = task.exception!!.message!!.toString()
                }
            }
        }
    }


    private fun validToLogin() : Boolean {
        val emailValidation = validateEmail()
        val passwordValidation = validatePassword()
        return emailValidation && passwordValidation
    }

    private fun validateEmail() : Boolean {
        return if (emailContent.value == "") {
            _emailError.value = localized(R.string.mail_cannot_be_empty)
            false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value).matches()) {
            _emailError.value = localized(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    private fun validatePassword() : Boolean {
        return if (passwordContent.value == "") {
            _passwordError.value = localized(R.string.password_cannot_be_empty)
            false
        } else {
            _passwordError.value = ""
            true
        }
    }

    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.d("LADIME", "Jdeme prihlasovat")
        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("LADIME", "Ted se prihlasuju pres google")
                auth.signInWithCredential(credential).await()
            } catch (e: FirebaseAuthException) {
                Log.d("LADIME", "Google spadnul")
                _loginResult.postValue(e.message)
                return@launch
            }

            var docRef = db.collection("Users").document(auth.currentUser.uid)
            val doesExists = docRef.get().await()
            if (doesExists.exists()) {
                Log.d("LADIME", "Skipuj vsechno")
                _loginResult.postValue(localized(R.string.log_in_successful))
                return@launch
            }

            try {
                saveUserProfilePhotoFromGoogleAuth().await()
                Log.d("LADIME", "Ted nahravam obrazek s vysledkem: ")
            } catch (e: StorageException) {
                Log.d("LADIME", "Obrazek spadnul")
                //Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG).show()
                auth.currentUser.delete()
                _loginResult.postValue(e.message)
                return@launch
            }

            try {
                createUserInDatabase().await()
                Log.d("LADIME", "Ted se vyvarim zaznam v databazi")
            } catch (e: FirebaseFirestoreException) {
                Log.d("LADIME", "Databaze spadla")
                _loginResult.postValue(e.message)
                auth.currentUser.delete()
                return@launch
            }
            Log.d("LADIME", "Probehlo to")
            _loginResult.postValue(localized(R.string.log_in_successful))
        }

        GlobalScope.launch(Dispatchers.Main) {
            job.join()
            Log.d("LADIME", "jobDone")
        }

    }

    suspend private fun createUserInDatabase(): Task<Void> {
        val user = HashMap<String, String>()
        storage.reference

        var url: Uri? = null
        try {
            url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
            user["url"] = url.toString()
            Log.d("LADIME", "Url obrazku je: " + url.toString())
        } catch (e: StorageException) {
            Log.d("LADIME", "Nemame obrazek")
        }

        user["uid"] = auth.currentUser.uid
        user["name"] = auth.currentUser.displayName
        user["email"] = auth.currentUser.email
        return db.collection("Users").document(auth.currentUser.uid).set(user)
    }

    suspend private fun saveUserProfilePhotoFromGoogleAuth(): UploadTask {
        var userImageURL = auth.currentUser.photoUrl.toString()
        var storageRef = storage.reference
        var photoRef = storageRef.child("images/" + auth.currentUser.uid + "/profile.jpg")
        val picture = Picasso.get().load(userImageURL).get()
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return photoRef.putBytes(data)
    }

    fun validateResetEmail(email: String) : Pair<Boolean, String> {
        var error = ""
        return if (email == "") {
            error = localized(R.string.mail_cannot_be_empty)
            Pair(false, error)
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            error = localized(R.string.mail_is_not_in_form)
            Pair(false, error)
        } else {
            error = ""
            Pair(true, error)
        }
    }

    fun sendResetPassword(email: String) {
        Log.d("CTVRTEK", "Odesilam reset na mail: " + email)
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("CTVRTEK", "Uspesne odeslanej reset mail")
            } else {
                Log.d("CTVRTEK", "Neco se podelalo")
            }
        }
    }


}