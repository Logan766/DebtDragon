package tech.janhoracek.debtdragon.groups.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.group_item_general.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Groups firebase adapter
 *
 * @property mGroupListener as interface for group click
 * @constructor
 *
 * @param options as firestore recycler options for Group Model
 */
class GroupsFirebaseAdapter(options: FirestoreRecyclerOptions<GroupModel>, val mGroupListener: OnGroupClickListener) :
    FirestoreRecyclerAdapter<GroupModel, GroupsFirebaseAdapter.GroupAdapterViewHolder>(options){
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Group adapter view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class GroupAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        /**
         * Bind to view holder
         *
         * @param group as Group Model
         */
        fun bindToVH(group: GroupModel) {
            itemView.tv_GroupGeneral_Name.text = group.name

            // Load image
            if(group.photoUrl.isNotEmpty()) {
                Glide.with(itemView).load(group.photoUrl).into(itemView.CircImageView_GroupGeneral)
            } else {
                Glide.with(itemView).load(R.drawable.avatar_groupavatar).into(itemView.CircImageView_GroupGeneral)
            }

            // Check if current user is owner
            if(group.owner == auth.currentUser.uid) {
                itemView.ImageView_isAdmin_group_item_general.visibility = View.VISIBLE
            } else {
                itemView.ImageView_isAdmin_group_item_general.visibility = View.GONE
            }

            // Check status of group and show icons respectively
            when (group.calculated) {
                Constants.DATABASE_GROUPS_STATUS_LOCKED -> {
                    itemView.ImageView_isLocked_group_item_general.visibility = View.VISIBLE
                    itemView.ImageView_isCalculated_group_item_general.visibility = View.GONE
                }
                Constants.DATABASE_GROUPS_STATUS_CALCULATED -> {
                    itemView.ImageView_isLocked_group_item_general.visibility = View.VISIBLE
                    itemView.ImageView_isCalculated_group_item_general.visibility = View.VISIBLE
                }
                else -> {
                    itemView.ImageView_isLocked_group_item_general.visibility = View.GONE
                    itemView.ImageView_isCalculated_group_item_general.visibility = View.GONE
                }
            }

            // Setup on click listener
            itemView.setOnClickListener {
                mGroupListener.onGroupClick(group.id)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.group_item_general, parent, false)
        return GroupAdapterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GroupAdapterViewHolder,
        position: Int,
        model: GroupModel) {

        holder.bindToVH(model)
    }


    /**
     * On group click listener interface
     *
     * @constructor Create empty On group click listener
     */
    interface OnGroupClickListener {
        fun onGroupClick(groupID: String)
    }

}