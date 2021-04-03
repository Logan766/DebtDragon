package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.bill_item.view.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.BillModel
import tech.janhoracek.debtdragon.utility.Constants

class FirebaseBillAdapter(options: FirestoreRecyclerOptions<BillModel>, val mBillListener: OnBillClickListener) :
    FirestoreRecyclerAdapter<BillModel, FirebaseBillAdapter.BillAdapterViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    inner class BillAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val billOwnerName = itemView.tv_payer_BillItem
        val billName = itemView.tv_name_BillItem
        val billOwnerImage = itemView.image_View_BillItem
        val billValue = itemView.tv_value_BillItem

        fun bindVisibleInfo(ownerID: String ,ownerName: String, image: String, billID: String, name: String) {
            itemView.setOnClickListener {
                mBillListener.onDebtClick(billID)
            }

            billName.text = name

            if(ownerID == auth.currentUser.uid) {
                billOwnerName.text = "Já"
            } else {
                billOwnerName.text = ownerName
            }

            if(image == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(billOwnerImage)
            } else {
                Glide.with(itemView).load(image).into(billOwnerImage)
            }

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.bill_item, parent, false)
        return  BillAdapterViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: BillAdapterViewHolder,
        position: Int,
        model: BillModel
    ) {
        var billID = ""
        var owner = ""
        var image = ""
        var billSum = 0

        GlobalScope.launch(Main) {
            db.collection(Constants.DATABASE_USERS).document(model.payer).addSnapshotListener{snapshot, error ->
                if (error != null) {
                    Log.w("LSTNR", error.message.toString())
                }

                if (snapshot != null && snapshot.exists()) {
                    owner = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                    image = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                    holder.bindVisibleInfo(model.payer ,owner, image, model.id, model.name)
                } else {
                    Log.w("DATA", "Current data null")
                }
            }
        }
    }

    interface OnBillClickListener {
        fun onDebtClick(billID: String)
    }
}