package tech.janhoracek.debtdragon.groups.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentReference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.friends.viewmodels.AddEditDebtViewModel
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.groups.models.MembershipModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import java.lang.Exception

class CreateGroupViewModel: BaseViewModel() {

    val groupImageURL = MutableLiveData<String>("")
    val groupName = MutableLiveData<String>("")
    val groupDescription = MutableLiveData<String>("")

    val groupModel = MutableLiveData<GroupModel>()

    var groupProfilePhoto = MutableLiveData<ByteArray>()

    private val _toolbarTitle = MutableLiveData<String>()
    val toolbarTitle: LiveData<String> get() = _toolbarTitle

    private val _buttonTitle = MutableLiveData<String>()
    val buttonTitle: LiveData<String> get() = _buttonTitle

    var newGroup = true

    sealed class Event {
        object NavigateBack: Event()
        object ShowLoading: Event()
        object HideLoading: Event()
        data class GroupCreated(val groupID: String): Event()
    }

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventsFlow = eventChannel.receiveAsFlow()

    fun setData(groupData: GroupModel?) {
        if(groupData != null) {
            Log.d("SLUZ", "Uprava")
            newGroup = false
            groupModel.value = groupData!!
            _buttonTitle.value = "Uložit změny"
            _toolbarTitle.value = "Upravit skupinu"
        } else {
            Log.d("SLUZ", "Novy")
            groupModel.value = GroupModel()
            newGroup = true
            _buttonTitle.value = "Vytvořit skupinu"
            _toolbarTitle.value = "Vytvořit novou skupinu"
        }
    }

    fun saveGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            var groupImageURL: Uri? = null
            var groupRef: DocumentReference
            val groupData = GroupModel()

            if(newGroup) {
                groupRef = db.collection(Constants.DATABASE_GROUPS).document()
                groupData.id = groupRef.id
                val members = arrayListOf<String>()
                members.add(auth.currentUser.uid)
                groupData.members = members
                //val membership = MembershipModel(groupData.id, true)
                //db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_GROUPS).document(groupData.id).set(membership)
            } else {
                groupRef = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
                groupData.id = groupModel.value!!.id
                groupData.members = groupModel.value!!.members
            }

            Log.d("SLUZ", "Hodnota obrazku: " + groupProfilePhoto.value)
            if(groupProfilePhoto.value != null) {
                try {
                    storage.reference.child("/${Constants.DATABASE_GROUPS}/${groupData.id}/${Constants.DATABASE_NAMES_GROUP_PROFILE_IMAGE}").putBytes(groupProfilePhoto.value!!).await()
                    groupImageURL = storage.reference.child("/${Constants.DATABASE_GROUPS}/${groupData.id}/${Constants.DATABASE_NAMES_GROUP_PROFILE_IMAGE}").downloadUrl.await()
                    Log.d("SLUZ", "URL tady je: " + groupImageURL)
                } catch (e: Exception) {
                    Log.d("SLUZ", "Padlo to")
                    Log.d("STRG", e.message.toString())
                }
            }

            Log.d("SLUZ", "Name jest: " + groupModel.value!!.name)
            Log.d("SLUZ", "URL je: " + groupImageURL)


            groupData.name = groupModel.value!!.name
            groupData.description = groupModel.value!!.description
            groupData.owner = auth.currentUser.uid
            groupData.photoUrl = groupImageURL?.toString() ?: groupModel.value!!.photoUrl

            groupRef.set(groupData).await()

            eventChannel.send(Event.HideLoading)
            eventChannel.send(Event.GroupCreated(groupData.id))
            Log.d("SLUZ", "Ulozeno")
        }


    }

}