package tech.janhoracek.debtdragon.dashboard.viewmodels

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory

class DashboradViewModel : BaseViewModel() {

    private val PIE_TYPE_FRIEND = "friend"
    private val PIE_TYPE_USER = "user"

    private val _summaryPieData = MutableLiveData<PieData>()
    val summaryPieData: LiveData<PieData> get() = _summaryPieData

    private val _userCategoryPieData = MutableLiveData<PieData>()
    val userCategoryPieData: LiveData<PieData> get() = _userCategoryPieData

    private val _friendsCategoryPieData = MutableLiveData<PieData>()
    val friendsCategoryPieData: LiveData<PieData> get() = _friendsCategoryPieData

    private val _topDebtors = MutableLiveData<MutableList<Pair<String, Int>>>()
    val topDebtors:  LiveData<MutableList<Pair<String, Int>>> get() = _topDebtors

    private val _topCreditors = MutableLiveData<MutableList<Pair<String, Int>>>()
    val topCreditors:  LiveData<MutableList<Pair<String, Int>>> get() = _topCreditors

    private val categorySummaryFriends = HashMap<String, Int>()
    private val categorySummaryUser = HashMap<String, Int>()
    private val summaryOfFriendsDebts = HashMap<String, Int>()
    var summaryNet = 0
    var mySummary = 0
    var friendsSummary = 0

    val pozdrav = "Ahoj"

