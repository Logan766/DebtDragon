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
import tech.janhoracek.debtdragon.UserObject
import tech.janhoracek.debtdragon.friends.viewmodels.AddEditDebtViewModel
import tech.janhoracek.debtdragon.profile.models.UserModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class ChangeAccountViewModel: BaseViewModel() {

    val ibanAccount = MutableLiveData<String>()

    private val _accountError = MutableLiveData<String>("")
    val accountError: LiveData<String> get() = _accountError

    private val _accountExists = MutableLiveData<Boolean>(false)
    val accountExists: LiveData<Boolean> get() = _accountExists

    private var userData = UserModel()

    sealed class Event {
        object AccountChanged : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    init {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }
                if (snapshot != null && snapshot.exists()) {
                    userData = snapshot.toObject(UserModel::class.java)!!
                    ibanAccount.postValue(userData.account)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
        //ibanAccount.value = UserObject.account
        Log.d("PERMONIK", "Ucet je: " + ibanAccount.value)
    }

    fun saveAccount() {
        if(isValidIban(ibanAccount.value!!)) {
            _accountError.value = ""
            GlobalScope.launch(IO) {
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).update("account", ibanAccount.value).await()
                eventChannel.send(Event.AccountChanged)
            }
            Log.d("PERMONIK", "Je to validni IBAN")
        } else {
            _accountError.value = "Účet není ve formátu IBAN"
        }
    }

    private fun isValidIban(iban: String): Boolean {
        if (!"^[0-9A-Z]*\$".toRegex().matches(iban)) {
            return false
        }

        val symbols = iban.trim { it <= ' ' }
        if (symbols.length < 15 || symbols.length > 34) {
            return false
        }
        val swapped = symbols.substring(4) + symbols.substring(0, 4)
        return swapped.toCharArray()
            .map { it.toInt() }
            .fold(0) { previousMod: Int, _char: Int ->
                val value = Integer.parseInt(Character.toString(_char.toChar()), 36)
                val factor = if (value < 10) 10 else 100
                (factor * previousMod + value) % 97
            } == 1

    }

    fun deleteAccount() {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).update("account", "").await()
            eventChannel.send(Event.AccountChanged)
        }
    }

}