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

class GroupsFirebaseAdapter(options: FirestoreRecyclerOptions<GroupModel>, val mGroupListener: OnGroupClickListener) :
    FirestoreRecyclerAdapter<GroupModel, GroupsFirebaseAdapter.GroupAdapterViewHolder>(options){
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    inner class GroupAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindToVH(group: GroupModel) {
            itemView.tv_GroupGeneral_Name.text = group.name
            if(group.photoUrl.isNotEmpty()) {
                Glide.with(itemView).load(group.photoUrl).into(itemView.CircImageView_GroupGeneral)
            } else {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_GroupGeneral)
            }
            if(group.owner == auth.currentUser.uid) {
                itemView.ImageView_isAdmin_group_item_general.visibility = View.VISIBLE
            } else {
                itemView.ImageView_isAdmin_group_item_general.visibility = View.GONE
            }

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
        //holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }

    interface OnGroupClickListener {
        fun onGroupClick(groupID: String)
    }

}