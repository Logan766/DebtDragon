package tech.janhoracek.debtdragon.dashboard.viewmodels

import android.graphics.Color
import android.net.Uri
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
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.UserObject
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory
import java.lang.Exception

/**
 * Dashborad view model
 *
 * @constructor Create empty Dashborad view model
 */
class DashboradViewModel : BaseViewModel() {

    private val PIE_TYPE_FRIEND = "friend"
    private val PIE_TYPE_USER = "user"

    private val _userImage = MutableLiveData<String>()
    val userImage: LiveData<String> get() = _userImage

    private val _summaryPieData = MutableLiveData<PieData>()
    val summaryPieData: LiveData<PieData> get() = _summaryPieData

    private val _userCategoryPieData = MutableLiveData<PieData>()
    val userCategoryPieData: LiveData<PieData> get() = _userCategoryPieData

    private val _friendsCategoryPieData = MutableLiveData<PieData>()
    val friendsCategoryPieData: LiveData<PieData> get() = _friendsCategoryPieData

    private val _topDebtors = MutableLiveData<List<Pair<String, Int>>>()
    val topDebtors: LiveData<List<Pair<String, Int>>> get() = _topDebtors

    private val _topCreditors = MutableLiveData<List<Pair<String, Int>>>()
    val topCreditors: LiveData<List<Pair<String, Int>>> get() = _topCreditors

    private val _summary = MutableLiveData<Int>()
    val summary: LiveData<Int> get() = _summary

    var test = MutableLiveData<List<Pair<String, Int>>>()

    private val categorySummaryFriends = HashMap<String, Int>()
    private val categorySummaryUser = HashMap<String, Int>()
    private val summaryOfFriendsDebts = HashMap<String, Int>()
    var summaryNet = 0
    var mySummary = 0
    var friendsSummary = 0

