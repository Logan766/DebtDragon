package tech.janhoracek.debtdragon.groups.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.dashboard.ui.adapters.TopDebtorsAdapter
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.groups.models.BillModel
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import java.lang.Error
import java.lang.Exception

class GroupDetailViewModel : BaseViewModel() {

    val isCurrentUserOwner = MutableLiveData<Boolean>()

    val groupModel = MutableLiveData<GroupModel>()

    val billModel = MutableLiveData<BillModel>()

    val groupDebtModel = MutableLiveData<GroupDebtModel>()

    val friendsToAdd = MutableLiveData<List<String>>()

    val billNameToAdd = MutableLiveData<String>("")

    private val _membersAndNames = MutableLiveData<List<Pair<String, String>>>()
    val membersAndNames: LiveData<List<Pair<String, String>>> get() = _membersAndNames

    private val _membersNames = MutableLiveData<List<String>>()
    val membersNames: LiveData<List<String>> get() = _membersNames

    private val _possibleDebtors = MutableLiveData<List<String>>()
    val possibleDebtors: LiveData<List<String>> get() = _possibleDebtors

    private val _debtorName = MutableLiveData<String>()
    val debtorName: LiveData<String> get() = _debtorName

    private val _payerProfileImg = MutableLiveData<String>()
    val payerProfileImg: LiveData<String> get() = _payerProfileImg

    private val _billNameError = MutableLiveData<String>("")
    val billNameError: LiveData<String> get() = _billNameError

    private val _billDetailName = MutableLiveData<String>("")
    val billDetailName: LiveData<String> get() = _billDetailName

    private val _billDetailPayerName = MutableLiveData<String>("")
    val billDetailPayerName: LiveData<String> get() = _billDetailPayerName

    private val _billDetailPayerImg = MutableLiveData<String>("")
    val billDetailPayerImg: LiveData<String> get() = _billDetailPayerImg

    private val _groupDebtNameError = MutableLiveData<String>("")
    val groupDebtNameError: LiveData<String> get() = _groupDebtNameError

    private val _groupDebtValueError = MutableLiveData<String>("")
    val groupDebtValueError: LiveData<String> get() = _groupDebtValueError

    private val _groupDebtDebtorError = MutableLiveData<String>("")
    val groupDebtDebtorError: LiveData<String> get() = _groupDebtDebtorError

    sealed class Event {
        data class BillCreated(val billID: String) : Event()
        object GroupDebtCreated : Event()
        data class ShowToast(val message: String) : Event()
        object ShowLoading : Event()
        object HideLoading: Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun setData(groupID: String) {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_GROUPS).document(groupID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    groupModel.value = snapshot.toObject(GroupModel::class.java)
                    isCurrentUserOwner.value = groupModel.value!!.owner == auth.currentUser.uid
                    Log.d("VODA", "Spravce: " + isCurrentUserOwner.value)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
    }

    fun getMembers() {
        val membersInGroup = groupModel.value!!.members
        for (member in membersInGroup) {
            Log.d("VODA", "Member ID je: " + member)
        }

        GlobalScope.launch(IO) {
            val friendList = mutableListOf<String>()
            val friends =
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).get().await()
            friends.forEach { document ->
                if (document["member1"] == auth.currentUser.uid) {
                    friendList.add(document["member2"].toString())
                } else {
                    friendList.add(document["member1"].toString())
                }
            }
            for (friend in friendList) {
                Log.d("VODA", "Friend ID je: " + friend)
            }

            val peopleToInvite = friendList.minus(membersInGroup)
            for (member in peopleToInvite) {
                Log.d("VODA", "Person to invite: " + member)
            }

            friendsToAdd.postValue(peopleToInvite)
        }
    }

    fun addMembers(membersToAdd: ArrayList<String>) {
        val document = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
        for (member in membersToAdd) {
            document.update("members", FieldValue.arrayUnion(member))
        }
    }

    fun removeMembers(membersToRemove: ArrayList<String>) {
        val document = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
        for (member in membersToRemove) {
            document.update("members", FieldValue.arrayRemove(member))
        }
    }

