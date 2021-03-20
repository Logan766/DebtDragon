package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import tech.janhoracek.debtdragon.utility.BaseViewModel


class FriendDetailViewModel : BaseViewModel() {


    val userId = MutableLiveData<String>("Nazdar")


    fun setArguments(friendshipID: String){
        userId.value = friendshipID
    }

}