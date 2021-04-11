package tech.janhoracek.debtdragon.dashboard.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.friend_item_v2.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Top debtors adapter
 *
 * @property topDebtors
 * @constructor Create empty Top debtors adapter
 */
class TopDebtorsAdapter(var topDebtors: List<Pair<String, Int>>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    class TopDebtorViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val friendName = itemView.tv_FriendItem2_Name
        val friendImage = itemView.CircImageView_FriendItem2
        val debtorSum = itemView.tv_FriendItem2_Sum

        /**
         * Bind visible info
         *
         * @param name as debtor name
         * @param image as URL of debtor image
         * @param sum as net debt sum
         */
        fun bindVisibleInfo(name: String, image: String, sum: Int) {
            friendName.text = name
            debtorSum.text = sum.toString()
            if (image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(friendImage)
            } else {
                Glide.with(itemView).load(image).into(friendImage)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item_v2, parent, false)
        return TopDebtorViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var id = topDebtors[position].first
        var name = ""
        var image = ""
        var sum = topDebtors[position].second

        //gets data about debtor
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    (holder as TopDebtorViewHolder).bindVisibleInfo(name, image, sum)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return topDebtors.size
    }
}