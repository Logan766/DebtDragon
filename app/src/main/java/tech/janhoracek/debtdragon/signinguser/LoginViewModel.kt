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

    /**
     * On login click reaction
     *
     */
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


    /**
     * Validate login data
     *
     * @return true if valid
     */
    private fun validToLogin() : Boolean {
        val emailValidation = validateEmail()
        val passwordValidation = validatePassword()
        return emailValidation && passwordValidation
    }

    /**
     * Validate email
     *
     * @return true if valid
     */
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

    /**
     * Validate password
     *
     * @return true if valid
     */
    private fun validatePassword() : Boolean {
        return if (passwordContent.value == "") {
            _passwordError.value = localized(R.string.password_cannot_be_empty)
            false
        } else {
            _passwordError.value = ""
            true
        }
    }

    /**
     * Firebase auth with google
     *
     * @param idToken as login token
     */
    fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val job = GlobalScope.launch(Dispatchers.IO) {
            try {
                auth.signInWithCredential(credential).await()
            } catch (e: FirebaseAuthException) {
                _loginResult.postValue(e.message)
                return@launch
            }

            var docRef = db.collection("Users").document(auth.currentUser.uid)
            val doesExists = docRef.get().await()
            if (doesExists.exists()) {
                _loginResult.postValue(localized(R.string.log_in_successful))
                return@launch
            }

            try {
                saveUserProfilePhotoFromGoogleAuth().await()
            } catch (e: StorageException) {
                //Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG).show()
                auth.currentUser.delete()
                _loginResult.postValue(e.message)
                return@launch
            }

            try {
                createUserInDatabase().await()
            } catch (e: FirebaseFirestoreException) {
                _loginResult.postValue(e.message)
                auth.currentUser.delete()
                return@launch
            }
            _loginResult.postValue(localized(R.string.log_in_successful))
        }

        GlobalScope.launch(Dispatchers.Main) {
            job.join()
        }

    }

    /**
     * Create user in database
     *
     * @return task result
     */
    suspend private fun createUserInDatabase(): Task<Void> {
        val user = HashMap<String, String>()
        storage.reference

        var url: Uri? = null
        try {
            url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
            user["url"] = url.toString()
        } catch (e: StorageException) {
        }

        user["uid"] = auth.currentUser.uid
        user["name"] = auth.currentUser.displayName
        user["email"] = auth.currentUser.email
        return db.collection("Users").document(auth.currentUser.uid).set(user)
    }

    /**
     * Save user profile photo from google auth
     *
     * @return task result
     */
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

    /**
     * Validate reset email
     *
     * @param email as email to send reset password
     * @return true if valid
     */
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

    /**
     * Send reset password
     *
     * @param email as email to send reset password
     */
    fun sendResetPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.d("RSTEMAIL", "Reset email sent")
            } else {
                Log.d("RSTEMAIL", "Something went wrong")
            }
        }
    }


}