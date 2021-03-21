package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.GridView
import android.widget.ImageView
import androidx.core.view.marginTop
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import kotlin.math.abs


class FriendDetailFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailBinding
    private lateinit var viewModel: FriendDetailViewModel

    ///////////////////////////
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var ivUserAvatar: ImageView
    private var EXPAND_AVATAR_SIZE: Float = 300F
    private var COLLAPSE_IMAGE_SIZE: Float = 150F
    private var horizontalToolbarAvatarMargin: Float = 0F
    private var avatarAnimateStartPointY: Float = 0F
    private var avatarCollapseAnimationChangeWeight: Float = 0F
    private var isCalculated = false
    private var verticalToolbarAvatarMargin =0F
    private var cashCollapseState: Pair<Int, Int>? = null

    ///////////////////////////////////
    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: FriendDetailFragmentArgs by navArgs()
            viewModel = ViewModelProvider(requireActivity()).get(FriendDetailViewModel::class.java)
            viewModel.setArguments(args.userId)


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
        //binding.tvMyGreetings.text = args.userId


        ////////////////////////////////////////////////////
        EXPAND_AVATAR_SIZE = 150F
        COLLAPSE_IMAGE_SIZE = 150F
        horizontalToolbarAvatarMargin = 16F
        appBarLayout = binding.materialupAppbar
        ivUserAvatar = binding.materialupProfileImage

        avatar_normalwidth = ivUserAvatar.layoutParams.width
        avatar_normalHeight = ivUserAvatar.layoutParams.height
        avatar_normalTopMargin = ivUserAvatar.marginTop

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, icko ->
                Log.d("TRPOS", "Meni se offset " + icko)
                /*if (isCalculated.not()) {
                    avatarAnimateStartPointY = Math.abs((appBarLayout.height - (EXPAND_AVATAR_SIZE + horizontalToolbarAvatarMargin)) / appBarLayout.totalScrollRange)
                    avatarCollapseAnimationChangeWeight = 1 / (1 - avatarAnimateStartPointY)
                    isCalculated = true
                }*/
                /**/
                //updateViews(Math.abs(icko / appBarLayout.totalScrollRange.toFloat()))
                val offset = abs(icko / appBarLayout.totalScrollRange.toFloat())
                updateProfile(offset)
                //updateProfile(icko /appBarLayout.totalScrollRange.toFloat())
            })

        /////////////////////////////

        binding.lottieArrowUpFriendDetail.setOnClickListener {
            binding.materialupAppbar.setExpanded(false, true)
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myView = binding.mainCollapsing
        val appbar = binding.materialupAppbar


        /*
        myView.setTag(myView.getVisibility())
        myView.getViewTreeObserver().addOnGlobalLayoutListener(OnGlobalLayoutListener {
            if (myView.getTag() == View.INVISIBLE) {
                Log.d("TRPOS", "Jsem neviditelnej!")
            } else if(myView.getTag() == View.GONE) {
                Log.d("TRPOS", "Jsem fuc")
            } else {
                Log.d("TRPOS", "Jsem tadyyyy!")
            }
        })*/

        /*btn_FriendDetailFragment_back.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_friendsOverViewFragment)
        }*/
    }

    private fun updateProfile(offset: Float) {
        Log.d("TRPOS", "Tady je offset pro obrazek " + offset)
        //ivUserAvatar.width = abs(avatar_normalwidth * offset).toInt()
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
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpFriendDetail.visibility = View.VISIBLE
        }
    }

    private fun updateViews(offset: Float) {
        /* apply levels changes*/

        /** collapse - expand switch*/
        when {
            offset < SWITCH_BOUND -> Pair(TO_EXPANDED, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
            else -> Pair(TO_COLLAPSED, cashCollapseState?.second ?: WAIT_FOR_SWITCH)
        }.apply {
            when {
                cashCollapseState != null && cashCollapseState != this -> {
                    when (first) {
                        TO_EXPANDED -> {
                            /* set avatar on start position (center of parent frame layout)*/
                            ivUserAvatar.translationX = 0F
                            /**/
                        }
                    }
                    cashCollapseState = Pair(first, SWITCHED)
                }
                else -> {
                    cashCollapseState = Pair(first, WAIT_FOR_SWITCH)
                }
            }

            /* Collapse avatar img*/
            ivUserAvatar.apply {
                when {
                    offset > avatarAnimateStartPointY -> {
                        val avatarCollapseAnimateOffset = (offset - avatarAnimateStartPointY) * avatarCollapseAnimationChangeWeight
                        val avatarSize = EXPAND_AVATAR_SIZE - (EXPAND_AVATAR_SIZE - COLLAPSE_IMAGE_SIZE) * avatarCollapseAnimateOffset
                        this.layoutParams.also {
                            it.height = Math.round(avatarSize)
                            it.width = Math.round(avatarSize)
                        }


                        this.translationX = ((appBarLayout.width - horizontalToolbarAvatarMargin - avatarSize) / 2) * avatarCollapseAnimateOffset
                    }
                    else -> this.layoutParams.also {
                        if (it.height != EXPAND_AVATAR_SIZE.toInt()) {
                            it.height = EXPAND_AVATAR_SIZE.toInt()
                            it.width = EXPAND_AVATAR_SIZE.toInt()
                            this.layoutParams = it
                        }
                        translationX = 0f
                    }
                }
            }
        }
    }

    companion object {
        const val SWITCH_BOUND = 0.8f
        const val TO_EXPANDED = 0
        const val TO_COLLAPSED = 1
        const val WAIT_FOR_SWITCH = 0
        const val SWITCHED = 1
    }


}