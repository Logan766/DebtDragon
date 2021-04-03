package tech.janhoracek.debtdragon.groups.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.dashboard.ui.adapters.TopDebtorsAdapter
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class GroupDetailViewModel: BaseViewModel() {

    val isCurrentUserOwner = MutableLiveData<Boolean>()

    val groupModel = MutableLiveData<GroupModel>()

    val friendsToAdd = MutableLiveData<List<String>>()

    private val _membersAndNames = MutableLiveData<List<Pair<String, String>>>()
    val membersAndNames: LiveData<List<Pair<String, String>>> get() = _membersAndNames

    private val _membersNames = MutableLiveData<List<String>>()
    val membersNames: LiveData<List<String>> get() = _membersNames

    private val _payerProfileImg = MutableLiveData<String>()
    val payerProfileImg: LiveData<String> get() = _payerProfileImg

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
            db.collection(Constants.DATABASE_USERS).document(payerID).addSnapshotListener { value, error ->
                val url = value!![Constants.DATABASE_USER_IMG_URL].toString()
                if(url == "null") {
                    _payerProfileImg.postValue("")
                } else{
                    _payerProfileImg.postValue(url)
                }
                Log.d("KOFILA", "URL je: " + url)

            }
        }
    }


}