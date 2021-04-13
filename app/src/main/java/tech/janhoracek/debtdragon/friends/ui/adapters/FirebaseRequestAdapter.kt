package tech.janhoracek.debtdragon.friends.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.request_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.RequestModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.Constants

private const val REQUEST_TYPE_SENT: Int = 0
private const val REQUEST_TYPE_RECIEVED: Int = 1

/**
 * Firebase request adapter for friends requests
 *
 * @constructor
 *
 * @param options as Firestore Recycler Options for Request model
 */
class FirebaseRequestAdapter(options: FirestoreRecyclerOptions<RequestModel>): FirestoreRecyclerAdapter<RequestModel, FirebaseRequestAdapter.RequestAdapterViewHolder>(options) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Request adapter view holder
     *
     * @constructor
     *
     * @param itemView
     */
    class RequestAdapterViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        val view = itemView

        var userName = itemView.tv_RequestItem_name
        var userImage = itemView.ImageView_request_avatar
        var messageText = itemView.tv_requestItem_message
        var acceptButton = itemView.btn_requestItem_accept
        var declineButton = itemView.btn_requestItem_decline
        var imageView = itemView.ImageView_request_avatar

        /**
         * Bind data to sent requests
         *
         * @param name as sender full name
         * @param imgUrl as sender image url
         * @param id as sender ID
         */
        fun bindToSent(name: String, imgUrl: String, id: String){
            userName.text = name
            messageText.text = localized(R.string.your_friend_request_was_sent)
            acceptButton.visibility = View.INVISIBLE
            acceptButton.isEnabled = false
            if (imgUrl == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(imageView)
            } else {
                Glide.with(itemView).load(imgUrl).into(imageView)
            }
            declineButton.setOnClickListener {
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_REQUESTS).document(id).delete()
                db.collection(Constants.DATABASE_USERS).document(id).collection(Constants.DATABASE_REQUESTS).document(auth.currentUser.uid).delete()
            }
        }

        /**
         * Binds data to recieved requests
         *
         * @param name as reciever full name
         * @param imgUrl as reciever image url
         * @param id as reciever ID
         */
        fun bindToRecieved(name: String, imgUrl: String, id: String) {
            userName.text = name
            messageText.text = localized(R.string.is_sending_you_friend_request)
            if (imgUrl == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(imageView)
            } else {
                Glide.with(itemView).load(imgUrl).into(imageView)
            }
            // Sets up decline button functionality
            declineButton.setOnClickListener {
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_REQUESTS).document(id).delete()
                db.collection(Constants.DATABASE_USERS).document(id).collection(Constants.DATABASE_REQUESTS).document(auth.currentUser.uid).delete()
            }
            // Sets up accept button functionality
            acceptButton.setOnClickListener {
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_REQUESTS).document(id).delete()
                db.collection(Constants.DATABASE_USERS).document(id).collection(Constants.DATABASE_REQUESTS).document(auth.currentUser.uid).delete()

                val friendshipID = db.collection(Constants.DATABASE_FRIENDSHIPS).document()
                val friendship: MutableMap<String, Any> = HashMap()
                val members = ArrayList<String>()

                // Creates friendship requests
                members.add(id)
                members.add(auth.currentUser.uid)
                friendship["uid"] = friendshipID.id
                friendship["member1"] = auth.currentUser.uid
                friendship["member2"] = id
                friendship["members"] = members
                friendshipID.set(friendship)
                db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS).document(id).set(friendship)
                db.collection(Constants.DATABASE_USERS).document(id).collection(Constants.DATABASE_FRIENDSHIPS).document(auth.currentUser.uid).set(friendship)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return RequestAdapterViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val type = snapshots.getSnapshot(position).get("type")
        return if(type == "sent") {
            REQUEST_TYPE_SENT
        } else {
            REQUEST_TYPE_RECIEVED
        }
    }

    override fun onBindViewHolder(
        holder: RequestAdapterViewHolder,
        position: Int,
        model: RequestModel
    ) {
        val id = snapshots.getSnapshot(position).id
        var name = ""
        var image = ""

        // Gets data for friendship request
        db.collection(Constants.DATABASE_USERS).document(id).addSnapshotListener{
            snapshot, e ->
            if (e != null) {
                Log.w("LSTNR", "Listening failed: " + e)
            }

            if (snapshot != null && snapshot.exists()) {
                name = snapshot.data?.get("name").toString()
                image = snapshot.data?.get("url").toString()
                if(getItemViewType(position) == REQUEST_TYPE_SENT) {
                    holder.bindToSent(name, image, id)
                } else {
                    holder.bindToRecieved(name, image, id)
                }
            } else {
                Log.w("DATA", "Current data null")
            }
        }
        // Adds animation
        holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }
}