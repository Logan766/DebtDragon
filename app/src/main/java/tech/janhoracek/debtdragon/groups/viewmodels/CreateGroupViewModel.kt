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
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants
import java.lang.Exception

/**
 * Create group view model
 *
 * @constructor Create empty Create group view model
 */
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

    /**
     * Set data for create/edit group fragment
     *
     * @param groupData as Group Model
     */
    fun setData(groupData: GroupModel?) {
        if(groupData != null) {
            newGroup = false
            groupModel.value = groupData!!
            _buttonTitle.value = localized(R.string.create_edit_group_fragment_save_changes)
            _toolbarTitle.value = localized(R.string.create_edit_group_fragment_edit_group)
        } else {
            groupModel.value = GroupModel()
            newGroup = true
            _buttonTitle.value = localized(R.string.create_edit_group_fragment_create_group)
            _toolbarTitle.value = localized(R.string.create_edit_group_fragment_create_new_group)
        }
    }

    /**
     * Save group to firestore
     *
     */
    fun saveGroup() {
        GlobalScope.launch(IO) {
            eventChannel.send(Event.ShowLoading)
            var groupImageURL: Uri? = null
            var groupRef: DocumentReference
            val groupData = GroupModel()

            // Determine if new group is being created
            if(newGroup) {
                groupRef = db.collection(Constants.DATABASE_GROUPS).document()
                groupData.id = groupRef.id
                val members = arrayListOf<String>()
                members.add(auth.currentUser.uid)
                groupData.members = members
            } else {
                groupRef = db.collection(Constants.DATABASE_GROUPS).document(groupModel.value!!.id)
                groupData.id = groupModel.value!!.id
                groupData.members = groupModel.value!!.members
            }

            // Check if new profile photo was taken
            if(groupProfilePhoto.value != null) {
                try {
                    storage.reference.child("/${Constants.DATABASE_GROUPS}/${groupData.id}/${Constants.DATABASE_NAMES_GROUP_PROFILE_IMAGE}").putBytes(groupProfilePhoto.value!!).await()
                    groupImageURL = storage.reference.child("/${Constants.DATABASE_GROUPS}/${groupData.id}/${Constants.DATABASE_NAMES_GROUP_PROFILE_IMAGE}").downloadUrl.await()
                } catch (e: Exception) {
                    Log.d("STRG", e.message.toString())
                }
            }

            // Set up group data
            groupData.name = groupModel.value!!.name
            groupData.description = groupModel.value!!.description
            groupData.owner = auth.currentUser.uid
            groupData.photoUrl = groupImageURL?.toString() ?: groupModel.value!!.photoUrl

            groupRef.set(groupData).await()

            eventChannel.send(Event.HideLoading)
            eventChannel.send(Event.GroupCreated(groupData.id))
        }


    }

}