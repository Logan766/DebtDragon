package tech.janhoracek.debtdragon.friends.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class FriendshipModel (
    val uid: String="",
    val member1: String="",
    val member2: String=""
    ) : Serializable