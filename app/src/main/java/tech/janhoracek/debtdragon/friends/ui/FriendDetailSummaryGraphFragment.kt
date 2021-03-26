package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.res.colorResource
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navGraphViewModels
import com.github.mikephil.charting.data.PieData
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailSummaryGraphBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


class FriendDetailSummaryGraphFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailSummaryGraphBinding
    //private lateinit var viewModel: FriendDetailViewModel
    val viewModel by viewModels<FriendDetailViewModel>({requireParentFragment()})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailSummaryGraphBinding.inflate(inflater, container, false)
        /*viewModel.debtSummary.observe(viewLifecycleOwner, Observer {
            binding.tvGrafTest.text = it
        })*/

        viewModel.pieData.observe(viewLifecycleOwner, Observer { pieData ->
            setupPie(pieData)
        })

        //viewModel = ViewModelProvider(requireParentFragment()).get(FriendDetailViewModel::class.java)
        Log.d("SUTR", "Vracim tady promenou: " + viewModel.debtSummary.value)
        Log.d("SUTR", "Vracim tady rodice: " + requireParentFragment())
        return binding.root
    }

    private fun setupPie(pieData: PieData) {
        Log.d("KOLAC", "Sypu data do kolace")
        binding.pieChartFriendDetailChildFragment.description.isEnabled = false
        binding.pieChartFriendDetailChildFragment.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieChartFriendDetailChildFragment.transparentCircleRadius = 0F
        binding.pieChartFriendDetailChildFragment.setUsePercentValues(true)
        binding.pieChartFriendDetailChildFragment.animateY(500)
        binding.pieChartFriendDetailChildFragment.legend.isEnabled = false
        binding.pieChartFriendDetailChildFragment.data = pieData
        binding.pieChartFriendDetailChildFragment.maxAngle = 270F
        binding.pieChartFriendDetailChildFragment.rotationAngle = 135F
        binding.pieChartFriendDetailChildFragment.isRotationEnabled = false
        binding.pieChartFriendDetailChildFragment.notifyDataSetChanged()
        binding.pieChartFriendDetailChildFragment.invalidate()
    }

}