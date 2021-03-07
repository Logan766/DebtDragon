package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.lifecycle.MutableLiveData

class FormModel {


    val name = MutableLiveData<String>("Jmeno")
    val email = MutableLiveData<String>("Email")
    val password1 = MutableLiveData<String>("Heslo1")
    val password2 = MutableLiveData<String>("Heslo2")

    val nameError = MutableLiveData<String>("Chyba jmeno")
    val emailError = MutableLiveData<String>("Chyba mail")
    val passwordError = MutableLiveData<String>("Chyba password")


    fun nameValidation(nameToValidate: String) {

    }

    fun validateForRegistration() {
        Log.d("HOVNO", "Jmeno: " + name.value)
        Log.d("HOVNO", "Email: " + email.value)
        Log.d("HOVNO", "Heslo1: " + password1.value)
        Log.d("HOVNO", "Heslo2: " + password2.value)
        Log.d("HOVNO", "Error name: " + nameError.value)
        Log.d("HOVNO", "Error email " + emailError.value)
        Log.d("HOVNO", "Error password: " + passwordError.value)
    }



}