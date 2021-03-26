package tech.janhoracek.debtdragon.friends.viewmodels

import android.graphics.Color.rgb
import android.util.Log
import androidx.compose.animation.core.snap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants


class FriendDetailViewModel : BaseViewModel() {

    private val _friendData = MutableLiveData<FriendDetailModel>()
    val friendData: LiveData<FriendDetailModel> get() = _friendData

    private val _friendshipData = MutableLiveData<FriendshipModel>()
    val friendshipData: LiveData<FriendshipModel> get() = _friendshipData

    private val _debtSummary = MutableLiveData<String>("Načítám")
    val debtSummary: LiveData<String> get() = _debtSummary

    /*private val _debtFriendPie = MutableLiveData<Int>(0)
    val debtFriendPie: LiveData<Int> get() = _debtFriendPie

    private val _debtMyPie = MutableLiveData<Int>(0)
    val debtMyPie: LiveData<Int> get() = _debtMyPie*/

    private val _pieData = MutableLiveData<PieData>()
    val pieData: LiveData<PieData> get() = _pieData


    sealed class Event {
        object NavigateBack : Event()
        object GenerateQR : Event()
        object CreatePayment : Event()
        data class CreateEditDebt(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun setData(friendshipID: String) {
        //this.friendshipID = friendshipID
        GlobalScope.launch(IO) {
            val friendshipDocument = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).get().await()
            val friendID = if (friendshipDocument.get("member1") == auth.currentUser.uid) {
                friendshipDocument.get("member2").toString()
            } else {
                friendshipDocument.get("member1").toString()
            }

            db.collection(Constants.DATABASE_USERS).document(friendID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _friendData.value = snapshot.toObject(FriendDetailModel::class.java)
                    } else {
                        Log.w("DATA", "Current data null")
                    }
                }
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    _friendshipData.value = snapshot.toObject(FriendshipModel::class.java)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }

            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        var summary_net = 0
                        var myPie = 0
                        var friendPie = 0
                        snapshot.forEach { document ->
                            if (document["payer"] == auth.currentUser.uid) {
                                myPie += (document["value"]).toString().toInt()
                            } else {
                                friendPie += (document["value"]).toString().toInt()
                            }
                        }
                        summary_net = myPie - friendPie
                        if (summary_net > 0) {
                            _debtSummary.value = "Přítel vám dluží ${summary_net}"
                        } else if (summary_net < 0) {
                            _debtSummary.value = "Dlužíte příteli ${summary_net}"
                        } else {
                            _debtSummary.value = "Vaše dluhy jsou vyrovnány"
                        }
                        setupDataForPie(myPie, friendPie)
                    } else {
                        Log.w("DATA", "Current data null")
                    }
                }
        }
    }

    fun onBackPressed() {
        Log.d("RANO", "Posilam signal zpet")
        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack) }

    }

    fun onAddDebtPressed() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.CreateEditDebt(null))
        }
    }

    fun onGenerateQRPressed() {
        GlobalScope.launch(Main) { eventChannel.send(Event.GenerateQR) }
    }

    fun onCreatePaymentPressed() {

    }

    fun setupDataForPie(myPie: Int, friendPie: Int) {
        /*db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("LSTNR", "Listening failed: " + error)
            }

            if (snapshot != null && snapshot.exists()) {
                val userName = snapshot.data?.get("name").toString()
                val summary = myPie + friendPie
                val friendPercentage = (friendPie.toFloat() / summary) * 100
                val myPercentage = (myPie.toFloat() / summary) * 100
                val listPie = ArrayList<PieEntry>()
                val listColors = ArrayList<Int>()

                listPie.add(PieEntry(myPercentage, userName))
                listColors.add(rgb(18, 15, 56))
                listPie.add(PieEntry(friendPercentage, friendData.value!!.name))
                listColors.add(rgb(238, 31, 67))

                val pieDataSet = PieDataSet(listPie, "")
                pieDataSet.colors = listColors

                _pieData.value = PieData(pieDataSet)

            } else {
                Log.w("LSTNR", "Current data null")
            }
        }*/

        val userName = "Já"
        val summary = myPie + friendPie
        val friendPercentage = (friendPie.toFloat() / summary) * 100
        val myPercentage = (myPie.toFloat() / summary) * 100
        val listPie = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        listPie.add(PieEntry(myPercentage, userName))
        listColors.add(rgb(18, 15, 56))
        listPie.add(PieEntry(friendPercentage, friendData.value!!.name))
        listColors.add(rgb(238, 31, 67))

        val pieDataSet = PieDataSet(listPie, "")
        pieDataSet.colors = listColors
        pieDataSet.valueTextSize = 11F
        pieDataSet.valueTextColor = rgb(255, 255, 255)

        _pieData.value = PieData(pieDataSet)


    }


}
