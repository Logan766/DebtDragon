package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.lifecycle.MutableLiveData

class FormModel {

    private val passwordLength = 5

    val name = MutableLiveData<String>("Jmeno")
    val email = MutableLiveData<String>("Email")
    val password1 = MutableLiveData<String>("Heslo1")
    val password2 = MutableLiveData<String>("Heslo2")

    val nameError = MutableLiveData<String>("Chyba jmeno")
    val emailError = MutableLiveData<String>("Chyba mail")
    val passwordError = MutableLiveData<String>("Chyba password")


    fun validForRegistration():Boolean {
        Log.d("HOVNO", "Jmeno: " + name.value)
        Log.d("HOVNO", "Email: " + email.value)
        Log.d("HOVNO", "Heslo1: " + password1.value)
        Log.d("HOVNO", "Heslo2: " + password2.value)
        Log.d("HOVNO", "Error name: " + nameError.value)
        Log.d("HOVNO", "Error email " + emailError.value)
        Log.d("HOVNO", "Error password: " + passwordError.value)

        if(name.value?.isEmpty()!!) {
            Log.d("HOVNO1", "Jmeno je prazdne")
            return false
        } else if (!validateEmail()) {
            Log.d("HOVNO1", "Neni validni e-mail")
            return false
        } else if(!validatePasswordLength()) {
            Log.d("HOVNO1", "Neni dlouhy heslo")
            return false
        } else if(!validateSamePassword()) {
            Log.d("HOVNO1", "Nejsou stejny hesla")
            return false
        } else {
            Log.d("HOVNO1", "Je to validni")
            return true
        }
    }

    fun validateEmail(): Boolean {
        Log.d("HOVNO1", "Email je " + email.value)
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email.value).matches()
    }

    fun validatePasswordLength(): Boolean {
        return password1.value?.length!! >= passwordLength
    }

    fun validateSamePassword(): Boolean {
        return password1.value == password2.value
    }



}