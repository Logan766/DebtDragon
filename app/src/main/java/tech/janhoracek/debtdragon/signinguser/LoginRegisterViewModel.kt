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

    //@Bindable
    val nameContent = formModel.name

    //@Bindable
    val emailContent = formModel.email

   // @Bindable
    val password1Content = formModel.password1

   // @Bindable
    val password2Content = formModel.password2

   // @Bindable
    val nameError = formModel.nameError

   // @Bindable
    val emailError = formModel.emailError

    //@Bindable
    val passwordError = formModel.passwordError

    fun onRegisterClick() {
        //dalsi.value = true
        formModel.validateForRegistration()
        Log.d("HOVNO", "Klikas na cudlik")


    }


}