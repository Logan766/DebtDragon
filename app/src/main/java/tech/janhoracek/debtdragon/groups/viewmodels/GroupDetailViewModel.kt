package tech.janhoracek.debtdragon.groups.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.groups.models.BillModel
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.DebtCalculator
import java.lang.Exception

/**
 * Group detail view model
 *
 * @constructor Create empty Group detail view model
 */
class GroupDetailViewModel : BaseViewModel() {

    val isCurrentUserOwner = MutableLiveData<Boolean>()

    val groupModel = MutableLiveData<GroupModel>()

    val billModel = MutableLiveData<BillModel>()

    val groupDebtModel = MutableLiveData<GroupDebtModel>()

    val friendsToAdd = MutableLiveData<List<String>>()

    val billNameToAdd = MutableLiveData<String>("")

    val groupDebtAddEditTitle = MutableLiveData<String>("")

    private val _groupSummary = MutableLiveData<String>("0")
    val groupSummary: LiveData<String> get() = _groupSummary

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

    private val _billSummary = MutableLiveData<String>()
    val billSummary: LiveData<String> get() = _billSummary

    private val _billNameError = MutableLiveData<String>("")
    val billNameError: LiveData<String> get() = _billNameError

    private val _billPayerError = MutableLiveData<String>("")
    val billPayerError: LiveData<String> get() = _billPayerError

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
        data class EditGroup(val groupData: GroupModel): Event()
        data class BillCreated(val billID: String) : Event()
        object GroupDebtCreated : Event()
        data class ShowToast(val message: String) : Event()
        object BillDataSet : Event()
        object GroupDeleted : Event()
        object BillDeleted : Event()
        object GroupDebtDeleted : Event()
        object NavigateUp : Event()
        object ShowLoading : Event()
        object HideLoading : Event()
        data class AreAllResolved(val status: Boolean) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    /**
     * Set data for group detail fragment
     *
     * @param groupID as group ID
     */
    fun setData(groupID: String) {
        GlobalScope.launch(IO) {
            // Get group data
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
                    GlobalScope.launch(Main) { eventChannel.send(Event.GroupDeleted) }
                }
            }

            _groupSummary.postValue("0")

