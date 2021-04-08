package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.friend_item_v2.view.*
import kotlinx.android.synthetic.main.member_item.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.Constants

class MembersAdapter(var members: List<String>, var owner: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    class GroupMemberViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val memberName = itemView.tv_member_item_name
        val memberImage = itemView.CircImageView_member_item
        val administrator = itemView.ImageView_administrator_member_item

        fun bindToVH(name: String, image: String, owner: Boolean) {
            Log.d("PICA", "Binduju: " + name)
            memberName.text = name

            if(owner) {
                administrator.visibility = View.VISIBLE

            } else {
                administrator.visibility = View.GONE
            }

            if(image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(memberImage)
            } else {
                Glide.with(itemView).load(image).into(memberImage)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.member_item, parent, false)
        return GroupMemberViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int) {

        var id = members[position]
        var name =""
        var image = ""
        var isOwnerOfGroup = false

        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener {snapshot, error ->
                if(error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    if(snapshot.id == owner) isOwnerOfGroup = true
                    if(snapshot.id == auth.currentUser.uid) name = "Vy"
                    (holder as GroupMemberViewHolder).bindToVH(name, image, isOwnerOfGroup)
                } else {
                    Log.w("DATA", "Current data null")
                }
                Log.d("KURVA", "SIZE JEST: " + snapshot!!.data!!.size)
            }
        }

    }

    override fun getItemCount(): Int {
        return members.size
    }
}