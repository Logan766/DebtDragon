package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.utility.BaseViewModel

class AddEditDebtViewModel: BaseViewModel() {

    private val _debtData = MutableLiveData<FriendDetailModel>()
    val debtData: LiveData<FriendDetailModel> get() = _debtData

    private val _payerList = MutableLiveData<ArrayList<String>>()
    val payerList: LiveData<ArrayList<String>> get() = _payerList

    val test = MutableLiveData<String>("")

    sealed class Event {
        object NavigateBack : Event()
        object SaveDebt: Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    fun setData(debtId: String?) {
        if (debtId == null) {
            Log.d("VALECEK", "Je to novej task")


        } else {
            //edit debt
            //test.value = "STARY"
        }
    }

    fun createArrayList() {
        //payerList.value!!.add("Já")
        //payerList.value!!.add("Nějakej user")
    }


    private fun prepareForNewDebt() {

    }



}