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


class RegisterViewModel() : BaseViewModel() {

    private val passwordLength = 6

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

    //var registerResult2 = authRepository.registerResult


    fun onRegisterClick() {
        if (validForRegistration()) {
            //authRepository.registerUser(nameContent.value!!,emailContent.value!!,password1Content.value!!)
            auth.createUserWithEmailAndPassword(emailContent.value, password1Content.value).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createUser(auth.currentUser.uid, nameContent.value!!, emailContent.value!!)
                    Log.d("TIGER", "Success creating user in Auth")
                } else {
                    _registerResult.value = task.exception!!.message!!.toString()
                    Log.d("TIGER", task.exception!!.message.toString())
                }
            }
        }

    }

    private fun validForRegistration(): Boolean {
        val nameValidation = validateName()
        val emailValidation = validateEmail()
        val samePasswordValidation = validateSamePassword()
        val lengthPasswordValidation = validatePasswordLength()

        return nameValidation && emailValidation && samePasswordValidation && lengthPasswordValidation
    }

    private fun validateName(): Boolean {
        return if (nameContent.value?.isEmpty()!!) {
            _nameError.value = localized(R.string.type_in_name)
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    private fun validateEmail(): Boolean {
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailContent.value).matches()) {
            _emailError.value = localized(R.string.mail_is_not_in_form)
            false
        } else {
            _emailError.value = ""
            true
        }
    }

    private fun validatePasswordLength(): Boolean {
        return if (!(password1Content.value?.length!! >= passwordLength)) {
            _passwordErrorLength.value =
                localized(R.string.passwor_must_have) + passwordLength + localized(
                    R.string.number_of_characters)
            false
        } else {
            _passwordErrorLength.value = ""
            true
        }
    }

    private fun validateSamePassword(): Boolean {
        return if (password1Content.value != password2Content.value) {
            _passwordErrorSimilarity.value = localized(R.string.password_not_same)
            false
        } else {
            _passwordErrorSimilarity.value = ""
            true
        }
    }

    private fun createUser(uid: String, name: String, email: String) {
        val user = HashMap<String, String>()
        user["uid"] = auth.currentUser.uid
        user["name"] = name
        user["email"] = email
        db.collection("Users").document(uid).set(user)
            .addOnSuccessListener {
                //_registerResult.value = context.getString(R.string.registration_succesful)
                _registerResult.value = localized(R.string.registration_succesful)
                Log.d("TIGER", "Success creating user in database")
            }
            .addOnFailureListener {
                //_registerResult.value = "Registrace neúspěšná"
                _registerResult.value = localized(R.string.registration_failed)
                auth.currentUser.delete()
                Log.d("TIGER", "Failure creating user in database")
            }
    }




}