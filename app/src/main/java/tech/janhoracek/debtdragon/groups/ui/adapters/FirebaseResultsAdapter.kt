package tech.janhoracek.debtdragon.groups.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debt_friend_item.view.*
import kotlinx.android.synthetic.main.payment_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import tech.janhoracek.debtdragon.utility.Constants

class FirebaseResultsAdapter(options: FirestoreRecyclerOptions<PaymentModel>) :
    FirestoreRecyclerAdapter<PaymentModel, FirebaseResultsAdapter.PaymentViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    class PaymentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindToVH(value: Int) {
            itemView.value_payment_item.text = value.toString()
        }

        fun bindCreditor(creditorName: String, creditorImg: String) {
            itemView.tv_creditor_name_payment_item.text = creditorName
            if(creditorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_creditor_payment_item)
            } else {
                Glide.with(itemView).load(creditorImg).into(itemView.CircImageView_creditor_payment_item)
            }
        }

        fun bindDebtor(debtorName: String, debtorImg: String) {
            itemView.tv_debtor_name_payment_item.text = debtorName
            if(debtorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_debtor_payment_item)
            } else {
                Glide.with(itemView).load(debtorImg).into(itemView.CircImageView_debtor_payment_item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.payment_item, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: PaymentViewHolder,
        position: Int,
        model: PaymentModel
    ) {
        var value = 0
        var debtorImg = ""
        var debtorName = ""
        var creditorImg = ""
        var creditorName = ""

        value = model.value

        holder.bindToVH(value)

        db.collection(Constants.DATABASE_USERS).document(model.creditor).addSnapshotListener { userData, error ->
            creditorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            creditorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindCreditor(creditorName, creditorImg)
        }

        db.collection(Constants.DATABASE_USERS).document(model.debtor).addSnapshotListener { userData, error ->
            debtorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            debtorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindDebtor(debtorName, debtorImg)
        }
    }
}