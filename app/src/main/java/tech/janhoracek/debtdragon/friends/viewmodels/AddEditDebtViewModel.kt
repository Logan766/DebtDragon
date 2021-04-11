package tech.janhoracek.debtdragon.friends.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformCategoryToDatabaseString
import tech.janhoracek.debtdragon.utility.transformDatabaseStringToCategory
import java.lang.Exception

/**
 * Add edit debt view model
 *
 * @constructor Create empty Add edit debt view model
 */
class AddEditDebtViewModel : BaseViewModel() {

    private lateinit var friendshipData: FriendshipModel
    private lateinit var friendName: String
    private lateinit var friendId: String
    private var timestamp: Timestamp? = null

    private lateinit var debtData: DebtModel
    private var debtID: String? = null

    val categoryList = MutableLiveData<List<String>>()
    val payerList = MutableLiveData<List<String>>()

    val debtId = MutableLiveData<String>("")
    val debtName = MutableLiveData<String>("")
    val debtValue = MutableLiveData<String>("")
    val debtDescription = MutableLiveData<String>("")
    val debtImageURL = MutableLiveData<String>("")
    val debtPayer = MutableLiveData<String>("")
    val category = MutableLiveData<String>("")

    private val _nameError = MutableLiveData<String>("")
    val nameError: LiveData<String> get() = _nameError

    private val _valueError = MutableLiveData<String>("")
    val valueError: LiveData<String> get() = _valueError

    private val _payerError = MutableLiveData<String>("")
    val payerError: LiveData<String> get() = _payerError

    private val _categoryError = MutableLiveData<String>("")
    val categoryError: LiveData<String> get() = _categoryError


    var debtProfilePhoto = MutableLiveData<ByteArray>()

