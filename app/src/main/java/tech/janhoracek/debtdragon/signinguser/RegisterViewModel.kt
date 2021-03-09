package tech.janhoracek.debtdragon.signinguser

import android.provider.Settings.Global.getString
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import tech.janhoracek.debtdragon.ApplicationRepository
import tech.janhoracek.debtdragon.AuthRepository
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized

class RegisterViewModel : ViewModel() {

    private val passwordLength = 5

    val authRepository = AuthRepository()

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

    ////
    var registerResult = MutableLiveData<String>("")

    val dalsi = MutableLiveData<Boolean>(false)
    ///


    fun onRegisterClick() {
        //dalsi.value = true
        if (validForRegistration()) {
            authRepository.registerUser(emailContent.value!!,password1Content.value!!).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.d("REBORN", "Povedlo se")
                    registerResult.value = "Uspech"
                } else {
                    Log.d("REBORN", "Oh no")
                    registerResult.value = "Neuspech"
                }
            }
        }
    }

    fun validForRegistration(): Boolean {
        Log.d("HOVNO", "Jmeno: " + nameContent.value)
        Log.d("HOVNO", "Email: " + emailContent.value)
        Log.d("HOVNO", "Heslo1: " + password1Content.value)
        Log.d("HOVNO", "Heslo2: " + password2Content.value)
        Log.d("HOVNO", "Error name: " + nameError)
        Log.d("HOVNO", "Error email " + emailError)
        Log.d("HOVNO", "Error password: " + passwordErrorLength)
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
        Log.d("HOVNO1", "Email je " + emailContent.value)
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
            _passwordErrorLength.value = localized(R.string.passwor_must_have) + passwordLength + localized(R.string.number_of_characters)
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


}