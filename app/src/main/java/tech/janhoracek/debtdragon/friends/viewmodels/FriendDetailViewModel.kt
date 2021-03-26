package tech.janhoracek.debtdragon.friends.viewmodels

import android.graphics.Color.rgb
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory


class FriendDetailViewModel : BaseViewModel() {

    private val _friendData = MutableLiveData<FriendDetailModel>()
    val friendData: LiveData<FriendDetailModel> get() = _friendData

    private val _friendshipData = MutableLiveData<FriendshipModel>()
    val friendshipData: LiveData<FriendshipModel> get() = _friendshipData

    private val _debtSummary = MutableLiveData<String>("Načítám")
    val debtSummary: LiveData<String> get() = _debtSummary

    private val _pieData = MutableLiveData<PieData>()
    val pieData: LiveData<PieData> get() = _pieData

    private val _pieCategoryFriendData = MutableLiveData<PieData>()
    val pieCategoryFriendData: LiveData<PieData> get() = _pieCategoryFriendData

    private val categorySummaryFriend = HashMap<String, Int>()


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
                        categorySummaryFriend.clear()
                        snapshot.forEach { document ->
                            if (document["payer"] == auth.currentUser.uid) {
                                myPie += (document["value"]).toString().toInt()

                            } else {
                                val categoryAndValue = retrieveValueAndCategory(document)
                                friendPie += (document["value"]).toString().toInt()
                                categorySummaryFriend.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
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
                        setupDataForSummaryPie(myPie, friendPie)
                        setupDataForFriendCategoryPie(categorySummaryFriend)
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

    fun setupDataForSummaryPie(myPie: Int, friendPie: Int) {
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

    private fun retrieveValueAndCategory(document: DocumentSnapshot): Pair<String, Int> {
        val category = transformDatabaseStringToCategory(document[Constants.DATABASE_DEBTS_CATEGORY].toString())
        val value = document[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
        return Pair(category, value)
    }

    private fun setupDataForFriendCategoryPie(data: HashMap<String, Int>) {
        val listPieFriend = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        for (item in data) {
            Log.d("VALHALA", "Kategorie jsou: " + item.key + " = " + item.value)
            listPieFriend.add(PieEntry(item.value.toFloat(), item.key))
        }

        /*listColors.add(rgb(27, 16, 56))
        listColors.add(rgb(41, 17, 57))
        listColors.add(rgb(58, 18, 58))
        listColors.add(rgb(72, 19, 59))
        listColors.add(rgb(59, 20, 60))
        listColors.add(rgb(105, 21, 60))
        listColors.add(rgb(120, 22, 61))*/

        listColors.add(rgb(27, 16, 56))
        listColors.add(rgb(58, 18, 58))
        listColors.add(rgb(88, 20, 60))
        listColors.add(rgb(120, 22, 61))
        listColors.add(rgb(151, 25, 63))
        listColors.add(rgb(183, 27, 64))
        listColors.add(rgb(215, 29, 66))


        val pieDataSet = PieDataSet(listPieFriend, "")
        pieDataSet.colors = listColors
        pieDataSet.valueTextSize = 11F
        pieDataSet.valueTextColor = rgb(255, 255, 255)


        _pieCategoryFriendData.value = PieData(pieDataSet)
    }

    private fun setupDataForUserCategoryPie(data: HashMap<String, Int>) {
        for (item in data) {
            Log.d("VALHALA", "Kategorie jsou: " + item.key + " = " + item.value)
        }
    }


}