    init {
        GlobalScope.launch(IO) {
            // Sets listener to calculate total debts for friendship and debts
            db.collection(Constants.DATABASE_FRIENDSHIPS)
                .whereArrayContains(Constants.DATABASE_FRIENDSHIPS_MEMBERS, auth.currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        calculateDebt()
                        snapshot.forEach { document ->
                            db.collection(Constants.DATABASE_FRIENDSHIPS)
                                .document(document.id)
                                .collection(Constants.DATABASE_DEBTS)
                                .addSnapshotListener { snapshot, error ->
                                    calculateDebt()
                                }
                        }
                    } else {
                        Log.d("LSTNR", "Current data null")
                    }
                }
            var url: Uri? = null
            // Tries to load img from online storage
            try {
                url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
            } catch (e: Exception) {

            }
            _userImage.postValue(url.toString())
        }
    }

    /**
     * Retrieve value and category based on category strings in database
     *
     * @param document
     * @return Pair(name of category, value)
     */
    private fun retrieveValueAndCategory(document: DocumentSnapshot): Pair<String, Int> {
        val category = transformDatabaseStringToCategory(document[Constants.DATABASE_DEBTS_CATEGORY].toString())
        val value = document[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
        return Pair(category, value)
    }


    /**
     * Calculate debt
     *
     * Calculates all necessary data for dashboard
     */
    private fun calculateDebt() {
        val categorySummaryFriends = HashMap<String, Int>()
        val categorySummaryUser = HashMap<String, Int>()
        val summaryOfFriendsDebts = HashMap<String, Int>()
        var summaryNet = 0
        var mySummary = 0
        var friendsSummary = 0
        val job = GlobalScope.launch(IO) {

            try {
                //Gets all current user friendships
                val friendships = db.collection(Constants.DATABASE_FRIENDSHIPS)
                    .whereArrayContains(Constants.DATABASE_FRIENDSHIPS_MEMBERS, UserObject.uid.toString())
                    .get()
                    .await()

                friendships.forEach { friendship ->
                    // Determines who is who
                    val friendID = if (friendship["member1"] == auth.currentUser.uid) {
                        friendship["member2"].toString()
                    } else {
                        friendship["member1"].toString()
                    }

                    // Gets friendships data
                    val debts = db.collection(Constants.DATABASE_FRIENDSHIPS)
                        .document(friendship.id)
                        .collection(Constants.DATABASE_DEBTS)
                        .get()
                        .await()

                    // Calculates data for dashboard from friendships
                    debts.forEach { debt ->
                        if (debt[Constants.DATABASE_DEBT_PAYER] == auth.currentUser.uid) {
                            val categoryAndValue = retrieveValueAndCategory(debt)
                            mySummary += debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
                            summaryOfFriendsDebts.merge(friendID, debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt(), Int::plus)
                            categorySummaryUser.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                        } else {
                            val categoryAndValue = retrieveValueAndCategory(debt)
                            friendsSummary += debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt()
                            summaryOfFriendsDebts.merge(friendID, -debt[Constants.DATABASE_DEBTS_VALUE].toString().toInt(), Int::plus)
                            categorySummaryFriends.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                        }
                    }
                }
            } catch (e: Exception) {

            }
        }

        // Sends data to variables for Views
        GlobalScope.launch(IO) {
            job.join()
            _summary.postValue(mySummary - friendsSummary)
            setupDataForSummaryPie(mySummary, friendsSummary)
            setupDataForFriendsCategoryPie(categorySummaryUser, PIE_TYPE_USER)
            setupDataForFriendsCategoryPie(categorySummaryFriends, PIE_TYPE_FRIEND)
            getDataForTop3(summaryOfFriendsDebts)
        }

    }

    /**
     * Setup data for summary pie
     *
     * @param mySummary summary of debts of current user
     * @param friendSummary summary of debts of friends
     */
    fun setupDataForSummaryPie(mySummary: Int, friendSummary: Int) {
        val summary = mySummary + friendSummary
        val friendsPercentage = (friendSummary.toFloat() / summary) * 100
        val myPrecentage = (mySummary.toFloat() / summary) * 100
        val listPie = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        listPie.add(PieEntry(myPrecentage, localized(R.string.Me)))
        listColors.add(Color.rgb(18, 15, 56))
        listPie.add(PieEntry(friendsPercentage, localized(R.string.Friends)))
        listColors.add(Color.rgb(238, 31, 67))

        val pieDataSet = PieDataSet(listPie, "")
        pieDataSet.colors = listColors
        pieDataSet.valueTextSize = 11F
        pieDataSet.valueTextColor = Color.rgb(255, 255, 255)

        _summaryPieData.postValue(PieData(pieDataSet))
    }

    /**
     * Setup data for friends category pie
     *
     * @param data data for Category Pie (friends or users)
     * @param type determines which data comes in @param data (friends or users)
     */
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

        // Determines where to post data after settings based on @type
        if (type == PIE_TYPE_FRIEND) {
            _friendsCategoryPieData.postValue(PieData(pieDataSet))
        } else {
            _userCategoryPieData.postValue(PieData(pieDataSet))
        }
    }

    /**
     * Get data for top3 debtors and creditors
     *
     * @param data as net of debts of each friend <ID of friend, net of debts>
     */
    private fun getDataForTop3(data: HashMap<String, Int>) {
        val sortedMap1 = data.toList().sortedBy { (_, value) -> value }
        val sortedMap2 = data.toList().sortedBy { (_, value) -> value }.reversed()

        val topCreditors: MutableList<Pair<String, Int>> = mutableListOf()
        val topDebtors: MutableList<Pair<String, Int>> = mutableListOf()

        for (i in 0..2) {
            if (i <= sortedMap1.size - 1) {
                if (sortedMap1[i].second < 0) {
                    topCreditors.add(sortedMap1[i])
                }
            }
        }

        for (i in 0..2) {
            if (i <= sortedMap2.size - 1) {
                if (sortedMap2[i].second > 0) {
                    topDebtors.add(sortedMap2[i])
                }
            }
        }

        try {
            test.postValue(topCreditors)
        } catch (e: Exception) {
            Log.d("ERROR", e.message.toString())
        }

        try {
            _topCreditors.postValue(topCreditors)
            _topDebtors.postValue(topDebtors)
            GlobalScope.launch(Main) { _topDebtors.value = _topDebtors.value }
        } catch (e: Error) {
            Log.d("ERROR", e.message.toString())
        }
    }

    override fun onCleared() {
        super.onCleared()
    }


}