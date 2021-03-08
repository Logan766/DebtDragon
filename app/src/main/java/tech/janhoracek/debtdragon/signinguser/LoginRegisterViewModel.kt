package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import tech.janhoracek.debtdragon.ApplicationRepository

class LoginRegisterViewModel : ViewModel() {


    public val dalsi = MutableLiveData<Boolean>(false)

    private val formModel = FormModel()

    //private var authenticationRepository: AuthenticationRepository = AuthenticationRepository()
    private val userMutableLiveData: MutableLiveData<FirebaseUser> =
        ApplicationRepository.getUserMutableLiveData()


    fun register(name: String, email: String, password: String) {
        ApplicationRepository.registerUser(name, email, password)
    }

    val userLiveData: LiveData<FirebaseUser> get() = ApplicationRepository.userLiveData

    fun getUserMutableLiveData(): MutableLiveData<FirebaseUser> {
        return userMutableLiveData
    }

    fun login() {

    }


    val nameContent = formModel.name
    val emailContent = formModel.email
    val password1Content = formModel.password1
    val password2Content = formModel.password2
    var nameError = formModel.nameError
    var emailError = formModel.emailError
    var passwordErrorLength = formModel.passwordErrorLength
    var passwordErrorSimilarity = formModel.passwordErrorSimilarity
    var registerResult = MutableLiveData<String>()


    fun onRegisterClick() {
        //dalsi.value = true
        if (formModel.validForRegistration()) {
            register(nameContent.value!!, emailContent.value!!, password1Content.value!!)
            Log.d("HOVNO", "Klikas na cudlik")
        }


    }

}