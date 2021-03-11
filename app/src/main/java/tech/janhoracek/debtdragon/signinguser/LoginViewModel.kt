package tech.janhoracek.debtdragon.signinguser

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized

class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val emailContent = MutableLiveData<String>("")
    val passwordContent = MutableLiveData<String>("")

    private val _emailError = MutableLiveData<String>()
    val emailError: LiveData<String> get() = _emailError

    private val _passwordError = MutableLiveData<String>()
    val passwordError: LiveData<String> get() = _passwordError

    private val _loginResult = MutableLiveData<String>()
    val loginResult: LiveData<String> get() = _loginResult

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

    /*private fun signInWithEmailPassword() {
        auth.signInWithEmailAndPassword(emailContent.value, passwordContent.value).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _loginResult.value = localized(R.string.log_in_successful)
            } else {
                _loginResult.value = task.exception!!.message!!.toString()
            }
        }
    }*/




}