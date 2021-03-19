package tech.janhoracek.debtdragon.friends.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class FriendsOverviewViewModel : BaseViewModel() {

    private val _notificationCount = MutableLiveData<Int>(0)
    val notificationCount: LiveData<Int> get() = _notificationCount

    init {
        db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid)
            .collection(Constants.DATABASE_REQUESTS).addSnapshotListener { snaphot, error ->
            if (error != null) {
                Log.w("LSTNR", error.message.toString())
            }

            if (snaphot != null) {
                _notificationCount.value = snaphot.size()
            } else {
                _notificationCount.value = 0
            }
        }


    }
}