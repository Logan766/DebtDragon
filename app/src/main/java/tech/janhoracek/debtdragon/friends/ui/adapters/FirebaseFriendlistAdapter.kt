package tech.janhoracek.debtdragon.friends.ui.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.compose.animation.core.snap
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.friend_item_v2.view.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.friends.models.RequestModel
import tech.janhoracek.debtdragon.friends.ui.FriendsOverViewFragmentDirections
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.Constants
import kotlin.math.abs

/**
 * Firebase friendlist adapter
 *
 * @constructor
 *
 * @param options as FirestoreRecyclerOptions for FriendModel
 */
class FirebaseFriendlistAdapter(options: FirestoreRecyclerOptions<FriendModel>) :
    FirestoreRecyclerAdapter<FriendModel, FirebaseFriendlistAdapter.FriendAdapterViewHolder>(options) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Friend adapter view holder
     *
     * @constructor
     *
     * @param itemView
     */
    class FriendAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName = itemView.tv_FriendItem2_Name
        val friendImage = itemView.CircImageView_FriendItem2
        val debtSum = itemView.tv_FriendItem2_Sum
        val currency = itemView.tv_currency_FriendItem2
        val view = itemView

        /**
         * Bind visible info of friend
         *
         * @param name as friend full name
         * @param image as friend image
         */
        fun bindVisibleInfo(name: String, image: String) {
            friendName.text = name
            if (image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(friendImage)
            } else {
                Glide.with(itemView).load(image).into(friendImage)
            }
        }

        /**
         * Sets on clikc listener of friend item
         *
         * @param friendshipID as friendship ID
         */
        fun bindOnClick(friendshipID: String) {
            itemView.setOnClickListener {
                val action = FriendsOverViewFragmentDirections.actionFriendsOverViewFragmentToFriendDetailFragment2(friendshipID)
                itemView.findNavController().navigate(action)
            }
        }

        /**
         * Binds summary and sets color based on value of it
         *
         * @param sum as friendship debt summary
         */
        fun bindSummary(sum: Int) {
            when {
                sum == 0 -> {
                    // Sets blue color
                    currency.setTextColor(Color.parseColor("#120f38"))
                    debtSum.setTextColor(Color.parseColor("#120f38"))
                    debtSum.text = sum.toString()
                }
                sum > 0 -> {
                    // Sets blue color
                    currency.setTextColor(Color.parseColor("#120f38"))
                    debtSum.setTextColor(Color.parseColor("#120f38"))
                    debtSum.text = sum.toString()
                }
                else -> {
                    // Sets red color
                    currency.setTextColor(Color.parseColor("#ee1f43"))
                    debtSum.setTextColor(Color.parseColor("#ee1f43"))
                    debtSum.text = abs(sum).toString()
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_v2, parent, false)
        return FriendAdapterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FriendAdapterViewHolder,
        position: Int,
        model: FriendModel
    ) {
        val id = snapshots.getSnapshot(position).id
        var name = ""
        var image = ""
        var debtSum = 0
        var friendshipID = ""

        // Gets data for friendship from database
        GlobalScope.launch(Main) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    holder.bindVisibleInfo(name, image)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }

            val friendship = db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).document(id).get().await()
            friendshipID = friendship[Constants.DATABASE_USER_UID].toString()
            holder.bindOnClick(friendshipID)

            // Gets data for friendship sum
            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        snapshot.forEach { document ->
                            if (document[Constants.DATABASE_DEBT_PAYER] == auth.currentUser.uid) {
                                debtSum += (document[Constants.DATABASE_DEBTS_VALUE]).toString().toInt()
                            } else {
                                debtSum -= (document[Constants.DATABASE_DEBTS_VALUE]).toString().toInt()
                            }
                        }
                        holder.bindSummary(debtSum)
                    } else {
                        Log.w("DATA", "Current data null")
                    }
                }
        }




        // Sets animation
        holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }
}