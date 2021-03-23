package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class AddEditDebtViewModel: BaseViewModel() {

    private lateinit var friendshipData: FriendshipModel
    private lateinit var friendName: String

    val debtData = MutableLiveData<DebtModel>()

    val categoryList = MutableLiveData<List<String>>()

    val dropDownList = MutableLiveData<List<String>>()

    val payerList = MutableLiveData<ArrayList<String>>()



    val test = MutableLiveData<String>("")

    sealed class Event {
        object NavigateBack : Event()
        object SaveDebt: Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    fun setData(debtId: String?, friendshipData: FriendshipModel, friendName: String) {
        this.friendshipData = friendshipData
        this.friendName = friendName
        val categoryItems = listOf(localized(R.string.category_food), localized(R.string.category_entertainment), localized(R.string.categroy_finance), localized(
                    R.string.category_clothing_access),localized(R.string.category_electronics), localized(R.string.category_other))
        categoryList.value = categoryItems

        val payerNames = listOf("Já", friendName)

        if (debtId == null) {
            Log.d("VALECEK", "Je to novej task")


        } else {
            //edit debt
            //test.value = "STARY"
        }




        val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")
        dropDownList.value = items



    }

    fun onSaveClick() {
        /*
        val neco = Timestamp.now()
        val neco2 = FieldValue.serverTimestamp()
       Log.d("NOC", "Cas jest" + Timestamp.now().toString())*/

    }


    private fun prepareForNewDebt() {

    }



}