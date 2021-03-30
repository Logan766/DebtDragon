package tech.janhoracek.debtdragon.dashboard.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.github.mikephil.charting.data.PieData
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboradSummaryGraphBinding
import tech.janhoracek.debtdragon.utility.BaseFragment

class DashboradSummaryGraph : BaseFragment() {
    private lateinit var binding: FragmentDashboradSummaryGraphBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboradSummaryGraphBinding.inflate(inflater, container, false)

        viewModel.summaryPieData.observe(viewLifecycleOwner, Observer { pieData->
            setupSummaryPie(pieData)
        })


        return binding.root
    }


    private fun setupSummaryPie(pieData: PieData) {
        Log.d("KOLAC", "Sypu data do kolace")
        binding.pieSummaryDashboard.description.isEnabled = false
        binding.pieSummaryDashboard.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieSummaryDashboard.transparentCircleRadius = 0F
        binding.pieSummaryDashboard.setUsePercentValues(true)
        binding.pieSummaryDashboard.animateY(500)
        binding.pieSummaryDashboard.legend.isEnabled = false
        binding.pieSummaryDashboard.data = pieData
        binding.pieSummaryDashboard.maxAngle = 270F
        binding.pieSummaryDashboard.rotationAngle = 135F
        binding.pieSummaryDashboard.isRotationEnabled = false
        binding.pieSummaryDashboard.notifyDataSetChanged()
        binding.pieSummaryDashboard.invalidate()
    }
}