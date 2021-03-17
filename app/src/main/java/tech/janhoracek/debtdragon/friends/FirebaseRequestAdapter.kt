package tech.janhoracek.debtdragon.friends

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.request_item.view.*
import tech.janhoracek.debtdragon.R

private const val REQUEST_TYPE_SENT: Int = 0
private const val REQUEST_TYPE_RECIEVED: Int = 1

class FirebaseRequestAdapter(options: FirestoreRecyclerOptions<RequestModel>): FirestoreRecyclerAdapter<RequestModel, FirebaseRequestAdapter.RequestAdapterViewHolder>(options) {
    class RequestAdapterViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var userName = itemView.tv_RequestItem_name
        var userImage = itemView.ImageView_request_avatar



        fun bindTo(name: String, imgUrl: String){
            userName.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_sent_item, parent, false)
        return RequestAdapterViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        val type = snapshots.getSnapshot(position).get("type")
        Log.d("MAK", "Item type jest: " + type)
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
        holder.bindTo(id, "ahoj")
    }
}