package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debt_friend_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Firebase group debt adapter
 *
 * @property mGroupDebtListener as group click interface
 * @constructor
 *
 * @param options as firestore recycler options for Group Debt Model
 */
class FirebaseGroupDebtAdapter(options: FirestoreRecyclerOptions<GroupDebtModel>, val mGroupDebtListener: onGroupDebtClickListener) :
    FirestoreRecyclerAdapter<GroupDebtModel, FirebaseGroupDebtAdapter.GroupDebtViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Group debt view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class GroupDebtViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindToVH(model: GroupDebtModel, debtorName: String, debtorImage: String) {
            itemView.tv_value_debtFriendItem.text = model.value
            itemView.tv_payer_debtFriendItem.text = debtorName
            itemView.tv_name_debtFriendItem.text = model.name

            // Load image
            if(debtorImage == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.image_View_DebtFriendDetail)
            } else {
                Glide.with(itemView).load(debtorImage).into(itemView.image_View_DebtFriendDetail)
            }

            // Add on click listener
            itemView.setOnClickListener {
                mGroupDebtListener.onGroupDebtClick(model.id)
            }

            // Add ripple effect
            itemView.addRipple()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupDebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.debt_friend_item, parent, false)
        return GroupDebtViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GroupDebtViewHolder,
        position: Int,
        model: GroupDebtModel
    ) {
        // Gets data for group debt
        db.collection(Constants.DATABASE_USERS).document(model.debtor).addSnapshotListener{snapshot, error ->
            if (error != null) {
                Log.w("LSTNR", "Listening failed: " + error)
            }

            if (snapshot != null && snapshot.exists()) {
                val debtorName = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                val debtorImage = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                holder.bindToVH(model, debtorName, debtorImage)
            } else {
                Log.w("LSTNR", "Current data null")
            }
        }
    }

    /**
     * On group debt click listener interface
     *
     * @constructor Create empty On group debt click listener
     */
    interface onGroupDebtClickListener {
        fun onGroupDebtClick(groupDebtID: String)
    }

    /**
     * Adds ripple effect to view
     *
     */
    private fun View.addRipple() = with(TypedValue()) {
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
        setBackgroundResource(resourceId)
    }
}