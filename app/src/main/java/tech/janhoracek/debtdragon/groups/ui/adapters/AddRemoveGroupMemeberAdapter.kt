package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.member_item_checkbox.view.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Add remove group memeber adapter
 *
 * @property members as array of members
 * @constructor Create empty Add remove group memeber adapter
 */
class AddRemoveGroupMemeberAdapter(var members: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var checkedFriends = ArrayList<String>()

    /**
     * Group member change view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class GroupMemberChangeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val memberName = itemView.tv_member_item_checkbox_name
        val memberImage = itemView.CircImageView_member_item_checkbox
        val checkbox = itemView.chechbox_member_item

        /**
         * Bind to viewholder
         *
         * @param name as member name
         * @param image as memeber url img
         * @param id as memeber ID
         */
        fun bindToVH(name: String, image: String, id: String) {
            memberName.text = name

            if (image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(memberImage)
            } else {
                Glide.with(itemView).load(image).into(memberImage)
            }

            checkbox.setOnCheckedChangeListener {buttonView, isChecked ->
                if(isChecked) {
                   checkedFriends.add(id)
                } else if (!isChecked) {
                   checkedFriends.remove(id)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.member_item_checkbox, parent, false)
        return GroupMemberChangeViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int) {

        var id = members[position]
        var name = ""
        var image = ""

        // Gets memeber data from firestore
        GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener { snapshot, error ->
                if(error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    name = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    (holder as GroupMemberChangeViewHolder).bindToVH(name, image, id)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

}