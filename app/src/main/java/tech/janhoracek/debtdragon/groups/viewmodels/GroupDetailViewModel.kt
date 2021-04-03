package tech.janhoracek.debtdragon.groups.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import java.lang.Error
import java.lang.Exception

class GroupDetailViewModel: BaseViewModel() {

    val isCurrentUserOwner = MutableLiveData<Boolean>()

    val groupModel = MutableLiveData<GroupModel>()

    val billModel = MutableLiveData<BillModel>()

    val friendsToAdd = MutableLiveData<List<String>>()

    val billNameToAdd = MutableLiveData<String>("")

    private val _membersAndNames = MutableLiveData<List<Pair<String, String>>>()
    val membersAndNames: LiveData<List<Pair<String, String>>> get() = _membersAndNames

    private val _membersNames = MutableLiveData<List<String>>()
    val membersNames: LiveData<List<String>> get() = _membersNames

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


    sealed class Event {
        data class BillCreated(val billID: String) : Event()
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

    fun getGroupMembers() {

    }

    fun getMembers() {
        val membersInGroup = groupModel.value!!.members
        for(member in membersInGroup) {
            Log.d("VODA", "Member ID je: " + member)
        }

        GlobalScope.launch(IO) {
            val friendList = mutableListOf<String>()
            val friends = db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).get().await()
            friends.forEach { document ->
                if(document["member1"] == auth.currentUser.uid) {
                    friendList.add(document["member2"].toString())
                } else {
                    friendList.add(document["member1"].toString())
                }
            }
            for (friend in friendList) {
                Log.d("VODA", "Friend ID je: " + friend)
            }

            val rozdil = friendList.minus(membersInGroup)
            for (member in rozdil) {
                Log.d("VODA", "Person to invite: " + member)
            }

            friendsToAdd.postValue(rozdil)
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

            for(member in membersIDs) {
                val document = db.collection(Constants.DATABASE_USERS).document(member).get().await()
                membersAndNamesArray.add(Pair(member, document[Constants.DATABASE_USER_NAME].toString()))
            }

            for(member in membersAndNamesArray) {
                Log.d("KOFOLA", "ID: " + member.first + " Jméno: " + member.second)
                membersNames.add(member.second)
            }

            Log.d("KOFOLA", "Najdi Jana Horáčka: " + membersAndNamesArray.find { it.second == "Jan Horáček" }!!.first)

            //val membersNames = mutableListOf<String>()
            //val itemArray: ArrayList<Pair<String, String>> = arrayListOf()
            //val result = itemArray.find { it.first == "ahoj" }

            _membersAndNames.postValue(membersAndNamesArray)
            _membersNames.postValue(membersNames)
        }
    }

    fun setImageForPayer(payerID: String) {
        ///////////////DODELAT!
        Log.d("KOFILA", "ID je: " + payerID)
        GlobalScope.launch (IO) {
            try {
                db.collection(Constants.DATABASE_USERS).document(payerID).addSnapshotListener { value, error ->
                    val url = value!![Constants.DATABASE_USER_IMG_URL].toString()
                    if(url == "null") {
                        _payerProfileImg.postValue("")
                    } else{
                        _payerProfileImg.postValue(url)
                    }
                    Log.d("KOFILA", "URL je: " + url)

                }
            } catch (e: Exception) {
                Log.d("ERR", e.message.toString())
            }

        }
    }

    fun createBill(payerName: String) {
        if(billNameToAdd.value.isNullOrEmpty()) {
            _billNameError.value = "Název nemůže být prádzný"
        } else {
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
            GlobalScope.launch(Main) { eventChannel.send(Event.BillCreated(billToAdd.id)) }
        }
    }

    fun setDataForBillDetail(billID: String) {
        Log.d("SPATNY", "Nastavuju data")
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).document(billID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    _billDetailName.postValue(snapshot.data!![Constants.DATABASE_BILL_NAME].toString())
                    db.collection(Constants.DATABASE_USERS).document(snapshot.data!![Constants.DATABASE_BILL_PAYER].toString()).addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.w("LSTNR", error.message.toString())
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val model = snapshot.toObject(BillModel::class.java)
                            billModel.postValue(model!!)
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


}