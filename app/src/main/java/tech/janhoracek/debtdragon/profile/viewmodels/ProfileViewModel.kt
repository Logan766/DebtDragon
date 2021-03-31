package tech.janhoracek.debtdragon.profile.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants


class ProfileViewModel : BaseViewModel() {

    private val _userName = MutableLiveData<String>("Loading")
    val userName: LiveData<String> get() = _userName

    private val _userImage = MutableLiveData<String>()
    val userImage: LiveData<String> get() = _userImage

    private val _logOutStatus = MutableLiveData<Boolean>(false)
    val logOutStatus: LiveData<Boolean> get() = _logOutStatus

    private val _authProviderIsPassword = MutableLiveData<Boolean>(false)
    val authProviderIsPassword: LiveData<Boolean> get() = _authProviderIsPassword

    private var docRef: ListenerRegistration

    init {
        _authProviderIsPassword.value = auth.currentUser.providerData[auth.currentUser.providerData.size - 1].providerId == "password"

        docRef = db.collection("Users").document(auth.currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("OHEN", "Listening failed: " + e)
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.w("OHEN", "Current data: ${snapshot.data}")
                    _userName.value = snapshot.data?.get("name").toString()
                } else {
                    Log.w("OHEN", "Current data null")
                }


                viewModelScope.launch(IO) {
                    var url: Uri? = null
                    try {
                        url =
                            storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                        Log.d("OHEN", "URL obrazku je: " + url.toString())
                        _userImage.postValue(url.toString())
                    } catch (e: StorageException) {
                        Log.d("OHEN", "Nemame obrazek")
                    }
                    Log.d("OHEN", "Posilam do userImage: " + url.toString().isBlank())
                    //_userImage.postValue(url.toString())
                }

                /*val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser.providerData.size > 0) {
                    //Prints Out google.com for Google Sign In, prints facebook.com for Facebook
                    Log.d("PERMONIK", "Provider: " + firebaseUser.providerData[firebaseUser.providerData.size - 1].providerId)
                }*/



                //Log.d("PERMONIK", "Auth provider jest: " + auth.currentUser.providerData[auth.currentUser.providerData.size - 1].providerId)

            }


    }

    fun clickLogout() {
        //onCleared()
        auth.signOut()
        _logOutStatus.value = true
    }

    public override fun onCleared() {
        Log.d("PIRAT", "JSEM ZNICENEJ!")
        //docRef.remove()
        super.onCleared()
    }

    fun saveProfileImage(image: ByteArray) {
        Log.d("DOMBY", "Zkusim nahrat obrzek")
        viewModelScope.launch(IO) {
            try {
                storage.reference.child("/images/${auth.currentUser.uid}/profile.jpg").putBytes(image).await()
                Log.d("DOMBY", "Po nahrani")
                val url = storage.reference.child("images/" + auth.currentUser.uid + "/profile.jpg").downloadUrl.await()
                Log.d("DOMBY", "Stahuju URL ktery je: " + url)
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).update("url", url.toString())
                Log.d("DOMBY", "Update URL")
            } catch (e: Exception) {
                Log.d("STRG", "Picture upload error: " + e.message.toString())
            }
        }
    }

    fun isIbanValid(iban: String): Boolean {
        if (!"^[0-9A-Z]*\$".toRegex().matches(iban)) {
            return false
        }

        val symbols = iban.trim { it <= ' ' }
        if (symbols.length < 15 || symbols.length > 34) {
            return false
        }
        val swapped = symbols.substring(4) + symbols.substring(0, 4)
        return swapped.toCharArray()
            .map { it.toInt() }
            .fold(0) { previousMod: Int, _char: Int ->
                val value = Integer.parseInt(Character.toString(_char.toChar()), 36)
                val factor = if (value < 10) 10 else 100
                (factor * previousMod + value) % 97
            } == 1
    }

}