    // Events
    sealed class Event {
        object NavigateBack : Event()
        object SaveDebt : Event()
        object HideLoading : Event()
        object Deleted: Event()
        data class SetDropDowns(val payer: String, val category: String) : Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    /**
     * Set data for add/edit debt
     *
     * @param debtId as nullable debt ID (null means new, any means edit task)
     * @param friendshipData as friendship data
     * @param friendName as name of friend in current friend detail
     */
    fun setData(debtId: String?, friendshipData: FriendshipModel, friendName: String) {
        debtID = debtId
        this.friendshipData = friendshipData
        this.friendName = friendName

        // Determine which is friend ID
        if (friendshipData.member1 == auth.currentUser.uid) {
            friendId = friendshipData.member2
        } else {
            friendId = friendshipData.member1
        }

        // Sets category names
        val categoryItems =
            listOf(localized(R.string.category_food),
                localized(R.string.category_entertainment),
                localized(R.string.categroy_finance),
                localized(R.string.category_clothing_access),
                localized(R.string.category_electronics),
                localized(R.string.category_other))
        categoryList.value = categoryItems

        val payerNames = listOf(localized(R.string.Me), friendName)
        payerList.value = payerNames

        if (debtId == null) {
            //
        } else {
            var debtDetails: DebtModel
            // Load data for editing debt
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.uid).collection(Constants.DATABASE_DEBTS).document(debtId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }
                    if (snapshot != null && snapshot.exists()) {
                        debtDetails = snapshot.toObject(DebtModel::class.java)!!
                        timestamp = debtDetails.timestamp
                        debtName.value = debtDetails.name
                        debtDescription.value = debtDetails.description
                        debtValue.value = debtDetails.value.toString()
                        if (debtDetails.img.isNotEmpty()) {
                            debtImageURL.value = debtDetails.img
                        }
                        val payerName = if (debtDetails.payer == auth.currentUser.uid) {
                            localized(R.string.Me)
                        } else {
                            friendName
                        }

                        // Get category name based on database category value
                        GlobalScope.launch {
                            eventChannel.send(Event.SetDropDowns(payerName,
                                transformDatabaseStringToCategory(debtDetails.category)))
                        }

                    } else {
                        Log.w("DATA", "Current data null")
                    }
                }
        }
    }

    /**
     * Save debt to database
     *
     * @param imageURL as img url of debt
     * @param payer as ID of payer
     * @param category as category
     */
    fun saveToDatabase(imageURL: String, payer: String, category: String) {
        if (validateFields(category, payer)) {
            val payerId: String
            if (payer == localized(R.string.Me)) {
                payerId = auth.currentUser.uid
            } else {
                payerId = friendId
            }

            // Try to save image to online storage and debt
            GlobalScope.launch(IO) {
                try {
                    var profileImageURL: Uri? = null
                    var debtRef: DocumentReference = if(debtID == null) {
                        db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.uid).collection(Constants.DATABASE_DEBTS).document()
                    } else {
                        db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.uid).collection(Constants.DATABASE_DEBTS).document(debtID.toString())
                    }

                    if (debtProfilePhoto.value != null) {
                        try {
                            storage.reference.child("/${Constants.DATABASE_DEBTS}/${debtRef.id}/${Constants.DATABASE_NAMES_DEBT_PROFILE_IMAGE}").putBytes(debtProfilePhoto.value!!).await()
                            profileImageURL = storage.reference.child("/${Constants.DATABASE_DEBTS}/${debtRef.id}/${Constants.DATABASE_NAMES_DEBT_PROFILE_IMAGE}").downloadUrl.await()
                        } catch (e: Exception) {
                            Log.d("STRG", e.message.toString())
                        }
                    }


                    val debtDetails = DebtModel()
                    debtDetails.id = debtRef.id
                    debtDetails.name = debtName.value!!
                    debtDetails.value = debtValue.value!!.toInt()
                    debtDetails.description = debtDescription.value!!
                    debtDetails.payer = payerId
                    debtDetails.category = transformCategoryToDatabaseString(category)
                    debtDetails.img = profileImageURL?.toString() ?: debtImageURL.value!!
                    debtDetails.timestamp = timestamp ?: Timestamp.now()


                    debtRef.set(debtDetails).await()

                    eventChannel.send(Event.NavigateBack)

                } catch (e: Exception) {
                    Log.d("DTBS", e.message.toString())
                }

            }
        } else {
            GlobalScope.launch { eventChannel.send(Event.HideLoading) }
        }
    }

    /**
     * Validate debt fields
     *
     * @param category as category value
     * @param payer as payer ID
     * @return
     */
    private fun validateFields(category: String, payer: String): Boolean {
        val validateName = validateName()
        val validateValue = validateValue()
        val validateCategory = validateCategory(category)
        val validatePayer = validatePayer(payer)

        return validateName && validateValue && validateCategory && validatePayer
    }

    /**
     * Validate debt name
     *
     * @return true if valid
     */
    private fun validateName(): Boolean {
        return if (debtName.value!!.isEmpty()) {
            _nameError.value = localized(R.string.add_edit_debt_enter_debt_name)
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    /**
     * Validate debt value
     *
     * @return true if valid
     */
    private fun validateValue(): Boolean {
        return when {
            debtValue.value!!.isNullOrEmpty() -> {
                _valueError.value = localized(R.string.add_edit_debt_enter_debt_value)
                false
            }
            debtValue.value!!.toInt() > 0 -> {
                _valueError.value = ""
                true
            }
            else -> {
                _valueError.value = localized(R.string.add_edit_debt_value_must_be_grater_than_0)
                false
            }
        }
    }

    /**
     * Validate debt category
     *
     * @param category
     * @return true if valid
     */
    private fun validateCategory(category: String): Boolean {
        return if (category.isNullOrEmpty()) {
            _categoryError.value = localized(R.string.add_edit_debt_please_choose_category)
            false
        } else {
            _categoryError.value = ""
            true
        }
    }

    /**
     * Validate debt payer
     *
     * @param payer
     * @return true if valid
     */
    private fun validatePayer(payer: String): Boolean {
        return if (payer.isNullOrEmpty()) {
            _payerError.value = "Zvolte prosím plátce"
            false
        } else {
            _payerError.value = ""
            true
        }
    }

    /**
     * Delete debt
     *
     */
    fun deleteDebt() {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_FRIENDSHIPS)
                .document(friendshipData.uid)
                .collection(Constants.DATABASE_DEBTS)
                .document(debtID!!)
                .delete().await()
            eventChannel.send(Event.Deleted)
        }
    }

    public override fun onCleared() {
        super.onCleared()
    }



}