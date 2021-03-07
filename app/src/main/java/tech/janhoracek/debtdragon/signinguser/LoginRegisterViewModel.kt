package tech.janhoracek.debtdragon.signinguser

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import tech.janhoracek.debtdragon.R

class LoginRegisterViewModel : ViewModel() {
    private var loginRepository: LoginRepository = LoginRepository()
    private val userMutableLiveData: MutableLiveData<FirebaseUser> =
        loginRepository.getUserMutableLiveData()

    fun register(email: String, password: String) {
        loginRepository.registerUser(email, password)
    }

    val userLiveData: LiveData<FirebaseUser> get() = loginRepository.userLiveData

    fun getUserMutableLiveData(): MutableLiveData<FirebaseUser> {
        return userMutableLiveData
    }

    fun login() {

    }

    fun onRegisterClick() {
        Log.d("HOVNO", "Klikas na cudlik")
    }
}