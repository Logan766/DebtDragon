package tech.janhoracek.debtdragon.dashboard.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.friend_item_v2.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.Constants
import kotlin.math.abs

/**
 * Top creditors adapter
 *
 * @property topCreditors
 * @constructor Create empty Top creditors adapter
 */
class TopCreditorsAdapter(var topCreditors: List<Pair<String, Int>>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    class TopCreditorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val friendName = itemView.tv_FriendItem2_Name
        val friendImage = itemView.CircImageView_FriendItem2
        val debtorSum = itemView.tv_FriendItem2_Sum
        val currency = itemView.tv_currency_FriendItem2

        /**
         * Bind visible info of creditor
         *
         * @param name as creditor name
         * @param image as URL of creditor
         * @param sum as net sum of credit
         */
        fun bindVisibleInfo(name: String, image: String, sum: Int) {
            friendName.text = name
            debtorSum.text = abs(sum).toString()
            debtorSum.setTextColor(itemView.resources.getColor(R.color.second))
            currency.setTextColor(itemView.resources.getColor(R.color.second))
            if (image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(friendImage)
            } else {
                Glide.with(itemView).load(image).into(friendImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_v2, parent, false)
        return TopCreditorViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var id = topCreditors[position].first
        var name = ""
        var image = ""
        var sum = topCreditors[position].second

        //Gets data about creditor
        GlobalScope.launch(Dispatchers.IO) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    (holder as TopCreditorViewHolder).bindVisibleInfo(name, image, sum)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
        //adds animation to ViewHolder
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }

    override fun getItemCount(): Int {
        return topCreditors.size
    }

}