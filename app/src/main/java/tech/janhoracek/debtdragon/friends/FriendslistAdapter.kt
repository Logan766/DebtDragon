package tech.janhoracek.debtdragon.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.friend_item.view.*
import tech.janhoracek.debtdragon.R

class FriendslistAdapter(var friendsListItems: List<FriendModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class FriendViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun binduj(friendModel: FriendModel) {
            itemView.tv_FriendItem_friendMail.text = friendModel.email
            itemView.tv_friendItem_friendName.text = friendModel.name
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.friend_item, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as FriendViewHolder).binduj(friendsListItems[position])
    }

    override fun getItemCount(): Int {
        return friendsListItems.size
    }


}