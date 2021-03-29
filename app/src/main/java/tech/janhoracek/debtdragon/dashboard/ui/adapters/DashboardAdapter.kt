package tech.janhoracek.debtdragon.dashboard.ui.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DashboardAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    class topFriendsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    class pieChartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        return 5
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }
}