package tech.janhoracek.debtdragon.friends.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.rgb
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.UserObject
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory
import java.text.Normalizer
import kotlin.math.abs


/**
 * Friend detail view model
 *
 * @constructor Create empty Friend detail view model
 */
class FriendDetailViewModel : BaseViewModel() {

    private val PIE_TYPE_FRIEND = "friend"
    private val PIE_TYPE_USER = "user"
    private val REGEX_UNACCENT = "\\p{InCombiningDiacriticalMarks}+".toRegex()

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

    private val _pieCategoryUserData = MutableLiveData<PieData>()
    val pieCategoryUserData: LiveData<PieData> get() = _pieCategoryUserData

    val paymentError = MutableLiveData<String>("")

    val createPaymentValue = MutableLiveData<String>("0")

    val generateQRvalue = MutableLiveData<String>("0")

    var debtSummaryLive = MutableLiveData<Int>(null)
    var maxValueForSlider = MutableLiveData<Int>()


    private val categorySummaryFriend = HashMap<String, Int>()

    private val categorySummaryUser = HashMap<String, Int>()

    /////////////////////////////////////////////////////


    sealed class Event {
        object NavigateBack : Event()
        object GenerateQR : Event()
        object CreatePayment : Event()
        object SetupQRforFirstTime: Event()
        object PaymentCreated: Event()
        object HideLoading: Event()
        object FriendDeleted: Event()
        object FrienshipExistNoMore: Event()
        data class CreateEditDebt(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    /**
     * Set data data for friend detail
     *
     * @param friendshipID as friendship ID
     */
    fun setData(friendshipID: String) {
        GlobalScope.launch(IO) {

            val friendshipDocument = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).get().await()
            val friendID = if (friendshipDocument.get("member1") == auth.currentUser.uid) {
                friendshipDocument.get("member2").toString()
            } else {
                friendshipDocument.get("member1").toString()
            }

            //Gets friend data from Firebase
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

            //Gets friendship data from Firebase
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


            ////////////////////////////////////////Odstraneni pritele
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("KRAVA", "Cekuju uid a jest: " + snapshot[Constants.DATABASE_USER_UID].toString())
                    /*if(snapshot[Constants.DATABASE_USER_UID].toString().isEmpty()) {
                       GlobalScope.launch(IO) { eventChannel.send(Event.FrienshipExistNoMore) }
                    }*/
                    //_friendshipData.value = snapshot.toObject(FriendshipModel::class.java)
                } else {
                    Log.w("DATA", "Current data null")
                    //GlobalScope.launch(IO) { eventChannel.send(Event.FrienshipExistNoMore) }
                }
            }

            ///////////////////////////////////////


            //Gets all Debts from Firebase
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        var summaryNet = 0
                        var myPie = 0
                        var friendPie = 0
                        categorySummaryFriend.clear()
                        categorySummaryUser.clear()
                        snapshot.forEach { document ->
                            if (document["payer"] == auth.currentUser.uid) {
                                val categoryAndValue = retrieveValueAndCategory(document)
                                myPie += (document["value"]).toString().toInt()
                                categorySummaryUser.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                            } else {
                                val categoryAndValue = retrieveValueAndCategory(document)
                                friendPie += (document["value"]).toString().toInt()
                                categorySummaryFriend.merge(categoryAndValue.first, categoryAndValue.second, Int::plus)
                            }
                        }
                        summaryNet = myPie - friendPie
                        if (summaryNet > 0) {
                            _debtSummary.value = localized(R.string.friend_detail_friend_owes_you) + summaryNet + localized(R.string.currency)
                        } else if (summaryNet < 0) {
                            val absSummaryNet = abs(summaryNet)
                            _debtSummary.value = localized(R.string.friend_detail_you_owe_friend) + absSummaryNet + localized(R.string.currency)
                        } else {
                            _debtSummary.value = localized(R.string.friend_detail_your_debts_are_settled)
                        }
                        debtSummaryLive.value = summaryNet

                        if(summaryNet != 0) {
                            maxValueForSlider.value = abs(summaryNet)
                        } else {
                            maxValueForSlider.value = 1
                        }