            // Get data for summary
            db.collection(Constants.DATABASE_GROUPS).document(groupID).collection(Constants.DATABASE_BILL).addSnapshotListener { bills, error ->
                bills?.forEach { bill ->
                    getGroupSummary(groupID)
                    db.collection(Constants.DATABASE_GROUPS)
                        .document(groupID)
                        .collection(Constants.DATABASE_BILL)
                        .document(bill.id)
                        .collection(Constants.DATABASE_GROUPDEBT)
                        .addSnapshotListener { groupDebt, error ->
                            groupDebt?.forEach {
                                getGroupSummary(groupID)
                            }
                        }
                }
            }
        }
    }

    /**
     * Get group summary
     *
     * @param groupID as group ID
     */
    private fun getGroupSummary(groupID: String) {
        GlobalScope.launch(IO) {
            var summary = 0
            val bills = db.collection(Constants.DATABASE_GROUPS).document(groupID).collection(Constants.DATABASE_BILL).get().await()
            bills?.forEach {
                val billID = it[Constants.DATABASE_BILL_ID].toString()
                val gdebts = db.collection(Constants.DATABASE_GROUPS)
                    .document(groupID)
                    .collection(Constants.DATABASE_BILL)
                    .document(billID)
                    .collection(Constants.DATABASE_GROUPDEBT)
                    .get().await()
                gdebts?.forEach {
                    summary += it[Constants.DATABASE_GROUPDEBT_VALUE].toString().toInt()
                }

            }
            _groupSummary.postValue(summary.toString())
        }
    }

    /**
     * Get members of the group and their data
     *
     */
    fun getMembers() {
        val membersInGroup = groupModel.value!!.members

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
            val peopleToInvite = friendList.minus(membersInGroup)
            friendsToAdd.postValue(peopleToInvite)
        }
    }

    /**
     * Add members to group
     *
     * @param membersToAdd as list of possible memebers
     */
    fun addMembers(membersToAdd: ArrayList<String>) {
        val document = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
        for (member in membersToAdd) {
            document.update("members", FieldValue.arrayUnion(member))
        }
    }

    /**
     * Remove members from group
     *
     * @param membersToRemove as list of group members
     */
    fun removeMembers(membersToRemove: ArrayList<String>) {
        val document = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
        for (member in membersToRemove) {
            document.update("members", FieldValue.arrayRemove(member))
        }
    }

    /**
     * Get names for group members
     *
     */
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
                membersNames.add(member.second)
            }

            _membersAndNames.postValue(membersAndNamesArray)
            _membersNames.postValue(membersNames)
        }
    }

    /**
     * Edit group event implementation
     *
     */
    fun editGroup() {
        GlobalScope.launch(Main) { eventChannel.send(Event.EditGroup(groupModel.value!!)) }
    }

    /**
     * Set image for payer
     *
     * @param payerID as payer ID
     */
    fun setImageForPayer(payerID: String) {
        ///////////////
        if (payerID == "") {
            _payerProfileImg.postValue("")
        } else {
            GlobalScope.launch(IO) {
                try {
                    // Try to get img for url
                    db.collection(Constants.DATABASE_USERS).document(payerID).addSnapshotListener { value, error ->
                        val url = value?.get(Constants.DATABASE_USER_IMG_URL)?.toString()
                        if (error != null) {
                            Log.w("LSTNR", error.message.toString())
                            _payerProfileImg.postValue("")
                        }

                        if (url == "null") {
                            _payerProfileImg.postValue("")
                        } else {
                            if (url != null) {
                                _payerProfileImg.postValue(url!!)
                            } else {
                                _payerProfileImg.postValue("")
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("ERROR", e.message.toString())
                }
            }
        }

    }

    /**
     * Create bill
     *
     * @param payerName as payer name
     */
    fun createBill(payerName: String) {
        if (validateBillToSave(payerName)) {
            GlobalScope.launch(Main) { eventChannel.send(Event.ShowLoading) }
            _billNameError.value = ""
            val payerID = membersAndNames.value!!.find { it.second == payerName }!!.first
            val billRef = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).document()
            val billToAdd = BillModel()

            billToAdd.id = billRef.id
            billToAdd.name = billNameToAdd.value.toString()
            billToAdd.payer = payerID

            billRef.set(billToAdd)
            GlobalScope.launch(Main) {
                eventChannel.send(Event.BillCreated(billToAdd.id))
                eventChannel.send(Event.HideLoading)
            }
        }
    }

    /**
     * Validate bill to save
     *
     * @param payerName as name of payer
     * @return true if valid
     */
    private fun validateBillToSave(payerName: String): Boolean {
        val billNameValidation = validateBillName()
        val payerValidation = validatePayer(payerName)

        return billNameValidation && payerValidation
    }

    /**
     * Validate bill name
     *
     * @return true if valid
     */
    private fun validateBillName(): Boolean {
        return if (billNameToAdd.value.isNullOrEmpty()) {
            _billNameError.value = localized(R.string.group_detail_viewmodel_create_bill_name_cannot_empty)
            false
        } else {
            _billNameError.value = ""
            true
        }
    }

    /**
     * Validate payer
     *
     * @param payerName as name of payer
     * @return true if valid
     */
    private fun validatePayer(payerName: String) : Boolean {
        return if(payerName.isNullOrEmpty()) {
            _billPayerError.value = localized(R.string.group_detail_viewmodel_create_bill_payer_cannot_empty)
            false
        } else {
            _billPayerError.value = ""
            true
        }
    }

    /**
     * Set data for bill detail
     *
     * @param billID as bill ID
     */
    fun setDataForBillDetail(billID: String) {
        GlobalScope.launch(IO) {
            // Gets data for bill
            db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).document(billID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        _billDetailName.postValue(snapshot.data!![Constants.DATABASE_BILL_NAME].toString())
                        val model = snapshot.toObject(BillModel::class.java)
                        billModel.value = model!!
                        GlobalScope.launch(Main) { eventChannel.send(Event.BillDataSet) }

                        // Gets data for bill payer
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
                        GlobalScope.launch(Main) { eventChannel.send(Event.BillDeleted) }
                    }
                }

            // Gets data for bill summary
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_BILL)
                .document(billID)
                .collection(Constants.DATABASE_GROUPDEBT)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.d("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        var billSummary = 0
                        snapshot.forEach { document ->
                            Log.d("BROUCI", "ted je bill id: " + billID)
                            billSummary += document[Constants.DATABASE_GROUPDEBT_VALUE].toString().toInt()
                        }
                        _billSummary.postValue(billSummary.toString())
                    } else {
                        Log.d("DATA", "Current data null")
                    }
                }
        }
    }

    /**
     * Delete bill
     *
     * @param billID as bill ID
     */
    fun deleteBill(billID: String) {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_BILL)
                .document(billID)
                .delete()
                .await()
            eventChannel.send(Event.HideLoading)
        }

    }

    /**
     * Set data for add/edit debt fragment
     *
     * @param groupDebtID
     */
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
            groupDebtAddEditTitle.value = localized(R.string.group_debt_view_model_add_bill_add_new_bill)
            groupDebtModel.value = GroupDebtModel()
            _debtorName.value = ""
        } else {
            groupDebtAddEditTitle.value = localized(R.string.group_debt_view_model_add_bill_edit_bill)
            GlobalScope.launch(IO) {
                db.collection(Constants.DATABASE_GROUPS)
                    .document(groupModel.value!!.id)
                    .collection(Constants.DATABASE_BILL)
                    .document(billModel.value!!.id)
                    .collection(Constants.DATABASE_GROUPDEBT)
                    .document(groupDebtID!!)
                    .get().addOnCompleteListener {
                        val debtorID = it.result!!["debtor"].toString()

                        if (groupModel.value!!.members.contains(debtorID)) {
                            _debtorName.postValue(membersAndNames.value!!.find { it.first == debtorID }!!.second)
                        } else {
                            _debtorName.postValue("")
                        }
                        groupDebtModel.postValue(it.result!!.toObject(GroupDebtModel::class.java))
                    }
            }
        }
    }

    /**
     * Save group debt
     *
     * @param debtorName as debtor name
     */
    fun saveGroupDebt(debtorName: String) {
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

    /**
     * Validate group debt
     *
     * @param debtorName as debtor name
     * @return
     */
    private fun validateGroupDebt(debtorName: String): Boolean {
        val nameValidation = validateGroupDebtName()
        val valueValidation = validateGroupDebtValue()
        val debtorValidation = validateGroupDebtDebtor(debtorName)

        return nameValidation && valueValidation && debtorValidation
    }

    /**
     * Validate group debt name
     *
     * @return true if valid
     */
    private fun validateGroupDebtName(): Boolean {
        return if (groupDebtModel.value!!.name == "") {
            _groupDebtNameError.value = localized(R.string.group_debt_view_model_group_debt_name_cant_be_empty)
            false
        } else {
            _groupDebtNameError.value = ""
            true
        }
    }

    /**
     * Validate group debt value
     *
     * @return true if valid
     */
    private fun validateGroupDebtValue(): Boolean {
        return if (groupDebtModel.value!!.value == "") {
            _groupDebtValueError.value = localized(R.string.group_debt_view_model_group_debt_value_cant_be_empty)
            false
        } else if (groupDebtModel.value!!.value.toInt() <= 0) {
            _groupDebtValueError.value = localized(R.string.group_debt_view_model_group_debt_value_must_be_grater_than_0)
            false
        } else {
            _groupDebtValueError.value = ""
            true
        }
    }

    /**
     * Validate group debt debtor
     *
     * @param debtor
     * @return true if valid
     */
    private fun validateGroupDebtDebtor(debtor: String): Boolean {
        return if (debtor == "") {
            _groupDebtDebtorError.value = localized(R.string.group_debt_view_model_group_debt_select_debtor)
            false
        } else {
            _groupDebtDebtorError.value = ""
            true
        }
    }

    /**
     * Delete group debt
     *
     * @param groupDebtID as group debt ID
     */
    fun deleteGroupDebt(groupDebtID: String) {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_BILL)
                .document(billModel.value!!.id)
                .collection(Constants.DATABASE_GROUPDEBT)
                .document(groupDebtID)
                .delete()
                .await()
            eventChannel.send(Event.GroupDebtDeleted)
            eventChannel.send(Event.HideLoading)
        }
    }

    /**
     * Calculate group and create payments
     *
     */
    fun calculateGroup() {
        GlobalScope.launch(Main) { eventChannel.send(Event.ShowLoading) }
        // Check if group is unlocked and lock it
        if (groupModel.value!!.calculated == "") {
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .update(Constants.DATABASE_GROUPS_STATUS, Constants.DATABASE_GROUPS_STATUS_LOCKED)
        }

        val netMemberValues = HashMap<String, Int>()

        // Get all data from bills
        val getData = GlobalScope.launch(IO) {
            val bills = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).collection(Constants.DATABASE_BILL).get().await()
            bills?.forEach { bill ->
                val groupDebts = bill.reference.collection(Constants.DATABASE_GROUPDEBT).get().await()
                groupDebts?.forEach { debt ->
                    netMemberValues.merge(debt[Constants.DATABASE_GROUPDEBT_DEBTOR].toString(),
                        debt[Constants.DATABASE_GROUPDEBT_VALUE].toString().toInt(),
                        Int::plus)
                    netMemberValues.merge(debt[Constants.DATABASE_GROUPDEBT_PAYER].toString(),
                        -debt[Constants.DATABASE_GROUPDEBT_VALUE].toString().toInt(),
                        Int::plus)
                }
            }

            // Calculate payment and create them in firestore
            val calculator = DebtCalculator()
            GlobalScope.launch(Dispatchers.IO) {
                val results = calculator.calculatePayments(netMemberValues)
                for (result in results) {
                    val paymentRef = db.collection(Constants.DATABASE_GROUPS)
                        .document(groupModel.value!!.id)
                        .collection(Constants.DATABASE_PAYMENT)
                        .document()
                    result.id = paymentRef.id
                    paymentRef.set(result).await()
                }
                db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).update(Constants.DATABASE_GROUPS_STATUS, Constants.DATABASE_GROUPS_STATUS_CALCULATED).await()
                eventChannel.send(Event.HideLoading)
            }
        }
    }

    /**
     * Unlock calculated group and delete payments
     *
     */
    fun unlockCalculatedGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            val payments = db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_PAYMENT)
                .get()
                .await()
            payments.forEach { payment->
                payment.reference.delete()
            }
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .update(Constants.DATABASE_GROUPS_STATUS, "").await()
            eventChannel.send(Event.HideLoading)
        }
    }

    /**
     * Leave group and delete left member id
     *
     */
    fun leaveGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .update(Constants.DATABASE_GROUPS_MEMBERS, FieldValue.arrayRemove(auth.currentUser.uid))
                .await()
            eventChannel.send(Event.NavigateUp)
            eventChannel.send(Event.HideLoading)
        }
    }

    /**
     * Delete group
     *
     */
    fun deleteGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id).delete().await()
            eventChannel.send(Event.NavigateUp)
            eventChannel.send(Event.HideLoading)
        }
    }


    /**
     * Resolve payment
     *
     * @param paymentID as payment ID
     * @param status as payment status
     */
    fun resolvePayment(paymentID: String, status: Boolean) {
        db.collection(Constants.DATABASE_GROUPS)
            .document(groupModel.value!!.id)
            .collection(Constants.DATABASE_PAYMENT)
            .document(paymentID)
            .update(Constants.DATABASE_PAYMENT_RESOLVED, status)
    }

    /**
     * Create friend debt from group payment
     *
     * @param creditorID as creditor ID
     * @param value as payment value
     * @param friendshipID as friendship ID
     */
    fun createFriendDebt(creditorID: String, value: Int, friendshipID: String) {
        GlobalScope.launch(IO) {
            val payment = DebtModel()
            val paymentRef = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS).document()

            payment.id = paymentRef.id
            payment.name = localized(R.string.group_debt_view_model_create_debt_debt_from_group) + groupModel.value!!.name
            payment.value = value
            payment.category = Constants.DATABASE_DEBT_CATEGORY_GDEBT
            payment.payer = creditorID
            payment.timestamp = Timestamp.now()

            paymentRef.set(payment).await()
        }
    }

    /**
     * Check if payments are resolved
     *
     */
    fun checkIfPaymentsAreResolved() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            val payments = db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_PAYMENT)
                .whereEqualTo(Constants.DATABASE_PAYMENT_RESOLVED, false)
                .get()
                .await()
            val areAllResolvedStatus = payments.isEmpty
            eventChannel.send(Event.HideLoading)
            eventChannel.send(Event.AreAllResolved(areAllResolvedStatus))
        }

    }

    /**
     * Reset group, deletes group debts and bills
     *
     */
    fun resetGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            val payments = db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_PAYMENT)
                .get()
                .await()
            payments.forEach { payment->
                payment.reference.delete()
            }

            val bills = db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .collection(Constants.DATABASE_BILL)
                .get()
                .await()
            bills.forEach { bill->
                val gdebts = bill.reference.collection(Constants.DATABASE_GROUPDEBT).get().await()
                gdebts.forEach { gdebt ->
                    gdebt.reference.delete()
                }
                bill.reference.delete()
            }

            db.collection(Constants.DATABASE_GROUPS)
                .document(groupModel.value!!.id)
                .update(Constants.DATABASE_GROUPS_STATUS, "").await()
            eventChannel.send(Event.HideLoading)
            eventChannel.send(Event.NavigateUp)
            eventChannel.send(Event.ShowToast(localized(R.string.group_debt_view_model_group_reseted)))
        }
    }


}