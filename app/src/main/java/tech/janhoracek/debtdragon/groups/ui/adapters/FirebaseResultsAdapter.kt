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
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Firebase results adapter
 *
 * @property mCheckboxListener as interface for checkbox click
 * @property mButtonListener as interface for result button click
 * @constructor
 *
 * @param options as firestore recycler view for Payment Model
 * @param isOwner as status if current user is owner
 */
class FirebaseResultsAdapter(options: FirestoreRecyclerOptions<PaymentModel>, val mCheckboxListener: OnCheckboxChangeListener, val mButtonListener: OnAddToFriendDebtsListener, isOwner: Boolean) :
    FirestoreRecyclerAdapter<PaymentModel, FirebaseResultsAdapter.PaymentViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val isOwner = isOwner

    /**
     * Payment view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class PaymentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        /**
         * Bind payment value to viewholder
         *
         * @param value as payment value
         */
        fun bindToVH(value: Int) {
            itemView.value_payment_item.text = value.toString()
        }

        /**
         * Bind creditor data to viewholder
         *
         * @param creditorName as creditor name
         * @param creditorImg as creditor img url
         * @param model as Payment Model
         */
        fun bindCreditor(creditorName: String, creditorImg: String, model: PaymentModel) {
            if (model.creditor == auth.currentUser?.uid) {
                itemView.tv_creditor_name_payment_item.text = localized(R.string.Me)
            } else {
                itemView.tv_creditor_name_payment_item.text = creditorName
            }
            if(creditorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_creditor_payment_item)
            } else {
                Glide.with(itemView).load(creditorImg).into(itemView.CircImageView_creditor_payment_item)
            }
        }

        /**
         * Bind debtor data to view holder
         *
         * @param debtorName as debtor name
         * @param debtorImg as debtor img url
         * @param model as Payment Model
         */
        fun bindDebtor(debtorName: String, debtorImg: String, model: PaymentModel) {
            if (model.debtor == auth.currentUser?.uid) {
                itemView.tv_debtor_name_payment_item.text = localized(R.string.Me)
            } else {
                itemView.tv_debtor_name_payment_item.text = debtorName
            }
            if(debtorImg == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.CircImageView_debtor_payment_item)
            } else {
                Glide.with(itemView).load(debtorImg).into(itemView.CircImageView_debtor_payment_item)
            }
        }

        /**
         * Set up pay button in viewholder
         *
         * @param isFriend as status if creditor is current user friend
         * @param model as Payment Model
         * @param frienshipID as friendship ID
         */
        fun setPayButton(isFriend: Boolean, model: PaymentModel, frienshipID: String) {
            itemView.FAB_payment_item.isVisible = isFriend
            itemView.FAB_payment_item.setOnClickListener {
                mButtonListener.onAddToFriendsBtnClick(model.id, model.value, frienshipID, model.creditor)
            }
        }

        /**
         * Bind checkbox to view holder
         *
         * @param model as Payment Model
         */
        fun bindCheckbox(model: PaymentModel) {
            if ((model.creditor == auth.currentUser?.uid) || isOwner) {
                itemView.checkbox_paymentItem.isVisible = true
                itemView.checkbox_paymentItem.isEnabled = !model.isResolved
                itemView.checkbox_paymentItem.isChecked = model.isResolved

                itemView.checkbox_paymentItem.setOnCheckedChangeListener { buttonView, isChecked ->
                    if(isChecked) {
                        mCheckboxListener.onCheckboxChange(model.id)
                    }
                }
            } else if(model.debtor == auth.currentUser?.uid){
                itemView.checkbox_paymentItem.isVisible = true
                itemView.checkbox_paymentItem.isChecked = model.isResolved
                itemView.checkbox_paymentItem.isEnabled = false
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

        // Gets creditor data
        db.collection(Constants.DATABASE_USERS).document(model.creditor).addSnapshotListener { userData, error ->
            creditorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            creditorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindCreditor(creditorName, creditorImg, model)
        }

        // Gets debtor data
        db.collection(Constants.DATABASE_USERS).document(model.debtor).addSnapshotListener { userData, error ->
            debtorName = userData?.data?.get(Constants.DATABASE_USER_NAME).toString()
            debtorImg = userData?.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
            holder.bindDebtor(debtorName, debtorImg, model)
        }

        // Gets data for payment button
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
                        //creditor is friend
                        holder.setPayButton(true && !model.isResolved, model, snapshot[Constants.DATABASE_FRIENDSHIPS_UID].toString())
                    } else {
                        //creditor is not friend
                        holder.setPayButton(false, model, "")
                        Log.w("DATA", "Current data null")
                    }
                }
        } else {
            holder.setPayButton(false, model, "")
        }

    }

    /**
     * On checkbox change listener interface
     *
     * @constructor Create empty On checkbox change listener
     */
    interface OnCheckboxChangeListener {
        fun onCheckboxChange(paymentID: String)
    }

    /**
     * On add to friend debts listener interface
     *
     * @constructor Create empty On add to friend debts listener
     */
    interface OnAddToFriendDebtsListener {
        fun onAddToFriendsBtnClick(paymentID: String, value: Int, friendshipID: String, creditorID: String)
    }
}