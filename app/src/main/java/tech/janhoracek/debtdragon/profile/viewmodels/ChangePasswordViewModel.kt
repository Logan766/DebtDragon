package tech.janhoracek.debtdragon.profile.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.EmailAuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class ChangePasswordViewModel : BaseViewModel() {

    val currentPassword = MutableLiveData<String>("")
    val newPassword = MutableLiveData<String>("")
    val newPasswrodCheck = MutableLiveData<String>("")

    private val _passwordError = MutableLiveData<String>("")
    val passwordError: LiveData<String> get() = _passwordError

    private val _newPasswordError = MutableLiveData<String>("")
    val newPasswordError: LiveData<String> get() = _newPasswordError

    private val _newPasswordCheckError = MutableLiveData<String>("")
    val newPasswordCheckError: LiveData<String> get() = _newPasswordCheckError

    sealed class Event {
        object ShowLoading: Event()
        object HideLoading: Event()
        object PasswordChanged: Event()
        data class ShowToast(val message: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun changePassword() {
        GlobalScope.launch(Main) {
            eventChannel.send(Event.ShowLoading)
            if (validatePasswords()) {
                Log.d("CAJ", "Jsme ready menit hesla")
                auth.currentUser.updatePassword(newPassword.value).addOnCompleteListener {task ->
                    if(task.isSuccessful) {
                        GlobalScope.launch(Main) { eventChannel.send(Event.PasswordChanged) }
                        Log.d("CAJ", "Heslo zmeneno")
                    } else {
                        GlobalScope.launch(Main) {
                            eventChannel.send(Event.HideLoading)
                            eventChannel.send(Event.ShowToast("Něco se nepovedlo"))}
                        Log.d("CAJ", "NEco se posralo")
                    }
                }
            } else {
                eventChannel.send(Event.HideLoading)
                eventChannel.send(Event.ShowToast("TEstovaci toast"))
                Log.d("CAJ", "Nejsme ready menit hesla")
            }
        }
    }

    suspend private fun validatePasswords(): Boolean {
        if(currentPassword.value == "") {
            _passwordError.postValue("Heslo nemůže být prázdné")
            return false
        } else {
            _passwordError.postValue("")
        }

        val currentPasswordValidResult = validateCurrentPassword()
        val samePasswordValidResult = validateNewPasswordSame()
        val newPasswordLength = validatePasswordLenght()

        if(currentPasswordValidResult) {
            _passwordError.postValue("")
        } else {
            _passwordError.postValue("Zadané heslo není správné")
        }

        return currentPasswordValidResult && samePasswordValidResult && newPasswordLength
    }

    suspend private fun validateCurrentPassword(): Boolean {
        var result = CompletableDeferred<Boolean>()
        var credential = EmailAuthProvider.getCredential(auth.currentUser.email, currentPassword.value)
        auth.currentUser.reauthenticate(credential).addOnCompleteListener { task ->
            result.complete(task.isSuccessful)
            /*if (task.isSuccessful) {
                _passwordError.value = ""
                result = true
            } else {
                _passwordError.value = "Heslo není správné"
                result = false
            }*/
        }
        return result.await()
    }

    private fun validateNewPasswordSame(): Boolean {
        return if (newPassword.value == newPasswrodCheck.value) {
            _newPasswordCheckError.value = ""
            true
        } else {
            _newPasswordCheckError.value = "Hesla se neshodují"
            false
        }
    }

    private fun validatePasswordLenght(): Boolean {
        return if (newPassword.value!!.length < Constants.PASSWORD_LENGTH) {
            _newPasswordError.value = localized(R.string.passwor_must_have) + Constants.PASSWORD_LENGTH + localized(R.string.number_of_characters)
            false
        } else {
            _newPasswordError.value = ""
            true
        }
    }
}