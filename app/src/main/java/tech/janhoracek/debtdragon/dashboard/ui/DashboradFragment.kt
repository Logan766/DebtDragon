package tech.janhoracek.debtdragon.dashboard.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboradBinding
import tech.janhoracek.debtdragon.friends.ui.adapters.ViewPagerAdapter
import tech.janhoracek.debtdragon.utility.BaseFragment

/**
 * Dashborad fragment overall
 *
 * @constructor Create empty Dashborad fragment
 */
class DashboradFragment : BaseFragment() {

    private lateinit var binding: FragmentDashboradBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboradBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Gets all child fragments
        val graphList = arrayListOf<Fragment>(
            DashboradSummaryGraph(),
            DashboradUserCategoryGraph(),
            DashboardFriendsCategoryGraph(),
            DashboradTopDebtors(),
            DashboradTopCreditors()
        )

        // Sets up adapter for child fragments and DotsIndicator
        val graphAdapter = ViewPagerAdapter(graphList, childFragmentManager, lifecycle)
        binding.viewPagerDashborad.adapter = graphAdapter
        binding.springDotsIndicatorDashboard.setViewPager2(binding.viewPagerDashborad)
        binding.viewPagerDashborad.post{
            binding.viewPagerDashborad.currentItem = 0
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
    }


}