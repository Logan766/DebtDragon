package tech.janhoracek.debtdragon.signinguser

import android.util.Log
import androidx.databinding.Bindable
import androidx.databinding.ObservableInt
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
    var nameError = formModel.nameError
    var emailError = formModel.emailError
    var passwordErrorLength = formModel.passwordErrorLength
    var passwordErrorSimilarity = formModel.passwordErrorSimilarity


    fun onRegisterClick() {
        //dalsi.value = true
        if(formModel.validForRegistration()) {
            Log.d("HOVNO1", "Validni")
        }
        Log.d("HOVNO", "Klikas na cudlik")

    }


}