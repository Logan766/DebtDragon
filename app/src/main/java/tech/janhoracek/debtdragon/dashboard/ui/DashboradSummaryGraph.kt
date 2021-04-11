package tech.janhoracek.debtdragon.dashboard.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import tech.janhoracek.debtdragon.databinding.FragmentDashboradSummaryGraphBinding
import tech.janhoracek.debtdragon.localized
import tech.janhoracek.debtdragon.utility.BaseFragment
import kotlin.math.abs

/**
 * Dashborad summary graph
 *
 * @constructor Create empty Dashborad summary graph
 */
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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        // Observing Pie Data for Summary Pie graph
        viewModel.summaryPieData.observe(viewLifecycleOwner, Observer { pieData->
            setupSummaryPie(pieData)
        })

        // Sets text of debt summary based on summary value
        viewModel.summary.observe(viewLifecycleOwner, Observer { summary ->
            when {
                summary == 0 -> {
                    binding.tvStatusSummaryGraphDashborad.text = getString(R.string.dashborad_summary_debts_are_equal)
                    binding.tvStatusSummaryNumberGraphDashboard.visibility = View.INVISIBLE
                }
                summary < 0 -> {
                    binding.tvStatusSummaryGraphDashborad.text = getString(R.string.dashboard_summary_you_own_to_friends)
                    binding.tvStatusSummaryNumberGraphDashboard.visibility = View.VISIBLE
                    binding.tvStatusSummaryNumberGraphDashboard.text = abs(summary).toString() + localized(R.string.currency)
                    binding.tvStatusSummaryNumberGraphDashboard.setTextColor(Color.parseColor("#ee1f43"))
                }
                else -> {
                    binding.tvStatusSummaryGraphDashborad.text = getString(R.string.dashboard_summary_friends_owe_you)
                    binding.tvStatusSummaryNumberGraphDashboard.visibility = View.VISIBLE
                    binding.tvStatusSummaryNumberGraphDashboard.text = summary.toString() + localized(R.string.currency)
                    binding.tvStatusSummaryNumberGraphDashboard.setTextColor(Color.parseColor("#120f38"))

                }
            }
        })
        return binding.root
    }


    /**
     * Setup summary pie of total sum of debts
     *
     * @param pieData
     */
    private fun setupSummaryPie(pieData: PieData) {
        pieData.setValueFormatter(PercentFormatter(binding.pieSummaryDashboard))
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