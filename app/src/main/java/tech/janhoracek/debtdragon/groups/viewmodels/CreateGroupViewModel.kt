package tech.janhoracek.debtdragon.groups.viewmodels

import androidx.lifecycle.MutableLiveData
import tech.janhoracek.debtdragon.utility.BaseViewModel

class CreateGroupViewModel: BaseViewModel() {

    val groupImageURL = MutableLiveData<String>("")

}