package tech.janhoracek.debtdragon.friends.ui

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
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import kotlinx.coroutines.flow.onEach
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


class FriendDetailFragment : BaseFragment(), FirebaseDebtAdapter.OnDebtClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var offsetStatus = true

    private lateinit var binding: FragmentFriendDetailBinding

    //private lateinit var viewModel: FriendDetailViewModel
    //val viewModel: FriendDetailViewModel by viewModels<FriendDetailViewModel>({requireParentFragment().requireParentFragment()})
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
        /*if (savedInstanceState == null) {
            val args: FriendDetailFragmentArgs by navArgs()
            viewModel = ViewModelProvider(requireActivity()).get(FriendDetailViewModel::class.java)
            viewModel.setData(args.userId)
        }*/
        Log.d("SUTR", "Vracim tady sebe: " + this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        val args: FriendDetailFragmentArgs by navArgs()
        //viewModel = ViewModelProvider(requireActivity()).get(FriendDetailViewModel::class.java)
        if (savedInstanceState == null) {
            viewModel.setData(args.userId)
        }
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        Log.d("CIGO", "View model v DetailFragmentu: " + viewModel)

        appBarLayout = binding.materialupAppbar
        ivUserAvatar = binding.materialupProfileImage

        avatar_normalwidth = ivUserAvatar.layoutParams.width
        avatar_normalHeight = ivUserAvatar.layoutParams.height
        avatar_normalTopMargin = ivUserAvatar.marginTop

        binding.materialupAppbar.setExpanded(offsetStatus, false)

        val graphList = arrayListOf<Fragment>(
            FriendDetailSummaryGraphFragment(),
            FriendDetailCategoryGraphFragment(),
            FriendDetailCategoryFriendFragment()
        )

        val graphAdapter = ViewPagerAdapter(graphList, childFragmentManager, lifecycle)
        binding.viewPagerGraphFriendDetailFragment.adapter = graphAdapter
        binding.springDotsIndicator.setViewPager2(binding.viewPagerGraphFriendDetailFragment)
        binding.viewPagerGraphFriendDetailFragment.post {
            binding.viewPagerGraphFriendDetailFragment.currentItem = 0
        }

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { appBarLayout, icko ->
                Log.d("TRPOS", "Meni se offset " + icko)
                val offset = abs(icko / appBarLayout.totalScrollRange.toFloat())
                updateUI(offset)
            })

        binding.lottieArrowUpFriendDetail.setOnClickListener {
            binding.materialupAppbar.setExpanded(false, true)
        }

        viewModel.debtSummaryLive.observe(viewLifecycleOwner, Observer { sliderValue ->
            if(sliderValue != null) {
                setupCreatePayment(sliderValue)
            }
        })

        viewModel.friendData.observe(viewLifecycleOwner, Observer { data ->
            Log.d("QRRR", "OBSERVER")
            setupQR(data)
        })




        binding.toolbarFriendDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: FriendDetailFragmentArgs by navArgs()

        setUpRecyclerView(args.userId)
        debtAdapter!!.startListening()

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
                }
            }.observeInLifecycle(viewLifecycleOwner)

        binding.toolbarFriendDetail.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_friend -> {
                    Log.d("RANO", "Klikas na delete friend")
                }
                R.id.generateQR -> {
                    navigateToQR(view)
                    Log.d("RANO", "Klikas na generate QR")
                }
                R.id.createPayment -> {
                    navigateToCreatePayment(view)
                }
            }
            true
        }

        binding.paymentBottomFriendDetail.setOnClickListener {
            navigateToCreatePayment(view)
        }


    }

    private fun updateUI(offset: Float) {
        Log.d("TRPOS", "Tady je offset pro obrazek " + offset)
        ivUserAvatar.apply {
            this.layoutParams.also {
                it.height = (avatar_normalHeight - (offset * 150)).toInt()
                it.width = (avatar_normalwidth - (offset * 150)).toInt()
                this.layoutParams = it
            }
        }
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


    private fun goBack(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_friendsOverViewFragment)
    }

    private fun navigateToQR(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_generateQRCodeFragment)
    }

    private fun navigateToCreatePayment(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_createPaymentFragment)
        Log.d("LETY", "Klikas na create payment")
    }

    private fun setUpRecyclerView(friendshipID: String) {
        val query = db.collection(Constants.DATABASE_FRIENDSHIPS).document(friendshipID).collection(Constants.DATABASE_DEBTS)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<DebtModel> = FirestoreRecyclerOptions.Builder<DebtModel>()
            .setQuery(query, DebtModel::class.java)
            .build()

        debtAdapter = FirebaseDebtAdapter(firestoreRecyclerOptions, this)


        binding.recyclerViewFragmentFriendDetail.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewFragmentFriendDetail.adapter = debtAdapter
    }

    override fun onDebtClick(debtID: String) {
        val action = FriendDetailFragmentDirections.actionFriendDetailFragmentToAddEditDebtFragment(debtID,
            viewModel.friendshipData.value!!,
            viewModel.friendData.value!!.name)
        findNavController().navigate(action)
    }

    private fun setupQR(data: FriendDetailModel) {
        var summary = viewModel.debtSummaryLive.value
        Log.d("QRRR", "summary je: " + summary)
        if (summary != null) {
            if (data.account.isNotEmpty() && summary < 1) {
                Log.d("QRRR", "Nastavuji QR na true")
                qr_bottom_FriendDetail.isClickable = true
                qr_bottom_FriendDetail.background = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
                binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
                binding.toolbarFriendDetail.menu.getItem(1).isEnabled = true
            } else {
                Log.d("QRRR", "Nastavuji QR na false")
                qr_bottom_FriendDetail.isClickable = false
                qr_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_qr_code_24_gray)
                //qr_bottom_FriendDetail.background = resources.getDrawable(R.drawable.ic_baseline_qr_code_24_gray)
                binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24_gray)
                binding.toolbarFriendDetail.menu.getItem(1).isEnabled = false
            }
        }


        /*if (data.account == "" || userIsDebter) {
            qr_bottom_FriendDetail.isClickable = false
            qr_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_qr_code_24_gray)
            //qr_bottom_FriendDetail.background = resources.getDrawable(R.drawable.ic_baseline_qr_code_24_gray)
            binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24_gray)
            binding.toolbarFriendDetail.menu.getItem(1).isEnabled = false
        } else {
            qr_bottom_FriendDetail.isClickable = true
            qr_bottom_FriendDetail.background = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
            binding.toolbarFriendDetail.menu.getItem(1).icon = resources.getDrawable(R.drawable.ic_baseline_qr_code_24)
            binding.toolbarFriendDetail.menu.getItem(1).isEnabled = true
        }*/
    }

    private fun setupCreatePayment(sliderValue: Int) {
        Log.d("QRRR", "Nastavuji PAYMENT")
        if (sliderValue > 0) {
            Log.d("Zmena", "Slider je min nez 1, nastavuju false")
            payment_bottom_FriendDetail.isClickable = false
            payment_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_payments_24_gray)
            binding.toolbarFriendDetail.menu.getItem(2).icon = resources.getDrawable(R.drawable.ic_baseline_payments_24_gray)
            binding.toolbarFriendDetail.menu.getItem(2).isEnabled = false
        } else {
            Log.d("Zmena", "Slider je vic nez 1, nastavuju true")
            payment_bottom_FriendDetail.isClickable = true
            payment_bottom_FriendDetail.setImageResource(R.drawable.ic_baseline_payments_24)
            binding.toolbarFriendDetail.menu.getItem(2).icon = resources.getDrawable(R.drawable.ic_baseline_payments_24)
            binding.toolbarFriendDetail.menu.getItem(2).isEnabled = true
        }
    }

    private fun setuIcons() {

    }

}