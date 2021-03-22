package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.marginTop
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import kotlin.math.abs


class FriendDetailFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailBinding
    private lateinit var viewModel: FriendDetailViewModel

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var ivUserAvatar: ImageView
    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: FriendDetailFragmentArgs by navArgs()
            viewModel = ViewModelProvider(requireActivity()).get(FriendDetailViewModel::class.java)
            viewModel.setData(args.userId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        //val view =  inflater.inflate(R.layout.fragment_friend_detail, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val args: FriendDetailFragmentArgs by navArgs()

        appBarLayout = binding.materialupAppbar
        ivUserAvatar = binding.materialupProfileImage

        avatar_normalwidth = ivUserAvatar.layoutParams.width
        avatar_normalHeight = ivUserAvatar.layoutParams.height
        avatar_normalTopMargin = ivUserAvatar.marginTop

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, icko ->
                Log.d("TRPOS", "Meni se offset " + icko)
                val offset = abs(icko / appBarLayout.totalScrollRange.toFloat())
                updateUI(offset)
            })

        binding.lottieArrowUpFriendDetail.setOnClickListener {
            binding.materialupAppbar.setExpanded(false, true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val myView = binding.mainCollapsing
        val appbar = binding.materialupAppbar

    }

    private fun updateUI(offset: Float) {
        Log.d("TRPOS", "Tady je offset pro obrazek " + offset)
        ivUserAvatar.apply {
            this.layoutParams.also {
                it.height = (avatar_normalHeight-(offset*110)).toInt()
                Log.d("TRPOS", "height y mel bejt " + (avatar_normalHeight - (offset * 50)).toInt())
                it.width = (avatar_normalwidth-(offset*110)).toInt()
                Log.d("TRPOS", "width y mel bejt " + (avatar_normalHeight - (offset * 50)).toInt())

                this.layoutParams = it
            }
        }

        if(offset > 0.5) {
            requireActivity().window.statusBarColor = Color.parseColor("#120f38")
            binding.lottieArrowUpFriendDetail.visibility = View.INVISIBLE
            binding.btnBackBottomFriendDetail.visibility = View.VISIBLE
            binding.qrBottomFriendDetail.visibility = View.VISIBLE
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpFriendDetail.visibility = View.VISIBLE
            binding.btnBackBottomFriendDetail.visibility = View.GONE
            binding.qrBottomFriendDetail.visibility = View.GONE
        }
    }



}