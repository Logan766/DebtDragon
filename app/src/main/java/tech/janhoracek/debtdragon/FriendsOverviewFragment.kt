package tech.janhoracek.debtdragon

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_friends_overview.*
import tech.janhoracek.debtdragon.databinding.FragmentFriendsOverviewBinding

private var _binding: FragmentFriendsOverviewBinding? = null
private val binding get() = _binding!!

class FriendsOverViewFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding =  FragmentFriendsOverviewBinding.inflate(inflater, container, false)

        val fragmentList = arrayListOf<Fragment>(
            FriendslistFragment(),
            PendingFriendRequestsFragment()
        )

        val adapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
        binding.viewpagerFriendsOverview.adapter = adapter


        /*
        TabLayoutMediator(tabLayout_friendsOverview, viewpager_friendsOverview) {tab, position ->
            tab.text = "${position + 1}"
            viewpager_friendsOverview.setCurrentItem(tab.position, true)
        }.attach()*/

        TabLayoutMediator(binding.tabLayoutFriendsOverview, binding.viewpagerFriendsOverview,
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                //when (position) {

                    tab.text = adapter.getPageTitle(position)

                    /*0 -> { tab.text = "Seznam přátel"}
                    1 -> { tab.text = "Žádosti"}*/
                //}
            }).attach()

        binding.tabLayoutFriendsOverview.getTabAt(1)?.getOrCreateBadge()?.setNumber(3);


        return binding.root
    }

}