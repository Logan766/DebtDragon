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
import tech.janhoracek.debtdragon.databinding.FragmentDashboradUserCategoryGraphBinding
import tech.janhoracek.debtdragon.utility.BaseFragment

class DashboradUserCategoryGraph : BaseFragment() {
    private lateinit var binding: FragmentDashboradUserCategoryGraphBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboradUserCategoryGraphBinding.inflate(inflater, container, false)


        viewModel.userCategoryPieData.observe(viewLifecycleOwner, Observer { pieData ->
            setupFriendCategoryPie(pieData)
        })


        return binding.root
    }

    private fun setupFriendCategoryPie(data: PieData) {
        data.setValueFormatter(PercentFormatter(binding.pieUserCategoryDashboard))
        binding.pieUserCategoryDashboard.setCenterTextColor(requireActivity().getColor(R.color.white))
        binding.pieUserCategoryDashboard.description.isEnabled = false
        binding.pieUserCategoryDashboard.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieUserCategoryDashboard.transparentCircleRadius = 0F
        binding.pieUserCategoryDashboard.setUsePercentValues(true)
        binding.pieUserCategoryDashboard.animateY(500)
        binding.pieUserCategoryDashboard.legend.isEnabled = false
        binding.pieUserCategoryDashboard.isRotationEnabled = true
        binding.pieUserCategoryDashboard.data = data
        binding.pieUserCategoryDashboard.notifyDataSetChanged()
        binding.pieUserCategoryDashboard.invalidate()
    }


}