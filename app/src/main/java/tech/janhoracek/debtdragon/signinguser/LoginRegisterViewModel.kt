package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class LoginRegisterViewModel : ViewModel() {
    public val dalsi = MutableLiveData<Boolean>(false)

    private val formModel = FormModel()

    val error = "Error"

    private var authenticationRepository: AuthenticationRepository = AuthenticationRepository()
    private val userMutableLiveData: MutableLiveData<FirebaseUser> =
        authenticationRepository.getUserMutableLiveData()



    fun register(email: String, password: String) {
        authenticationRepository.registerUser(email, password)
    }

    val userLiveData: LiveData<FirebaseUser> get() = authenticationRepository.userLiveData

    fun getUserMutableLiveData(): MutableLiveData<FirebaseUser> {
        return userMutableLiveData
    }

    fun login() {

    }


    val nameContent = formModel.name
    val emailContent = formModel.email
    val password1Content = formModel.password1
    val password2Content = formModel.password2
    val nameError = formModel.nameError
    val emailError = formModel.emailError
    val passwordError = formModel.passwordError

    fun onRegisterClick() {
        //dalsi.value = true
        formModel.validForRegistration()
        Log.d("HOVNO", "Klikas na cudlik")

    }


}