package tech.janhoracek.debtdragon.profile.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Change name view model
 *
 * @constructor Create empty Change name view model
 */
class ChangeNameViewModel: BaseViewModel() {

    val userName = MutableLiveData<String>()

    private val _nameError = MutableLiveData<String>("")
    val nameError: LiveData<String> get() = _nameError

    sealed class Event {
        object NameChanged : Event()
        object ShowLoading : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    init {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).get().addOnSuccessListener { document ->
                userName.postValue(document[Constants.DATABASE_USER_NAME].toString())
            }
        }
    }

    /**
     * Change name
     *
     */
    fun changeName() {
        if(!userName.value.isNullOrEmpty()) {
            _nameError.value = ""
            GlobalScope.launch(IO) {
                eventChannel.send(Event.ShowLoading)
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).update(Constants.DATABASE_USER_NAME, userName.value).await()
                eventChannel.send(Event.NameChanged)
            }
            Log.d("CAJ", "Muzeme menit jmena")
        } else {
            _nameError.value = "Jméno nemůže být prázdné"
        }
    }

}