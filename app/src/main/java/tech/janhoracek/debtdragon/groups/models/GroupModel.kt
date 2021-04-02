package tech.janhoracek.debtdragon.groups.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupModel(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var owner: String = "",
    var members: List<String> = emptyList(),
    var photoUrl: String = "",
    var calculated: String = ""
) : Parcelable