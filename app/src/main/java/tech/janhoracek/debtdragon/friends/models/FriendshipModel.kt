package tech.janhoracek.debtdragon.friends.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

/**
 * Friendship model
 *
 * @property uid as friendship ID
 * @property member1 as #1 ID of member in friendship
 * @property member2 as #2 ID of member in friendship
 * @constructor Create empty Friendship model
 */
data class FriendshipModel (
    val uid: String="",
    val member1: String="",
    val member2: String=""
    ) : Serializable