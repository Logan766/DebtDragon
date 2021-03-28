package tech.janhoracek.debtdragon.friends.viewmodels

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.rgb
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
import tech.janhoracek.debtdragon.UserObject
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory
import kotlin.math.abs


class FriendDetailViewModel : BaseViewModel() {

    private val PIE_TYPE_FRIEND = "friend"
    private val PIE_TYPE_USER = "user"

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

    private val _paymentError = MutableLiveData<String>("")
    val paymentError: LiveData<String> get() = _paymentError

    val testovaci = MutableLiveData<String>("1")

    var debtSummaryLive = MutableLiveData<Int>(null)
    var maxValueForSlider = MutableLiveData<Int>()


    private val categorySummaryFriend = HashMap<String, Int>()

    private val categorySummaryUser = HashMap<String, Int>()

    /////////////////////////////////////////////////////

    var counter = MutableLiveData(0)


    sealed class Event {
        object NavigateBack : Event()
        object GenerateQR : Event()
        object CreatePayment : Event()
        object SetupQRforFirstTime: Event()
        object PaymentCreated: Event()
        object HideLoading: Event()
        data class CreateEditDebt(val debtID: String?) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

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
             val neco = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    _friendshipData.value = snapshot.toObject(FriendshipModel::class.java)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }


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
                            _debtSummary.value = "Přítel vám dluží ${summaryNet}"
                        } else if (summaryNet < 0) {
                            _debtSummary.value = "Dlužíte příteli ${summaryNet}"
                        } else {
                            _debtSummary.value = "Vaše dluhy jsou vyrovnány"
                        }
                        Log.d("ZMENA", "Vkladam to max value: " + summaryNet)
                        debtSummaryLive.value = summaryNet
                        maxValueForSlider.value = abs(summaryNet)
                        Log.d("CIGO", "Tak max valuje je teda: " + maxValueForSlider.value)
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

    private fun setupDataForFriendCategoryPie(data: HashMap<String, Int>, type: String) {
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

        if (type == PIE_TYPE_FRIEND) {
            _pieCategoryFriendData.value = PieData(pieDataSet)
        } else {
            _pieCategoryUserData.value = PieData(pieDataSet)
        }

    }

    fun menimeZivoty(value: Float) {
        Log.d("HILL", "Toz hodnota jest: " + value)
    }

    fun createPaymentClick(value: Float) {
        if(validatePayment(value)) {
            GlobalScope.launch(IO) {
                val payment = DebtModel()
                val paymentRef = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.value!!.uid).collection(Constants.DATABASE_DEBTS).document()

                payment.id = paymentRef.id
                payment.name = "Splátka dluhu"
                payment.value = value.toInt()
                payment.category = Constants.DATABASE_DEBT_CATEGORY_PAYMENT
                payment.payer = auth.currentUser.uid
                payment.timestamp = Timestamp.now()

                Log.d("RISE", "Payment ID: " + payment.id)
                Log.d("RISE", "Payment CATEGORY: " + payment.category)
                Log.d("RISE", "Payment Decstiption: " + payment.description)
                Log.d("RISE", "Payment img: " + payment.img)
                Log.d("RISE", "Payment NAME: " + payment.name)
                Log.d("RISE", "Payment PAYER: " + payment.payer)
                Log.d("RISE", "Payment VALUE: " + payment.value)
                Log.d("RISE", "Payment TIMESTAMP: " + payment.timestamp)

                paymentRef.set(payment).await()
                eventChannel.send(Event.PaymentCreated)
            }


        }
    }

    private fun validatePayment(value: Float):Boolean {
        _paymentError.value = ""
        return if(value.toInt() == 0) {
            _paymentError.value = "Částka musí být větší něž 0"
            GlobalScope.launch { eventChannel.send(Event.HideLoading) }
            false
        } else {
            true
        }
    }

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

    fun gatherDataForQR(value: String): String {

            db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).get().addOnCompleteListener {

            }

            val head = "SPD*1.0*"
            val acc = "ACC:${friendData.value!!.account}*"
            //val altAcc = "ALT-ACC:*"
            val moneyAmount = "AM:${value}.00*"
            //val currency = "CC:${currency}*"
            val recieverName = "RN:${friendData.value!!.name}*"
            val message = "MSG:Platba dluhu od uživatele ${UserObject.name} z aplikace DebtDragon ve výši ${value}*"



            return head+acc+moneyAmount+recieverName+message





    }


}
