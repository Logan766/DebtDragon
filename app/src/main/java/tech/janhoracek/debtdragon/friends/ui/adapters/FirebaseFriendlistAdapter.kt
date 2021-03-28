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

class FirebaseFriendlistAdapter(options: FirestoreRecyclerOptions<FriendModel>) :
    FirestoreRecyclerAdapter<FriendModel, FirebaseFriendlistAdapter.FriendAdapterViewHolder>(options) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    class FriendAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName = itemView.tv_FriendItem2_Name
        val friendImage = itemView.CircImageView_FriendItem2
        val debtSum = itemView.tv_FriendItem2_Sum
        val view = itemView

        fun bindVisibleInfo(name: String, image: String) {
            friendName.text = name
            Log.d("AJDY", "jmeno pritele: " + name)
            //debtSum.text = sum.toString()
            //Log.d("AJDY", "suma jest pritele: " + sum)
            Log.d("AJDY", "img url pritele: " + image)
            if (image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(friendImage)
            } else {
                Glide.with(itemView).load(image).into(friendImage)
            }
        }

        fun bindOnClick(friendshipID: String) {
            itemView.setOnClickListener {
                Log.d("AJDY", "ID kamosu jest: " + friendshipID)
                val action = FriendsOverViewFragmentDirections.actionFriendsOverViewFragmentToFriendDetailFragment2(friendshipID)
                itemView.findNavController().navigate(action)
            }
        }

        fun bindSummary(sum: Int) {
            if(sum == 0) {
                //blue
                debtSum.setTextColor(Color.parseColor("#120f38"))
                debtSum.text = sum.toString()
            } else if(sum > 0) {
                //blue
                debtSum.setTextColor(Color.parseColor("#120f38"))
                debtSum.text = sum.toString()
            } else {
                //red
                debtSum.setTextColor(Color.parseColor("#ee1f43"))
                debtSum.text = abs(sum).toString()
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
        var id = snapshots.getSnapshot(position).id
        Log.d("AJDY", "ID dokument jest: " + id)
        var name = ""
        var image = ""
        var debtSum = 0
        var friendshipID = ""

        GlobalScope.launch(Main) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    Log.d("AJDY", "Surovy jmeno jest: " + name)
                    Log.d("AJDY", "Surovej img jest: " + image)
                    holder.bindVisibleInfo(name, image)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }

            val friendship = db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).document(id).get().await()
            friendshipID = friendship[Constants.DATABASE_USER_UID].toString()
            holder.bindOnClick(friendshipID)


            db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null) {
                        //categorySummaryFriend.clear()
                        //categorySummaryUser.clear()
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





        holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }
}