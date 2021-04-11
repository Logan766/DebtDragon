package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_friends_overview.*
import tech.janhoracek.debtdragon.friends.ui.adapters.ViewPagerAdapter
import tech.janhoracek.debtdragon.databinding.FragmentFriendsOverviewBinding
import tech.janhoracek.debtdragon.friends.viewmodels.AddFriendDialogViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.FriendsOverviewViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


/**
 * Friends over view fragment
 *
 * @constructor Create empty Friends over view fragment
 */
class FriendsOverViewFragment : BaseFragment() {
    private lateinit var binding: FragmentFriendsOverviewBinding
    private var currentTab = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFriendsOverviewBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(requireActivity()).get(FriendsOverviewViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up list of child fragments
        val fragmentList = arrayListOf<Fragment>(
            FriendslistFragment(),
            PendingFriendRequestsFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, childFragmentManager, lifecycle)
        binding.viewpagerFriendsOverview.adapter = adapter

        TabLayoutMediator(binding.tabLayoutFriendsOverview, binding.viewpagerFriendsOverview
        ) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        // Set up notification on tab for requests
        viewModel.notificationCount.observe(viewLifecycleOwner, Observer { count->
            if (count == 0) {
                binding.tabLayoutFriendsOverview.getTabAt(1)?.removeBadge()
            } else {
                binding.tabLayoutFriendsOverview.getTabAt(1)?.orCreateBadge?.number = count
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onPause() {
        currentTab = binding.tabLayoutFriendsOverview.selectedTabPosition
        super.onPause()
    }

    override fun onResume() {
        binding.viewpagerFriendsOverview.post {
            binding.viewpagerFriendsOverview.currentItem = currentTab
        }
        super.onResume()
    }



}