package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.payment_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import tech.janhoracek.debtdragon.utility.Constants

class FirebaseResultsAdapter(options: FirestoreRecyclerOptions<PaymentModel>, val mCheckboxListener: OnCheckboxChangeListener, val mButtonListener: OnAddToFriendDebtsListener, isOwner: Boolean) :
    FirestoreRecyclerAdapter<PaymentModel, FirebaseResultsAdapter.PaymentViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val isOwner = isOwner


    inner class PaymentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindToVH(value: Int) {
            itemView.value_payment_item.text = value.toString()
        }

        fun bindCreditor(creditorName: String, creditorImg: String, model: PaymentModel) {
            if (model.creditor == auth.currentUser?.uid) {
                itemView.tv_creditor_name_payment_item.text = "Já"
            } else {
                itemView.tv_creditor_name_payment_item.text = creditorName
            }
            if(creditorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_creditor_payment_item)
            } else {
                Glide.with(itemView).load(creditorImg).into(itemView.CircImageView_creditor_payment_item)
            }
        }

        fun bindDebtor(debtorName: String, debtorImg: String, model: PaymentModel) {
            if (model.debtor == auth.currentUser?.uid) {
                itemView.tv_debtor_name_payment_item.text = "Já"
            } else {
                itemView.tv_debtor_name_payment_item.text = debtorName
            }
            if(debtorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_debtor_payment_item)
            } else {
                Glide.with(itemView).load(debtorImg).into(itemView.CircImageView_debtor_payment_item)
            }
        }

        fun setPayButton(isFriend: Boolean, model: PaymentModel) {
            itemView.FAB_payment_item.isVisible = isFriend
            itemView.FAB_payment_item.setOnClickListener {
                mButtonListener.onAddToFriendsBtnClick(model.id, model.value)
            }
        }

        fun bindCheckbox(model: PaymentModel) {
            if ((model.debtor == auth.currentUser?.uid) || isOwner) {
                itemView.checkbox_paymentItem.isVisible = true
                itemView.checkbox_paymentItem.isEnabled = !model.isResolved
                itemView.checkbox_paymentItem.isChecked = model.isResolved

                itemView.checkbox_paymentItem.setOnCheckedChangeListener { buttonView, isChecked ->
                    if(isChecked) {
                        mCheckboxListener.onCheckboxChange(model.id)
                    }
                }
            } else {
                itemView.checkbox_paymentItem.isVisible = false
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

        holder.bindCheckbox(model)

        db.collection(Constants.DATABASE_USERS).document(model.creditor).addSnapshotListener { userData, error ->
            creditorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            creditorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindCreditor(creditorName, creditorImg, model)
        }

        db.collection(Constants.DATABASE_USERS).document(model.debtor).addSnapshotListener { userData, error ->
            debtorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            debtorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindDebtor(debtorName, debtorImg, model)
        }

        if (model.debtor == auth.currentUser.uid) {
            db.collection(Constants.DATABASE_USERS)
                .document(model.debtor)
                .collection(Constants.DATABASE_FRIENDSHIPS)
                .document(model.creditor)
                .addSnapshotListener { snapshot, error ->
                    if(error != null) {
                        Log.w("LSTNR", error.message.toString())
                    }

                    if (snapshot != null && snapshot.exists()) {
                        //je friend
                        holder.setPayButton(true && !model.isResolved, model)
                    } else {
                        //neni friend
                        holder.setPayButton(false, model)
                        Log.w("DATA", "Current data null")
                    }
                }
        } else {
            holder.setPayButton(false, model)
        }

    }

    interface OnCheckboxChangeListener {
        fun onCheckboxChange(paymentID: String)
    }

    interface OnAddToFriendDebtsListener {
        fun onAddToFriendsBtnClick(paymentID: String, value: Int)
    }
}