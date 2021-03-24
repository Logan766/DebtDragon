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

private const val REQUEST_TYPE_SENT: Int = 0
private const val REQUEST_TYPE_RECIEVED: Int = 1

class FirebaseRequestAdapter(options: FirestoreRecyclerOptions<RequestModel>): FirestoreRecyclerAdapter<RequestModel, FirebaseRequestAdapter.RequestAdapterViewHolder>(options) {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

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

        fun bindToSent(name: String, imgUrl: String, id: String){
            userName.text = name
            messageText.text = "Vaše žádost o přátelství byla odeslána."
            acceptButton.visibility = View.INVISIBLE
            acceptButton.isEnabled = false
            if (imgUrl == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(imageView)
            } else {
                Glide.with(itemView).load(imgUrl).into(imageView)
            }
            declineButton.setOnClickListener {
                Log.d("MAK", "Mackas decline na userovi " + id + " a ty jsi: " + auth.currentUser.uid)
                db.collection("Users").document(auth.currentUser.uid).collection("Requests").document(id).delete()
                db.collection("Users").document(id).collection("Requests").document(auth.currentUser.uid).delete()
            }
            Log.d("MAK", "Vykresluju")
        }

        fun bindToRecieved(name: String, imgUrl: String, id: String) {
            userName.text = name
            messageText.text = "Vám odesílá žádost o přátelství."
            if (imgUrl == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(imageView)
            } else {
                Glide.with(itemView).load(imgUrl).into(imageView)
            }
            declineButton.setOnClickListener {
                db.collection("Users").document(auth.currentUser.uid).collection("Requests").document(id).delete()
                db.collection("Users").document(id).collection("Requests").document(auth.currentUser.uid).delete()

                Log.d("MAK", "Mackas decline na userovi " + id + " a ty jsi: " + auth.currentUser.uid)
            }
            acceptButton.setOnClickListener {
                db.collection("Users").document(auth.currentUser.uid).collection("Requests").document(id).delete()
                db.collection("Users").document(id).collection("Requests").document(auth.currentUser.uid).delete()

                val friendshipID = db.collection("Friendships").document()
                val friendship: MutableMap<String, Any> = HashMap()

                friendship["uid"] = friendshipID.id
                friendship["member1"] = auth.currentUser.uid
                friendship["member2"] = id

                friendshipID.set(friendship)

                db.collection("Users").document(auth.currentUser.uid).collection("Friendships").document(id).set(friendship)
                db.collection("Users").document(id).collection("Friendships").document(auth.currentUser.uid).set(friendship)
            }
            Log.d("MAK", "Vykresluju")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return RequestAdapterViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val type = snapshots.getSnapshot(position).get("type")
        //Log.d("MAK", "Item type jest: " + type)
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

        db.collection("Users").document(id).addSnapshotListener{
            snapshot, e ->
            if (e != null) {
                Log.w("MAK", "Listening failed: " + e)
            }

            if (snapshot != null && snapshot.exists()) {
                name = snapshot.data?.get("name").toString()
                image = snapshot.data?.get("url").toString()
                Log.d("MAK", "Name jest: " + name)
                Log.d("MAK", "URL jest: " + image)

                if(getItemViewType(position) == REQUEST_TYPE_SENT) {
                    holder.bindToSent(name, image, id)
                } else {
                    holder.bindToRecieved(name, image, id)
                }
            } else {
                Log.w("MAK", "Current data null")
            }
        }

        holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
        /*
        if(getItemViewType(position) == REQUEST_TYPE_SENT) {
            holder.bindToSent(name, image)
        } else {
            holder.bindToRecieved(name, image)
        }*/
    //holder.bindTo(id, "ahoj")
    }
}