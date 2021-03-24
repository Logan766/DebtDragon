package tech.janhoracek.debtdragon.friends.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.transformCategoryToDatabaseString
import java.lang.Exception

class AddEditDebtViewModel : BaseViewModel() {

    private lateinit var friendshipData: FriendshipModel
    private lateinit var friendName: String
    private lateinit var friendId: String

    private lateinit var debtData: DebtModel

    val categoryList = MutableLiveData<List<String>>()
    val payerList = MutableLiveData<List<String>>()

    val debtId = MutableLiveData<String>("")
    val debtName = MutableLiveData<String>("")
    val debtValue = MutableLiveData<String>("")
    val debtDescription = MutableLiveData<String>("")
    val debtImage = MutableLiveData<String>("")
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

    sealed class Event {
        object NavigateBack : Event()
        object SaveDebt : Event()
        object HideLoading: Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()


    fun setData(debtId: String?, friendshipData: FriendshipModel, friendName: String) {
        this.friendshipData = friendshipData
        this.friendName = friendName
        if (friendshipData.member1 == auth.currentUser.uid) {
            friendId = friendshipData.member2
        } else {
            friendId = friendshipData.member1
        }
        val categoryItems =
            listOf(localized(R.string.category_food), localized(R.string.category_entertainment), localized(R.string.categroy_finance), localized(
                R.string.category_clothing_access), localized(R.string.category_electronics), localized(R.string.category_other))
        categoryList.value = categoryItems

        val payerNames = listOf("Já", friendName)
        payerList.value = payerNames

        if (debtId == null) {
            Log.d("VALECEK", "Je to novej task")
        } else {

            //test.value = "STARY"
        }
    }

    fun saveToDatabase(imageURL: String, payer: String, category: String) {
        if (validateFields(category, payer)) {
            var payerId: String
            if (payer == "Já") {
                payerId = auth.currentUser.uid
            } else {
                payerId = friendId
            }

            Log.d("RANO", "ID jest: ")
            Log.d("RANO", "ImgUrl jest: ")
            Log.d("RANO", "Nazev jest: " + debtName.value)
            Log.d("RANO", "Castka jest: " + debtValue.value)
            Log.d("RANO", "Platce jest: " + payerId)
            Log.d("RANO", "Kategorie jest: " + transformCategoryToDatabaseString(category))
            Log.d("RANO", "Popis jest: " + debtDescription.value)

            GlobalScope.launch(IO) {

                try {
                    var profileImageURL: Uri? = null
                    val debtRef = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipData.uid).collection(Constants.DATABASE_DEBTS).document()
                    if(debtProfilePhoto.value != null) {
                        try {
                            storage.reference.child("/${Constants.DATABASE_DEBTS}/${debtRef.id}/${Constants.DATABASE_NAMES_DEBT_PROFILE_IMAGE}").putBytes(debtProfilePhoto.value!!).await()
                            profileImageURL = storage.reference.child("/${Constants.DATABASE_DEBTS}/${debtRef.id}/${Constants.DATABASE_NAMES_DEBT_PROFILE_IMAGE}").downloadUrl.await()
                        } catch (e: Exception) {
                            Log.d("STRG", e.message.toString())
                        }

                    }
                    val debtDetails = DebtModel(debtRef.id, debtValue.value!!.toInt(), debtName.value!!, debtDescription.value!!, profileImageURL?.toString() ?: "", payerId, transformCategoryToDatabaseString(category), Timestamp.now())
                    debtRef.set(debtDetails).await()
                    eventChannel.send(Event.NavigateBack)

                } catch (e: Exception) {
                    Log.d("DTBS", e.message.toString() )
                }

            }
        } else {
            GlobalScope.launch { eventChannel.send(Event.HideLoading) }
        }
    }

    private fun validateFields(category: String, payer: String): Boolean {
        val validateName = validateName()
        val validateValue = validateValue()
        val validateCategory = validateCategory(category)
        val validatePayer = validatePayer(payer)

        return validateName && validateValue && validateCategory && validatePayer
    }

    private fun validateName(): Boolean {
        return if (debtName.value!!.isEmpty()) {
            _nameError.value = "Zadejte název dluhu"
            false
        } else {
            _nameError.value = ""
            true
        }
    }

    private fun validateValue(): Boolean {
        return when {
            debtValue.value!!.isNullOrEmpty() -> {
                _valueError.value = "Zadejte částku"
                false
            }
            debtValue.value!!.toInt() > 0 -> {
                _valueError.value = ""
                true
            }
            else -> {
                _valueError.value = "Zadejte částku vyšší než 0"
                false
            }
        }
    }

    private fun validateCategory(category: String): Boolean {
        return if (category.isNullOrEmpty()) {
            _categoryError.value = "Zvolte prosím kategorii"
            false
        } else {
            _categoryError.value = ""
            true
        }
    }

    private fun validatePayer(payer: String): Boolean {
        return if (payer.isNullOrEmpty()) {
            _payerError.value = "Zvolte prosím plátce"
            false
        } else {
            _payerError.value = ""
            true
        }
    }

    public override fun onCleared() {
        super.onCleared()
       /* debtId = MutableLiveData<String>("")
        debtName = MutableLiveData<String>("")
        debtValue = MutableLiveData<String>("")
        debtDescription = MutableLiveData<String>("")
        debtPayer = MutableLiveData<String>("")
        category = MutableLiveData<String>("")*/
        Log.d("RANO", "Mazu data")
    }


}