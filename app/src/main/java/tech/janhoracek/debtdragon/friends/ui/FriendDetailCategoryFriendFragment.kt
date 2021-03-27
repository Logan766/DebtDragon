package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailCategoryFriendBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import java.util.*

class FriendDetailCategoryFriendFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailCategoryFriendBinding
    val viewModel by navGraphViewModels<FriendDetailViewModel>(R.id.friends)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFriendDetailCategoryFriendBinding.inflate(inflater, container, false)
        viewModel.pieCategoryUserData.observe(viewLifecycleOwner, Observer { pieData ->
            setupFriendCategoryPie(pieData)
        })



        return binding.root
    }

    private fun setupFriendCategoryPie(data: PieData) {
        binding.pieChartCategoryUserUserDetailChildFragment.centerText = "Poměr dluhů přitele"
        binding.pieChartCategoryUserUserDetailChildFragment.setCenterTextColor(requireActivity().getColor(R.color.white))
        binding.pieChartCategoryUserUserDetailChildFragment.description.isEnabled = false
        binding.pieChartCategoryUserUserDetailChildFragment.setHoleColor(requireActivity().getColor(R.color.transparent))
        binding.pieChartCategoryUserUserDetailChildFragment.transparentCircleRadius = 0F
        binding.pieChartCategoryUserUserDetailChildFragment.setUsePercentValues(true)
        binding.pieChartCategoryUserUserDetailChildFragment.animateY(500)
        binding.pieChartCategoryUserUserDetailChildFragment.legend.isEnabled = false
        binding.pieChartCategoryUserUserDetailChildFragment.isRotationEnabled = false
        binding.pieChartCategoryUserUserDetailChildFragment.data = data
        binding.pieChartCategoryUserUserDetailChildFragment.notifyDataSetChanged()
        binding.pieChartCategoryUserUserDetailChildFragment.invalidate()
    }


}