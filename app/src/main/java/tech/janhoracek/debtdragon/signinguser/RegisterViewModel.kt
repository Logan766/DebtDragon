package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants


/**
 * Register view model
 *
 * @constructor Create empty Register view model
 */
class RegisterViewModel() : BaseViewModel() {

    val nameContent = MutableLiveData<String>("")
    val emailContent = MutableLiveData<String>("")
    val password1Content = MutableLiveData<String>("")
    val password2Content = MutableLiveData<String>("")

    private val _nameError = MutableLiveData<String>()
    val nameError: LiveData<String> get() = _nameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordErrorLength = MutableLiveData<String>()
    val passwordErrorLength: LiveData<String> get() = _passwordErrorLength

    private val _passwordErrorSimilarity = MutableLiveData<String>()
    val passwordErrorSimilarity: LiveData<String> get() = _passwordErrorSimilarity

    private val _registerResult = MutableLiveData<String>()
    val registerResult: LiveData<String> get() = _registerResult

    /**
     * On register button click
     *
     */
    fun onRegisterClick() {
        if (validForRegistration()) {
            //authRepository.registerUser(nameContent.value!!,emailContent.value!!,password1Content.value!!)
            auth.createUserWithEmailAndPassword(emailContent.value, password1Content.value).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createUser(auth.currentUser.uid, nameContent.value!!, emailContent.value!!)
                } else {
                    _registerResult.value = task.exception!!.message!!.toString()
                }
            }
        }

    }

    /**
     * Validate data for registration
     *
     * @return true if valid
     */
    private fun validForRegistration(): Boolean {
        val nameValidation = validateName()
        val emailValidation = validateEmail()
        val samePasswordValidation = validateSamePassword()
        val lengthPasswordValidation = validatePasswordLength()

        return nameValidation && emailValidation && samePasswordValidation && lengthPasswordValidation
    }

    /**
     * Validate name
     *
     * @return true if valid
     */
    private fun validateName(): Boolean {
        return if (nameContent.value?.isEmpty()!!) {
            _nameError.value = localized(R.string.type_in_name)
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    /**
     * Validate email
     *
     * @return true if valid
     */
    private fun validateEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value).matches()) {
            _emailError.value = localized(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    /**
     * Validate password length
     *
     * @return true if valid
     */
    private fun validatePasswordLength(): Boolean {
        return if (!(password1Content.value?.length!! >= Constants.PASSWORD_LENGTH)) {
            _passwordErrorLength.value =
                localized(R.string.passwor_must_have) + Constants.PASSWORD_LENGTH + localized(
                    R.string.number_of_characters)
            false
        } else {
            _passwordErrorLength.value = ""
            true
        }
    }

    /**
     * Validate passwords are same
     *
     * @return true if passwords are same
     */
    private fun validateSamePassword(): Boolean {
        return if (password1Content.value != password2Content.value) {
            _passwordErrorSimilarity.value = localized(R.string.password_not_same)
            false
        } else {
            _passwordErrorSimilarity.value = ""
            true
        }
    }

    /**
     * Create user in firestore
     *
     * @param uid as user ID
     * @param name as user name
     * @param email as user email
     */
    private fun createUser(uid: String, name: String, email: String) {
        val user = HashMap<String, String>()
        user["uid"] = auth.currentUser.uid
        user["name"] = name
        user["email"] = email
        db.collection("Users").document(uid).set(user)
            .addOnSuccessListener {
                _registerResult.value = localized(R.string.registration_succesful)
            }
            .addOnFailureListener {
                _registerResult.value = localized(R.string.registration_failed)
                auth.currentUser.delete()
            }
    }




}