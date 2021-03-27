package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.utils.ColorTemplate
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailCategoryGraphBinding
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailSummaryGraphBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


class FriendDetailCategoryGraphFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailCategoryGraphBinding
    val viewModel by navGraphViewModels<FriendDetailViewModel>(R.id.friends)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendDetailCategoryGraphBinding.inflate(inflater, container, false)
        binding.pieChartCategoryFriendFriendDetailChildFragment.animate()
        viewModel.pieCategoryFriendData.observe(viewLifecycleOwner, Observer { pieData ->
            setupFriendCategoryPie(pieData)
        })


        return binding.root
    }

    private fun setupFriendCategoryPie(data: PieData) {
        binding.pieChartCategoryFriendFriendDetailChildFragment.centerText = "Poměr vašich dluhů"
        binding.pieChartCategoryFriendFriendDetailChildFragment.setCenterTextColor(requireActivity().getColor(R.color.white))
        binding.pieChartCategoryFriendFriendDetailChildFragment.description.isEnabled = false
        binding.pieChartCategoryFriendFriendDetailChildFragment.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieChartCategoryFriendFriendDetailChildFragment.transparentCircleRadius = 0F
        binding.pieChartCategoryFriendFriendDetailChildFragment.setUsePercentValues(true)
        binding.pieChartCategoryFriendFriendDetailChildFragment.animateY(500)
        binding.pieChartCategoryFriendFriendDetailChildFragment.legend.isEnabled = false
        binding.pieChartCategoryFriendFriendDetailChildFragment.isRotationEnabled = false
        binding.pieChartCategoryFriendFriendDetailChildFragment.data = data
        binding.pieChartCategoryFriendFriendDetailChildFragment.notifyDataSetChanged()
        binding.pieChartCategoryFriendFriendDetailChildFragment.invalidate()
    }

}