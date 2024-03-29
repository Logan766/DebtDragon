package tech.janhoracek.debtdragon.friends.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debt_friend_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendshipModel
import tech.janhoracek.debtdragon.friends.ui.FriendDetailFragmentDirections
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Firebase debt adapter
 *
 * @property mDebtListener interface listener for clicking on debt
 * @constructor
 *
 * @param options firestore recycler options of Debt Model
 */
class FirebaseDebtAdapter constructor(options: FirestoreRecyclerOptions<DebtModel>, val mDebtListener: OnDebtClickListener):
    FirestoreRecyclerAdapter<DebtModel,FirebaseDebtAdapter.DebtViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Debt view holder
     *
     * @constructor
     *
     * @param itemView
     */
    inner class DebtViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val view = itemView


        /**
         * Bind to DebtViewHolder
         *
         * @param debt as DebtModel (data)
         * @param payerName as full name of payer
         * @param debtID as ID of debt
         */
        fun bindToVH(debt: DebtModel, payerName: String, debtID: String) {
            // Determines who is payer of the debt and set colors based on that
            if(debt.payer == auth.currentUser.uid) {
                itemView.tv_value_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.main))
                itemView.image_View_DebtFriendDetail.borderColor = itemView.resources.getColor(R.color.main)
                itemView.tv_currency_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.main))
                itemView.tv_payer_debtFriendItem.text = localized(R.string.payer_me)
            } else {
                itemView.tv_value_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.second))
                itemView.tv_currency_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.second))
                itemView.tv_payer_debtFriendItem.text = payerName
            }

            // Load debt image
            if (debt.img.isNotEmpty()) {
                Glide.with(itemView).load(debt.img).into(itemView.image_View_DebtFriendDetail)
            } else {
                Glide.with(itemView).load(R.drawable.debt_placeholder).into(itemView.image_View_DebtFriendDetail)
            }

            itemView.tv_name_debtFriendItem.text = debt.name
            itemView.tv_value_debtFriendItem.text = debt.value.toString()

            // Determines whether debt is Payment or Group Debt and disables editing on them
            when (debt.category) {
                Constants.DATABASE_DEBT_CATEGORY_PAYMENT -> {
                    itemView.setOnClickListener{
                        Toast.makeText(view.context, localized(R.string.payments_cant_be_edited), Toast.LENGTH_LONG).show()
                    }
                }
                Constants.DATABASE_DEBT_CATEGORY_GDEBT -> {
                    itemView.setOnClickListener{
                        Toast.makeText(view.context, localized(R.string.gdebts_cant_be_edited), Toast.LENGTH_LONG).show()
                    }
                }
                else -> {
                    itemView.setOnClickListener {
                        mDebtListener.onDebtClick(debt.id)
                    }
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.debt_friend_item, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int, model: DebtModel) {
        db.collection(Constants.DATABASE_USERS).document(model.payer).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("LSTNR", "Listening failed: " + error)
            }

            if (snapshot != null && snapshot.exists()) {
                val payerName = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                holder.bindToVH(model, payerName, model.id)
                holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
            } else {
                Log.w("LSTNR", "Current data null")
            }
        }
        holder.itemView.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
    }

    /**
     * On debt click listener interface
     *
     * @constructor Create empty On debt click listener
     */
    interface OnDebtClickListener {
        fun onDebtClick(debtID: String)
    }




}