package tech.janhoracek.debtdragon.groups.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.group_item_general.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.GroupModel

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
                itemView.lottie_GroupGeneral_Status.visibility = View.VISIBLE
            } else {
                itemView.lottie_GroupGeneral_Status.visibility = View.INVISIBLE
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