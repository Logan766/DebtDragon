package tech.janhoracek.debtdragon.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import tech.janhoracek.debtdragon.R

private const val REQUEST_TYPE_SENT: Int = 0
private const val REQUEST_TYPE_RECIEVED: Int = 1

class RequestAdapter(var requestsListItems: List<RequestModel>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    class sentRequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindTo(requestModel: RequestModel) {

        }
    }

    class recievedRequestViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bindTo(requestModel: RequestModel) {

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(requestsListItems[position].type == "sent") {
            REQUEST_TYPE_SENT
        } else {
            REQUEST_TYPE_RECIEVED
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == REQUEST_TYPE_SENT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.request_sent_item, parent, false)
            return sentRequestViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
            return recievedRequestViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        if(getItemViewType(position) == REQUEST_TYPE_SENT) {
            (holder as sentRequestViewHolder).bindTo(requestsListItems[position])
        } else {
            (holder as recievedRequestViewHolder).bindTo(requestsListItems[position])
        }
    }

    override fun getItemCount(): Int {
        return requestsListItems.size
    }

}