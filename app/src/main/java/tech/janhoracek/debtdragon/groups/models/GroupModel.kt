package tech.janhoracek.debtdragon.groups.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Group model
 *
 * @property id as group ID
 * @property name as group name
 * @property description as group description
 * @property owner as group owner
 * @property members as array of memebers
 * @property photoUrl as img url of group
 * @property calculated as status of group
 * @constructor Create empty Group model
 */
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