    init {
        GlobalScope.launch(IO) {
            Log.d("ZIPPO", "Spoustim")
            db.collection(Constants.DATABASE_FRIENDSHIPS)
                .whereArrayContains("members", auth.currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("ZIPPO", error.message.toString())
                    }

                    if (snapshot != null) {
                        Log.d("INSANE", "Ted bych nacital podle frienships")
                        snapshot.forEach { document ->
                            db.collection(Constants.DATABASE_FRIENDSHIPS).document(document.id).collection(Constants.DATABASE_DEBTS)
                                .addSnapshotListener { snapshot, error ->
                                    Log.d("INSANE", "Ted bych nacital podle debts")
                                }
                        }
                    } else {
                        Log.d("ZIPPO", "Current data null")
                    }
                }
        }
    }

    private fun printuj(
        mySummary: Int,
        friendSummary: Int,
        summaryDebts: HashMap<String, Int>,
        categoryFriends: HashMap<String, Int>,
        categoryUser: HashMap<String, Int>
    ) {
        Log.d("DASHIK", "------------------Zacatek--------------------")
        Log.d("DASHIK", "Moje summary jest: " + mySummary)
        Log.d("DASHIK", "Friend summary jest: " + friendSummary)


        var counter = 1
        for (item in summaryDebts) {
            Log.d("DASHIK", counter.toString() + ":Dluznik a castka celkem: " + item.key + ": " + item.value)
            counter++
        }

        counter = 1
        for (item in categoryFriends) {
            Log.d("DASHIK", counter.toString() + ":Ostatnich kategorie: " + item.key + ": " + item.value)
            counter++
        }

        counter = 1
        for (item in categoryUser) {
            Log.d("DASHIK", counter.toString() + ":Moje kategorie: " + item.key + ": " + item.value)
            counter++
        }
        Log.d("DASHIK", "------------------Konec--------------------")
    }

    fun printuj2() {
        Log.d("DASHIK", "------------------Zacatek--------------------")
        Log.d("DASHIK", "Moje summary jest: " + mySummary)
        Log.d("DASHIK", "Friend summary jest: " + friendsSummary)

        var counter = 1
        for (item in summaryOfFriendsDebts) {
            Log.d("DASHIK", counter.toString() + ":Dluznik a castka celkem: " + item.key + ": " + item.value)
            counter++
        }

        counter = 1
        for (item in categorySummaryFriends) {
            Log.d("DASHIK", counter.toString() + ":Ostatnich kategorie: " + item.key + ": " + item.value)
            counter++
        }

        counter = 1
        for (item in categorySummaryUser) {
            Log.d("DASHIK", counter.toString() + ":Moje kategorie: " + item.key + ": " + item.value)
            counter++
        }
        Log.d("DASHIK", "------------------Konec--------------------")
    }

    private fun retrieveValueAndCategory(document: DocumentSnapshot): Pair<String, Int> {
        val category = transformDatabaseStringToCategory(document[Constants.DATABASE_DEBTS_CATEGORY].toString())
        val value = document[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
        return Pair(category, value)
    }


    fun nactiPolozky() {
        val categorySummaryFriends = HashMap<String, Int>()
        val categorySummaryUser = HashMap<String, Int>()
        val summaryOfFriendsDebts = HashMap<String, Int>()
        var summaryNet = 0
        var mySummary = 0
        var friendsSummary = 0
        val job = GlobalScope.launch(IO) {

            val friendships = db.collection(Constants.DATABASE_FRIENDSHIPS).whereArrayContains("members", auth.currentUser.uid).get().await()

            friendships.forEach { friendship ->
                var friendID = if (friendship["member1"] == auth.currentUser.uid) {
                    friendship["member2"].toString()
                } else {
                    friendship["member1"].toString()
                }

                val debts = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendship.id).collection(Constants.DATABASE_DEBTS).get().await()
                debts.forEach { debt ->
                    if (debt[Constants.DATABASE_DEBT_PAYER] == auth.currentUser.uid) {
                        val categoryAndValue = retrieveValueAndCategory(debt)
                        mySummary += debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
                        summaryOfFriendsDebts.merge(friendID, debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt(), Int::plus)
                        categorySummaryUser.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                        Log.d("Dashik", "Merguju: " + friendID + " s hodnotou +" + debt[Constants.DATABASE_DEBTS_VALUE].toString())
                        Log.d("Dashik", "Hodnota " + friendID + " je ted: " + summaryOfFriendsDebts.get(friendID))
                    } else {
                        val categoryAndValue = retrieveValueAndCategory(debt)
                        friendsSummary += debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
                        summaryOfFriendsDebts.merge(friendID, -debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt(), Int::plus)
                        categorySummaryFriends.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                        Log.d("Dashik", "Merguju: " + friendID + " s hodnotou -" + debt[Constants.DATABASE_DEBTS_VALUE].toString())
                        Log.d("Dashik", "Hodnota " + friendID + " je ted: " + summaryOfFriendsDebts.get(friendID))

                    }
                }
            }
        }
        GlobalScope.launch(IO) {
            job.join()
            setupDataForSummaryPie(mySummary, friendsSummary)
            setupDataForFriendsCategoryPie(categorySummaryUser, PIE_TYPE_USER)
            setupDataForFriendsCategoryPie(categorySummaryFriends, PIE_TYPE_FRIEND)

            printuj(mySummary, friendsSummary, summaryOfFriendsDebts, categorySummaryFriends, categorySummaryUser)
            getDataForTop5(summaryOfFriendsDebts)
        }
    }

    fun setupDataForSummaryPie(mySummary: Int, friendSummary: Int) {
        val summary = mySummary + friendSummary
        val friendsPercentage = (friendSummary.toFloat() / summary) * 100
        val myPrecentage = (mySummary.toFloat() / summary) * 100
        val listPie = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        listPie.add(PieEntry(myPrecentage, "Já"))
        listColors.add(Color.rgb(18, 15, 56))
        listPie.add(PieEntry(friendsPercentage, "Přátelé"))
        listColors.add(Color.rgb(238, 31, 67))

        val pieDataSet = PieDataSet(listPie, "")
        pieDataSet.colors = listColors
        pieDataSet.valueTextSize = 11F
        pieDataSet.valueTextColor = Color.rgb(255, 255, 255)

        _summaryPieData.postValue(PieData(pieDataSet))
        //_summaryPieData.value = PieData(pieDataSet)
    }

    fun setupDataForFriendsCategoryPie(data: HashMap<String, Int>, type: String) {
        val listPieFriend = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        for (item in data) {
            listPieFriend.add(PieEntry(item.value.toFloat(), item.key))
        }

        listColors.add(Color.rgb(27, 16, 56))
        listColors.add(Color.rgb(58, 18, 58))
        listColors.add(Color.rgb(88, 20, 60))
        listColors.add(Color.rgb(120, 22, 61))
        listColors.add(Color.rgb(151, 25, 63))
        listColors.add(Color.rgb(183, 27, 64))
        listColors.add(Color.rgb(215, 29, 66))

        val pieDataSet = PieDataSet(listPieFriend, "")
        pieDataSet.colors = listColors
        pieDataSet.valueTextSize = 11F
        pieDataSet.valueTextColor = Color.rgb(255, 255, 255)
        pieDataSet.sliceSpace = 3F
        pieDataSet.valueFormatter = PercentFormatter()

        if (type == PIE_TYPE_FRIEND) {
            _friendsCategoryPieData.postValue(PieData(pieDataSet))
        } else {
            _userCategoryPieData.postValue(PieData(pieDataSet))
        }
    }

    fun getDataForTop5(data: HashMap<String, Int>) {
        //val sortedMap = data.toSortedMap(compareBy { it })
        val sortedMap1 = data.toList().sortedBy { (_, value) -> value}
        val sortedMap2 = data.toList().sortedBy { (_, value) -> value}.reversed()

        val topCreditors: MutableList<Pair<String, Int>> = mutableListOf()
        val topDebtors: MutableList<Pair<String, Int>> = mutableListOf()

        for (e in sortedMap1) {
            Log.d("SORTUJ", "1: " + e)
        }

        for (b in sortedMap2) {
            Log.d("SORTUJ", "2: " + b)
        }

        for(i in 0..2) {
            if(i <= sortedMap1.size) {
                if (sortedMap1[i].second < 0) {
                    topCreditors.add(sortedMap1[i])
                }
            }
        }

        for(i in 0..2) {
            if(i <= sortedMap2.size) {
                if (sortedMap2[i].second > 0) {
                    topDebtors.add(sortedMap2[i])
                }
            }
        }

        _topCreditors.value = topCreditors
        _topDebtors.value = topDebtors

        for (entry in topCreditors) {
            Log.d("SORTUJ", "1S: " + entry)
        }

        for (entry in topDebtors) {
            Log.d("SORTUJ", "2S: " + entry)
        }






        /*for (entry in sortedMap) {
            Log.d("SORTUJ", "ID: " + entry.key + " Hodnota: " + entry.value)
        }

        val sortedList = sortedMap.toList()
        val topCreditors: MutableList<Pair<String, Int>> = mutableListOf()

        for (i in 0..2) {
            if(sortedList[i].second < 0) {
                Log.d("SORTUJ", "F: " + sortedList[i])
                topCreditors.add(sortedList[i])
            }
        }

        Log.d("SORTUJ", "T: " + topCreditors.size)

        for (i in 0..topCreditors.size-1) {
            Log.d("SORTUJ", "C: " + topCreditors[i])
        }*/


        ///////////////////////////////////////////////////////////////////////

        /*val sortedList = sortedMap.toList()
        var topDebtors : MutableList<Pair<String, Int>>? = null
        var topCreditors: MutableList<Pair<String, Int>>? = null

        for (i in 0..2) {
            if(sortedList[i].second > 0) {
                topDebtors!!.add(sortedList[i])
            }
        }*/

        /*for (i in sortedList.size..sortedList.size-2) {
            if(sortedList[i].second > 0) {
                topCreditors!!.add(sortedList[i])
            }
        }*/

        /*if (topCreditors != null) {
            for (entry in topCreditors) {
                Log.d("SEZNAM", "Top creditor: " + entry)
            }
        }*/

        /*if (topDebtors != null) {
            for (entry in topDebtors) {
                Log.d("SEZNAM", "Top debtor: " + entry)
            }
        }*/

        //return sortedMap
    }

    fun pajova(vstup: String) {
        Log.d("PAJICEK", vstup + " pajo")
    }

}