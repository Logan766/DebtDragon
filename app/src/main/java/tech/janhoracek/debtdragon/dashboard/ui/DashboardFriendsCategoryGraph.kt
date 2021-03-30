package tech.janhoracek.debtdragon.dashboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboardFriendsCategoryGraphBinding
import tech.janhoracek.debtdragon.utility.BaseFragment

class DashboardFriendsCategoryGraph : BaseFragment() {
    private lateinit var binding: FragmentDashboardFriendsCategoryGraphBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboardFriendsCategoryGraphBinding.inflate(inflater, container, false)

        viewModel.friendsCategoryPieData.observe(viewLifecycleOwner, Observer { pieData ->
            setupFriendCategoryPie(pieData)
        })



        return binding.root
    }

    private fun setupFriendCategoryPie(data: PieData) {
        data.setValueFormatter(PercentFormatter(binding.pieFriendsCategoryDashboard))
        binding.pieFriendsCategoryDashboard.setCenterTextColor(requireActivity().getColor(R.color.white))
        binding.pieFriendsCategoryDashboard.description.isEnabled = false
        binding.pieFriendsCategoryDashboard.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieFriendsCategoryDashboard.transparentCircleRadius = 0F
        binding.pieFriendsCategoryDashboard.setUsePercentValues(true)
        binding.pieFriendsCategoryDashboard.animateY(500)
        binding.pieFriendsCategoryDashboard.legend.isEnabled = false
        binding.pieFriendsCategoryDashboard.isRotationEnabled = true
        binding.pieFriendsCategoryDashboard.data = data
        binding.pieFriendsCategoryDashboard.notifyDataSetChanged()
        binding.pieFriendsCategoryDashboard.invalidate()
    }

}