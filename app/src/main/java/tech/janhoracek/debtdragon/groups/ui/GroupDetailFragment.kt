package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginTop
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.material.appbar.AppBarLayout
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupDetailBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import kotlin.math.abs

class GroupDetailFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentGroupDetailBinding

    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var groupAvatar: ImageView

    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0

    var offsetStatus = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //var args:
        if(savedInstanceState == null) {
            viewModel.setData()
        }

        binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        appBarLayout = binding.appbarGroupdetail
        groupAvatar = binding.profileImageGroupDetail

        avatar_normalwidth = groupAvatar.layoutParams.width
        avatar_normalHeight = groupAvatar.layoutParams.height
        avatar_normalTopMargin = groupAvatar.marginTop

        appBarLayout.setExpanded(offsetStatus, false)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val offset = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            updateUI(offset)
        })

        binding.lottieArrowUpGroupDetail.setOnClickListener {
            appBarLayout.setExpanded(false, true)
        }

        binding.toolbarGroupDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun updateUI(offset: Float) {
        groupAvatar.apply {
            this.layoutParams.also {
                it.height = (avatar_normalHeight - (offset * 150)).toInt()
                it.width = (avatar_normalwidth - (offset * 150)).toInt()
                this.layoutParams = it
            }
        }
        if (offset > 0.5) {
            requireActivity().window.statusBarColor = Color.parseColor("#120f38")
            binding.lottieArrowUpGroupDetail.visibility = View.INVISIBLE
            //binding.btnBackBottomFriendDetail.visibility = View.VISIBLE
            //binding.qrBottomFriendDetail.visibility = View.VISIBLE
            //binding.paymentBottomFriendDetail.visibility = View.VISIBLE
            offsetStatus = false
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpGroupDetail.visibility = View.VISIBLE
            //binding.btnBackBottomFriendDetail.visibility = View.GONE
            //binding.qrBottomFriendDetail.visibility = View.GONE
            //binding.paymentBottomFriendDetail.visibility = View.GONE
            offsetStatus = true
        }
    }

}