    fun getNamesForGroup() {
        GlobalScope.launch(IO) {
            val membersIDs = groupModel.value!!.members
            val membersAndNamesArray: ArrayList<Pair<String, String>> = arrayListOf()
            val membersNames: ArrayList<String> = arrayListOf()

            for (member in membersIDs) {
                val document = db.collection(Constants.DATABASE_USERS).document(member).get().await()
                membersAndNamesArray.add(Pair(member, document[Constants.DATABASE_USER_NAME].toString()))
            }

            for (member in membersAndNamesArray) {
                Log.d("KOFOLA", "ID: " + member.first + " Jméno: " + member.second)
                membersNames.add(member.second)
            }

            //Log.d("KOFOLA", "Najdi Jana Horáčka: " + membersAndNamesArray.find { it.second == "Jan Horáček" }!!.first)

            //val membersNames = mutableListOf<String>()
            //val itemArray: ArrayList<Pair<String, String>> = arrayListOf()
            //val result = itemArray.find { it.first == "ahoj" }

            _membersAndNames.postValue(membersAndNamesArray)
            _membersNames.postValue(membersNames)
        }
    }

    fun setImageForPayer(payerID: String) {
        ///////////////DODELAT!
        Log.d("KOFILA", "ID payera je: " + payerID)
        if (payerID == "") {
            _payerProfileImg.postValue("")
        } else {
            GlobalScope.launch(IO) {
                try {
                    db.collection(Constants.DATABASE_USERS).document(payerID).addSnapshotListener { value, error ->
                        val url = value!![Constants.DATABASE_USER_IMG_URL].toString()
                        if (error != null) {
                            Log.w("LSTNR", error.message.toString())
                            Log.d("KOFILA", "Nastavuju nic protoze error")
                            _payerProfileImg.postValue("")
                        }

                        if (url == "null") {
                            Log.d("KOFILA", "Nastavuju nic: " + url)
                            _payerProfileImg.postValue("")
                        } else {
                            Log.d("KOFILA", "Nastavuju img nebo url jest: " + url)
                            _payerProfileImg.postValue(url)
                        }
                        Log.d("KOFILA", "URL je: " + url)

                    }
                } catch (e: Exception) {
                    Log.d("ERR", e.message.toString())
                }
            }
        }

    }

