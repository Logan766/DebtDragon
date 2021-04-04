package tech.janhoracek.debtdragon.groups.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.debt_friend_item.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.utility.Constants

class FirebaseGroupDebtAdapter(options: FirestoreRecyclerOptions<GroupDebtModel>) :
    FirestoreRecyclerAdapter<GroupDebtModel, FirebaseGroupDebtAdapter.GroupDebtViewHolder>(options){
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    class GroupDebtViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bindToVH(model: GroupDebtModel, debtorName: String, debtorImage: String) {
            itemView.tv_value_debtFriendItem.text = model.value
            itemView.tv_payer_debtFriendItem.text = debtorName
            itemView.tv_name_debtFriendItem.text = model.name

            Log.d("NEDELE", "Pro " + debtorName + " je img value: " + debtorImage)

            if(debtorImage == "null") {
                Glide.with(itemView).load(R.drawable.avatar_profileavatar).into(itemView.image_View_DebtFriendDetail)
            } else {
                Glide.with(itemView).load(debtorImage).into(itemView.image_View_DebtFriendDetail)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupDebtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.debt_friend_item, parent, false)
        return GroupDebtViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GroupDebtViewHolder,
        position: Int,
        model: GroupDebtModel
    ) {


        db.collection(Constants.DATABASE_USERS).document(model.debtor).addSnapshotListener{snapshot, error ->
            if (error != null) {
                Log.w("LSTNR", "Listening failed: " + error)
            }

            if (snapshot != null && snapshot.exists()) {
                val debtorName = snapshot.data?.get(Constants.DATABASE_USER_NAME).toString()
                val debtorImage = snapshot.data?.get(Constants.DATABASE_USER_IMG_URL).toString()
                holder.bindToVH(model, debtorName, debtorImage)
                //holder.view.animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.recycler_animation)
            } else {
                Log.w("LSTNR", "Current data null")
            }
        }
    }
}