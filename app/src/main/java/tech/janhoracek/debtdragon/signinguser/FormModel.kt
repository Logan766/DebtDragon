package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized

class FormModel {

    private val passwordLength = 5

    val name = MutableLiveData<String?>("Jmeno")
    val email = MutableLiveData<String>("Email")
    val password1 = MutableLiveData<String>("Heslo1")
    val password2 = MutableLiveData<String>("Heslo2")

    private val _nameError = MutableLiveData<String>()
    val nameError: LiveData<String> get() = _nameError

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordErrorLength = MutableLiveData<String>()
    val passwordErrorLength: LiveData<String> get() = _passwordErrorLength

    private val _passwordErrorSimilarity = MutableLiveData<String>()
    val passwordErrorSimilarity: LiveData<String> get() = _passwordErrorSimilarity


    fun validForRegistration(): Boolean {
        Log.d("HOVNO", "Jmeno: " + name.value)
        Log.d("HOVNO", "Email: " + email.value)
        Log.d("HOVNO", "Heslo1: " + password1.value)
        Log.d("HOVNO", "Heslo2: " + password2.value)
        Log.d("HOVNO", "Error name: " + nameError)
        Log.d("HOVNO", "Error email " + emailError)
        Log.d("HOVNO", "Error password: " + passwordErrorLength)
        val nameValidation = validateName()
        val emailValidation = validateEmail()
        val samePasswordValidation = validateSamePassword()
        val lengthPasswordValidation = validatePasswordLength()

        return nameValidation && emailValidation && samePasswordValidation && lengthPasswordValidation
        //return validateName() && validateEmail() && validateSamePassword() && validatePasswordLength()
    }

    private fun validateName(): Boolean {
        return if (name.value?.isEmpty()!!) {
            _nameError.value = localized(R.string.type_in_name)
            //_nameError.value = "Prazdne jmeno"
            false
        } else {
            _nameError.value = null
            true
        }
    }

    private fun validateEmail(): Boolean {
        Log.d("HOVNO1", "Email je " + email.value)
        return if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            _emailError.value = localized(R.string.mail_is_not_in_form)
            //_emailError.value = "E-mail neni ve formatu"
            false
        } else {
            _emailError.value = null
            true
        }
    }

    private fun validatePasswordLength(): Boolean {
        return if (!(password1.value?.length!! >= passwordLength)) {
            _passwordErrorLength.value = localized(R.string.passwor_must_have) + passwordLength + localized(R.string.number_of_characters)
            //_passwordErrorLength.value = "Heslo je prilis kratke"
            false
        } else {
            _passwordErrorLength.value = null
            true
        }
    }

    private fun validateSamePassword(): Boolean {
        return if (password1.value != password2.value) {
            _passwordErrorSimilarity.value = localized(R.string.password_not_same)
            //_passwordErrorSimilarity.value = "Hesla se neshoduji"
            false
        } else {
            _passwordErrorSimilarity.value = null
            true
        }
    }


}