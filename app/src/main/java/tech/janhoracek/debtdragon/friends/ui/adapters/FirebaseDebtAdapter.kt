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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debt_friend_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.Constants

class FirebaseDebtAdapter(options: FirestoreRecyclerOptions<DebtModel>) :
    FirestoreRecyclerAdapter<DebtModel,FirebaseDebtAdapter.DebtViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    class DebtViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val view = itemView

        fun bindToVH(debt: DebtModel, payerName: String) {
            if(debt.payer == auth.currentUser.uid) {
                //itemView.tv_payer_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.main))
                itemView.tv_value_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.main))
                itemView.image_View_DebtFriendDetail.borderColor = itemView.resources.getColor(R.color.main)
                itemView.tv_payer_debtFriendItem.text = localized(R.string.payer_me)
            } else {
                //itemView.tv_payer_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.second))
                itemView.tv_value_debtFriendItem.setTextColor(itemView.resources.getColor(R.color.second))
                //itemView.image_View_DebtFriendDetail.borderColor = itemView.resources.getColor(R.color.second)
                itemView.tv_payer_debtFriendItem.text = payerName
            }

            if (debt.img.isNotEmpty()) {
                Glide.with(itemView).load(debt.img).into(itemView.image_View_DebtFriendDetail)
            } else {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.image_View_DebtFriendDetail)
            }

            itemView.tv_name_debtFriendItem.text = debt.name
            itemView.tv_value_debtFriendItem.text = debt.value.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.debt_friend_item, parent, false)
        return DebtViewHolder(view)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int, model: DebtModel) {
        db.collection(Constants.DATABASE_USERS).document(model.payer).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("MAK", "Listening failed: " + error)
            }

            if (snapshot != null && snapshot.exists()) {
                val payerName = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                holder.bindToVH(model, payerName)
                holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
            } else {
                Log.w("MAK", "Current data null")
            }
        }
    }



}