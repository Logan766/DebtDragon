package tech.janhoracek.debtdragon.utility

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import tech.janhoracek.debtdragon.MainActivity

abstract class BaseFragment : Fragment() {
    lateinit var mainActivity: MainActivity
    protected open var bottomNavigationViewVisibility = View.VISIBLE

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the reference of the parent activity and call the setBottomNavigationVisibility method.
        if (activity is MainActivity) {
            mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(bottomNavigationViewVisibility)
        }
    }

    override fun onResume() {
        super.onResume()
        if(activity is MainActivity) {
            mainActivity.setBottomNavigationVisibility(bottomNavigationViewVisibility)
        }
    }

}