    fun createBill(payerName: String) {
        if (billNameToAdd.value.isNullOrEmpty()) {
            _billNameError.value = "Název nemůže být prádzný"
        } else {
            GlobalScope.launch(Main) { eventChannel.send(Event.ShowLoading) }
            _billNameError.value = ""
            Log.d("BILL", "Vytvářim účet")
            val payerID = membersAndNames.value!!.find { it.second == payerName }!!.first
            val billRef = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).document()
            val billToAdd = BillModel()

            billToAdd.id = billRef.id
            billToAdd.name = billNameToAdd.value.toString()
            billToAdd.payer = payerID

            Log.d("BILL", "ID billu jest: " + billToAdd.id)
            Log.d("BILL", "Name uctu jest: " + billToAdd.name)
            Log.d("BILL", "Payer ID jest: " + billToAdd.payer)
            Log.d("BILL", "Timestamp jest: " + billToAdd.timestamp)

            billRef.set(billToAdd)
            GlobalScope.launch(Main) {
                eventChannel.send(Event.BillCreated(billToAdd.id))
                eventChannel.send(Event.HideLoading)
            }
        }
    }

    fun setDataForBillDetail(billID: String) {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).document(billID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _billDetailName.postValue(snapshot.data!![Constants.DATABASE_BILL_NAME].toString())
                        val model = snapshot.toObject(BillModel::class.java)
                        Log.d("GDEBT", "Nastavuju model: " + model!!.id)
                        billModel.postValue(model!!)

                        db.collection(Constants.DATABASE_USERS).document(snapshot.data!![Constants.DATABASE_BILL_PAYER].toString())
                            .addSnapshotListener { snapshot, error ->
                                if (error != null) {
                                    Log.w("LSTNR", error.message.toString())
                                }

                                if (snapshot != null && snapshot.exists()) {
                                    _billDetailPayerImg.postValue(snapshot.data!![Constants.DATABASE_USER_IMG_URL].toString())
                                    _billDetailPayerName.postValue(snapshot.data!![Constants.DATABASE_USER_NAME].toString())
                                } else {
                                    Log.w("DATA", "Current data null1")
                                }
                            }
                    } else {
                        Log.w("DATA", "Current data null2")
                    }
                }
        }
    }

    fun setDataForAddDebt(groupDebtID: String?) {
        val memberWithoutPayer = groupModel.value!!.members.toMutableList()
        memberWithoutPayer.remove(billModel.value!!.payer)
        val membersNamesWithoutPayer: ArrayList<String> = arrayListOf()

        for (member in memberWithoutPayer) {
            val memberName = membersAndNames.value!!.find { it.first == member }!!.second
            membersNamesWithoutPayer.add(memberName)
        }

        _possibleDebtors.postValue(membersNamesWithoutPayer)
        _groupDebtDebtorError.value = ""
        _groupDebtValueError.value = ""
        _groupDebtNameError.value = ""

        if (groupDebtID == "none") {
            Log.d("NEDELE", "Ted pridavas novej groupDebt")
            groupDebtModel.value = GroupDebtModel()
            _debtorName.value = ""
        } else {
            Log.d("NEDELE", "Ted to neni novej groupDebt")
            GlobalScope.launch(IO) {
                db.collection(Constants.DATABASE_GROUPS)
                    .document(groupModel.value!!.id)
                    .collection(Constants.DATABASE_BILL)
                    .document(billModel.value!!.id)
                    .collection(Constants.DATABASE_GROUPDEBT)
                    .document(groupDebtID!!)
                    .get().addOnCompleteListener {
                        val debtorID = it.result!!["debtor"].toString()
                        _debtorName.postValue(membersAndNames.value!!.find { it.first == debtorID }!!.second)
                        groupDebtModel.postValue(it.result!!.toObject(GroupDebtModel::class.java))
                    }
            }
        }
    }

    fun saveGroupDebt(debtorName: String) {
        Log.d("GDEBT", "Dluznik jest: " + debtorName)
        if (validateGroupDebt(debtorName)) {
            GlobalScope.launch(IO) {
                eventChannel.send(Event.ShowLoading)
                var groupDebtRef: DocumentReference? = null
                val groupDebtToSave = GroupDebtModel()
                if (groupDebtModel.value!!.id == "") {
                    groupDebtRef = db.collection(Constants.DATABASE_GROUPS)
                        .document(groupModel.value!!.id)
                        .collection(Constants.DATABASE_BILL)
                        .document(billModel.value!!.id)
                        .collection(Constants.DATABASE_GROUPDEBT)
                        .document()
                    groupDebtToSave.id = groupDebtRef.id
                } else {
                    groupDebtRef = db.collection(Constants.DATABASE_GROUPS)
                        .document(groupModel.value!!.id)
                        .collection(Constants.DATABASE_BILL)
                        .document(billModel.value!!.id)
                        .collection(Constants.DATABASE_GROUPDEBT)
                        .document(groupDebtModel.value!!.id)
                    groupDebtToSave.id = groupDebtModel.value!!.id
                    groupDebtToSave.timestamp = groupDebtModel.value!!.timestamp
                }
                groupDebtToSave.payer = billModel.value!!.payer
                groupDebtToSave.debtor = membersAndNames.value!!.find { it.second == debtorName }!!.first
                groupDebtToSave.name = groupDebtModel.value!!.name
                groupDebtToSave.value = groupDebtModel.value!!.value

                Log.d("GDEBT", "S: ID: " + groupDebtToSave.id)
                Log.d("GDEBT", "S: payer: " + groupDebtToSave.payer)
                Log.d("GDEBT", "S: debtor: " + groupDebtToSave.debtor)
                Log.d("GDEBT", "S: name: " + groupDebtToSave.name)
                Log.d("GDEBT", "S: value: " + groupDebtToSave.value)
                Log.d("GDEBT", "S: timestamp: " + groupDebtToSave.timestamp)

                groupDebtRef!!.set(groupDebtToSave).addOnCompleteListener {
                    if (it.isSuccessful) {
                        GlobalScope.launch(Main) {
                            eventChannel.send(Event.GroupDebtCreated)
                        }
                    }
                    GlobalScope.launch(Main) {
                        eventChannel.send(Event.HideLoading)
                    }
                }
            }
        }


    }

    private fun validateGroupDebt(debtorName: String): Boolean {
        val nameValidation = validateGroupDebtName()
        val valueValidation = validateGroupDebtValue()
        val debtorValidation = validateGroupDebtDebtor(debtorName)

        return nameValidation && valueValidation && debtorValidation
    }

    private fun validateGroupDebtName(): Boolean {
        return if (groupDebtModel.value!!.name == "") {
            _groupDebtNameError.value = "Název nemůže být prázdný"
            false
        } else {
            _groupDebtNameError.value = ""
            true
        }
    }

    private fun validateGroupDebtValue(): Boolean {
        return if (groupDebtModel.value!!.value == "") {
            _groupDebtValueError.value = "Výše částky nemůže být prázdná"
            false
        } else if (groupDebtModel.value!!.value.toInt() <= 0) {
            _groupDebtValueError.value = "Částka musí být větší než 0"
            false
        } else {
            _groupDebtValueError.value = ""
            true
        }
    }

    private fun validateGroupDebtDebtor(debtor: String): Boolean {
        return if (debtor == "") {
            _groupDebtDebtorError.value = "Vyberte dlužníka"
            false
        } else {
            _groupDebtDebtorError.value = ""
            true
        }
    }


}