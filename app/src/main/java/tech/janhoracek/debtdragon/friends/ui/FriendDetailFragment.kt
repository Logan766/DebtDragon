package tech.janhoracek.debtdragon.friends.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.friends.models.FriendDetailModel
import tech.janhoracek.debtdragon.friends.ui.adapters.FirebaseDebtAdapter
import tech.janhoracek.debtdragon.friends.ui.adapters.ViewPagerAdapter
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.observeInLifecycle
import kotlin.math.abs


/**
 * Friend detail fragment
 *
 * @constructor Create empty Friend detail fragment
 */
class FriendDetailFragment : BaseFragment(), FirebaseDebtAdapter.OnDebtClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var offsetStatus = true

    private lateinit var binding: FragmentFriendDetailBinding
    val viewModel by navGraphViewModels<FriendDetailViewModel>(R.id.friends)
    private var debtAdapter: FirebaseDebtAdapter? = null

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var ivUserAvatar: ImageView
    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0

    private var userIsDebter = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val args: FriendDetailFragmentArgs by navArgs()
        if (savedInstanceState == null) {
            viewModel.setData(args.userId)
        }
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up appbar and avatar
        appBarLayout = binding.materialupAppbar
        ivUserAvatar = binding.materialupProfileImage

        // Get avatar parameters
        avatar_normalwidth = ivUserAvatar.layoutParams.width
        avatar_normalHeight = ivUserAvatar.layoutParams.height
        avatar_normalTopMargin = ivUserAvatar.marginTop

        // Set appbar expanded status based on last status
        binding.materialupAppbar.setExpanded(offsetStatus, false)

        // Set list of child fragments
        val graphList = arrayListOf<Fragment>(
            FriendDetailSummaryGraphFragment(),
            FriendDetailCategoryGraphFragment(),
            FriendDetailCategoryFriendFragment()
        )

        // Set up adapter for child fragments and DotsIndicator
        val graphAdapter = ViewPagerAdapter(graphList, childFragmentManager, lifecycle)
        binding.viewPagerGraphFriendDetailFragment.adapter = graphAdapter
        binding.springDotsIndicator.setViewPager2(binding.viewPagerGraphFriendDetailFragment)
        binding.viewPagerGraphFriendDetailFragment.post {
            binding.viewPagerGraphFriendDetailFragment.currentItem = 0
        }

        // Set listener to observe layout changes and update UI
        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, icko ->
                val offset = abs(icko / appBarLayout.totalScrollRange.toFloat())
                updateUI(offset)
            })

        // Set up button to quickly expand or hide upper layout
        binding.lottieArrowUpFriendDetail.setOnClickListener {
            binding.materialupAppbar.setExpanded(false, true)
        }

        // Observe debt summary and update value in friend detail
        viewModel.debtSummaryLive.observe(viewLifecycleOwner, Observer { sliderValue ->
            if(sliderValue != null) {
                setupCreatePayment(sliderValue)
            }
        })

        // Setup QR buttons
        viewModel.friendData.observe(viewLifecycleOwner, Observer { data ->
            setupQR(data)
        })


        // Setup image popup
        val imagePopup = ImagePopup(requireContext())
        imagePopup.windowHeight = 800
        imagePopup.windowWidth = 800

        // Set up listener to image popup
        binding.materialupProfileImage.setOnClickListener {
            imagePopup.initiatePopup(binding.materialupProfileImage.drawable)
            imagePopup.viewPopup()
        }

        // Set up app bar back button
        binding.toolbarFriendDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: FriendDetailFragmentArgs by navArgs()

        // Set up reycler view with args from navigation
        setUpRecyclerView(args.userId)
        debtAdapter!!.startListening()

        // Set up event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    FriendDetailViewModel.Event.NavigateBack -> {
                        goBack(view)
                    }
                    FriendDetailViewModel.Event.GenerateQR -> {
                        navigateToQR(view)
                    }
                    is FriendDetailViewModel.Event.CreateEditDebt -> {
                        val action = FriendDetailFragmentDirections.actionFriendDetailFragmentToAddEditDebtFragment(it.debtID,
                            viewModel.friendshipData.value!!,
                            viewModel.friendData.value!!.name)
                        Navigation.findNavController(view).navigate(action)
                    }
                    FriendDetailViewModel.Event.CreatePayment -> {
                    }

                    FriendDetailViewModel.Event.SetupQRforFirstTime -> {
                        setupQR(viewModel.friendData.value!!)
                    }
                    FriendDetailViewModel.Event.FriendDeleted -> {
                        (activity as MainActivity).hideLoading()
                        findNavController().navigateUp()
                    }
                    FriendDetailViewModel.Event.FrienshipExistNoMore -> {
                        findNavController().navigate(R.id.action_global_friendsOverViewFragment)
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)

        // Set up appbar menu
        binding.toolbarFriendDetail.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_friend -> {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(getString(R.string.remove_friend_title_dialog))
                    dialog.setMessage(getString(R.string.remove_friend_message_dialog))
                    dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                        (activity as MainActivity).showLoading()
                        viewModel.deleteFriend()
                    }
                    dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
                        //
                    }
                    dialog.show()
                }
                R.id.generateQR -> {
                    navigateToQR(view)
                }
                R.id.createPayment -> {
                    navigateToCreatePayment(view)
                }
            }
            true
        }

        // Set up create payment button click
        binding.paymentBottomFriendDetail.setOnClickListener {
            navigateToCreatePayment(view)
        }


    }

    /**
     * Update UI based on offset value
     *
     * @param offset as value changing during scrolling
     */
    private fun updateUI(offset: Float) {
        ivUserAvatar.apply {
            this.layoutParams.also {
                it.height = (avatar_normalHeight - (offset * 150)).toInt()
                it.width = (avatar_normalwidth - (offset * 150)).toInt()
                this.layoutParams = it
            }
        }

        // Hide or show icons and change color based on offset
        if (offset > 0.5) {
            requireActivity().window.statusBarColor = Color.parseColor("#120f38")
            binding.lottieArrowUpFriendDetail.visibility = View.INVISIBLE
            binding.springDotsIndicator.visibility = View.INVISIBLE
            binding.btnBackBottomFriendDetail.visibility = View.VISIBLE
            binding.qrBottomFriendDetail.visibility = View.VISIBLE
            binding.paymentBottomFriendDetail.visibility = View.VISIBLE
            offsetStatus = false
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpFriendDetail.visibility = View.VISIBLE
            binding.springDotsIndicator.visibility = View.VISIBLE
            binding.btnBackBottomFriendDetail.visibility = View.GONE
            binding.qrBottomFriendDetail.visibility = View.GONE
            binding.paymentBottomFriendDetail.visibility = View.GONE
            offsetStatus = true
        }
    }


    /**
     * Go back to previous fragment
     *
     * @param view
     */
    private fun goBack(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_friendsOverViewFragment)
    }

    /**
     * Navigate to QR fragment
     *
     * @param view
     */
    private fun navigateToQR(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_generateQRCodeFragment)
    }

    /**
     * Navigate to create payment
     *
     * @param view
     */
    private fun navigateToCreatePayment(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_createPaymentFragment)
        Log.d("LETY", "Klikas na create payment")
    }

    /**
     * Set up recycler view
     *
     * @param friendshipID as friendship ID
     */
    private fun setUpRecyclerView(friendshipID: String) {
        val query = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS).orderBy("timestamp", Query.Direction.DESCENDING)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<DebtModel> = FirestoreRecyclerOptions.Builder<DebtModel>()
            .setQuery(query, DebtModel::class.java)
            .build()

        debtAdapter = FirebaseDebtAdapter(firestoreRecyclerOptions, this)
        binding.recyclerViewFragmentFriendDetail.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewFragmentFriendDetail.adapter = debtAdapter
    }

    /**
     * On debt click implementation of interface from adapter
     *
     * @param debtID
     */
    override fun onDebtClick(debtID: String) {
        val action = FriendDetailFragmentDirections.actionFriendDetailFragmentToAddEditDebtFragment(debtID,
            viewModel.friendshipData.value!!,
            viewModel.friendData.value!!.name)
        findNavController().navigate(action)
    }

    /**
     * Setup QR buttons based on debt summary
     *
     * @param data as Friend Detail Model
     */
    private fun setupQR(data: FriendDetailModel) {
        val summary = viewModel.debtSummaryLive.value
        if (summary != null) {
            if (data.account.isNotEmpty() && summary < 0) {
                qr_bottom_FriendDetail.isClickable = true
                qr_bottom_FriendDetail.background = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
                binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
                binding.toolbarFriendDetail.menu.getItem(1).isEnabled = true
            } else {
                qr_bottom_FriendDetail.isClickable = false
                qr_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_qr_code_24_gray)
                binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24_gray)
                binding.toolbarFriendDetail.menu.getItem(1).isEnabled = false
            }
        }
    }

    /**
     * Setup create payment buttons based on debt summary
     *
     * @param sliderValue as value of slider
     */
    private fun setupCreatePayment(sliderValue: Int) {
        if (sliderValue >= 0) {
            payment_bottom_FriendDetail.isClickable = false
            payment_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_payments_24_gray)
            binding.toolbarFriendDetail.menu.getItem(2).icon = resources.getDrawable(R.drawable.ic_baseline_payments_24_gray)
            binding.toolbarFriendDetail.menu.getItem(2).isEnabled = false
        } else {
            payment_bottom_FriendDetail.isClickable = true
            payment_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_payments_24)
            binding.toolbarFriendDetail.menu.getItem(2).icon = resources.getDrawable(R.drawable.ic_baseline_payments_24)
            binding.toolbarFriendDetail.menu.getItem(2).isEnabled = true
        }
    }
}