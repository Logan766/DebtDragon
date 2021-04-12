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

/**
 * Change password view model
 *
 * @constructor Create empty Change password view model
 */
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
        object ReadyToChange: Event()
        data class ShowToast(val message: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    /**
     * Validates password and auth before passwrod change
     *
     */
    fun tryToChangePassword() {
        GlobalScope.launch(Main) {
            if (validatePasswords()) {
                eventChannel.send(Event.ReadyToChange)
            }
        }
    }

    /**
     * Change password of current user
     *
     */
    fun changePassword() {
        GlobalScope.launch(Main) {
            eventChannel.send(Event.ShowLoading)
            if (validatePasswords()) {
                auth.currentUser.updatePassword(newPassword.value).addOnCompleteListener {task ->
                    if(task.isSuccessful) {
                        GlobalScope.launch(Main) { eventChannel.send(Event.PasswordChanged) }
                    } else {
                        GlobalScope.launch(Main) {
                            eventChannel.send(Event.HideLoading)
                            eventChannel.send(Event.ShowToast(localized(R.string.change_password_view_model_something_went_wrong)))}
                    }
                }
            } else {
                eventChannel.send(Event.HideLoading)
            }
        }
    }

    /**
     * Validate passwords
     *
     * @return true if valid
     */
    suspend private fun validatePasswords(): Boolean {
        if(currentPassword.value == "") {
            _passwordError.postValue(localized(R.string.change_password_view_model_password_cant_be_empty))
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
            _passwordError.postValue(localized(R.string.change_password_view_model_entered_password_is_invalid))
        }

        return currentPasswordValidResult && samePasswordValidResult && newPasswordLength
    }

    /**
     * Validate current password
     *
     * @return true if valid
     */
    suspend private fun validateCurrentPassword(): Boolean {
        var result = CompletableDeferred<Boolean>()
        var credential = EmailAuthProvider.getCredential(auth.currentUser.email, currentPassword.value)
        auth.currentUser.reauthenticate(credential).addOnCompleteListener { task ->
            result.complete(task.isSuccessful)
        }
        return result.await()
    }

    /**
     * Validate new password same
     *
     * @return true if valid
     */
    private fun validateNewPasswordSame(): Boolean {
        return if (newPassword.value == newPasswrodCheck.value) {
            _newPasswordCheckError.value = ""
            true
        } else {
            _newPasswordCheckError.value = "Hesla se neshodují"
            false
        }
    }

    /**
     * Validate password lenght
     *
     * @return true if valid
     */
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