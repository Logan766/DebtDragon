package tech.janhoracek.debtdragon.friends.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.localized

/**
 * View pager adapter for friendlist and requests
 *
 * @constructor
 *
 * @param list as list of child fragments
 * @param fm as FragmentManager
 * @param lifecycle as Lifecycle
 */
class ViewPagerAdapter(list: ArrayList<Fragment>, fm: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = list

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

    /**
     * Get page title of view pager in friendlist
     *
     * @param position as position of child fragment
     * @return string as title
     */
    fun getPageTitle(position: Int): String{
        var title = ""
        when (position) {
            0 -> title = localized(R.string.friends)
            1 -> title = localized(R.string.requests)
        }
        return title
    }

}