                        GlobalScope.launch { eventChannel.send(Event.SetupQRforFirstTime) }
                        setupDataForSummaryPie(myPie, friendPie)
                        setupDataForFriendCategoryPie(categorySummaryFriend, PIE_TYPE_FRIEND)
                        setupDataForFriendCategoryPie(categorySummaryUser, PIE_TYPE_USER)
                    } else {
                        Log.w("DATA", "Current data null")
                    }
                }
        }
    }

    /**
     * On back button pressed
     *
     */
    fun onBackPressed() {
        Log.d("RANO", "Posilam signal zpet")
        GlobalScope.launch(Main) { eventChannel.send(Event.NavigateBack) }

    }

    /**
     * On add debt button pressed
     *
     */
    fun onAddDebtPressed() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.CreateEditDebt(null))
        }
    }

    /**
     * On generate QR button pressed
     *
     */
    fun onGenerateQRPressed() {
        GlobalScope.launch(Main) { eventChannel.send(Event.GenerateQR) }
    }


    /**
     * Setup data for summary pie
     *
     * @param myPie as user debt net value
     * @param friendPie as friend debt net value
     */
    private fun setupDataForSummaryPie(myPie: Int, friendPie: Int) {

        val userName = localized(R.string.Me)
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

    private fun setupDataForFriendCategoryPie(data: HashMap<String, Int>, type: String) {
        val listPieFriend = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        for (item in data) {
            listPieFriend.add(PieEntry(item.value.toFloat(), item.key))
        }


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

        if (type == PIE_TYPE_FRIEND) {
            _pieCategoryFriendData.value = PieData(pieDataSet)
        } else {
            _pieCategoryUserData.value = PieData(pieDataSet)
        }

    }

    fun menimeZivoty(value: Float) {
        Log.d("HILL", "Toz hodnota jest: " + value)
    }

    /**
     * Create payment button click
     *
     * @param value as value of payment
     */
    fun createPaymentClick(value: Float) {
        if(validatePayment(value)) {
            GlobalScope.launch(IO) {
                val payment = DebtModel()
                val paymentRef = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.value!!.uid).collection(Constants.DATABASE_DEBTS).document()

                payment.id = paymentRef.id
                payment.name = localized(R.string.create_payment_debt_payment)
                payment.value = value.toInt()
                payment.category = Constants.DATABASE_DEBT_CATEGORY_PAYMENT
                payment.payer = auth.currentUser.uid
                payment.timestamp = Timestamp.now()

                paymentRef.set(payment).await()
                eventChannel.send(Event.PaymentCreated)
            }


        }
    }

    /**
     * Validate payment
     *
     * @param value of payment
     * @return true if valid
     */
    private fun validatePayment(value: Float):Boolean {
        paymentError.value = ""
        return if(value.toInt() == 0) {
            paymentError.value = localized(R.string.payment_error_value_must_be_greater_than_0)
            GlobalScope.launch { eventChannel.send(Event.HideLoading) }
            false
        } else {
            true
        }
    }

    /**
     * Generate QR code
     *
     * @param text as raw string for QR
     * @return QR as bitmap
     */
    fun generateQRCode(text: String): Bitmap {
        val width = 500
        val height = 500
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val codeWriter = MultiFormatWriter()
        try {
            val bitMatrix = codeWriter.encode(text, BarcodeFormat.QR_CODE, width, height)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        } catch (e: WriterException) {
            Log.d("QR", "generateQRCode: ${e.message}")
        }
        return bitmap
    }

    /**
     * Gather data for QR
     *
     * @param value of payment
     * @return raw string for QR generation
     */
    fun gatherDataForQR(value: String): String {

            val head = "SPD*1.0*"
            val acc = "ACC:${friendData.value!!.account}*"
            val moneyAmount = "AM:${value}*"
            val recieverName = "RN:${friendData.value!!.name.unaccent()}*"
            val message = (localized(R.string.QR_payment_from_user) + UserObject.name!!.unaccent() + localized(R.string.QR_from_aplication_DD_with_value) + value + "*").unaccent()

            return head+acc+moneyAmount+recieverName+message
    }

    /**
     * Unaccent string for QR
     *
     * @return unnaccented string
     */
    fun CharSequence.unaccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return REGEX_UNACCENT.replace(temp, "")
    }

    /**
     * Delete friend
     *
     */
    fun deleteFriend() {
        val delete = GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.value!!.uid).delete().await()
            db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).document(friendData.value!!.uid).delete().await()
            db.collection(Constants.DATABASE_USERS).document(friendData.value!!.uid).collection(Constants.DATABASE_FRIENDSHIPS).document(auth.currentUser.uid).delete().await()
        }
        GlobalScope.launch (Main) {
            delete.join()
            eventChannel.send(Event.FriendDeleted)
        }

    }



}
