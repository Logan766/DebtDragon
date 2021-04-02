package tech.janhoracek.debtdragon.groups.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.dashboard.ui.adapters.TopDebtorsAdapter
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.utility.BaseViewModel
import tech.janhoracek.debtdragon.utility.Constants

class GroupDetailViewModel: BaseViewModel() {

    val groupModel = MutableLiveData<GroupModel>()

    fun setData(groupID: String) {
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_GROUPS).document(groupID).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    groupModel.value = snapshot.toObject(GroupModel::class.java)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
    }


}