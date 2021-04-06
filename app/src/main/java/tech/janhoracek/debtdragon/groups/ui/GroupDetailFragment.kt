package tech.janhoracek.debtdragon.groups.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.core.view.marginTop
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
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupDetailBinding
import tech.janhoracek.debtdragon.groups.models.BillModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseBillAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.observeInLifecycle
import kotlin.math.abs

class GroupDetailFragment : BaseFragment(), FirebaseBillAdapter.OnBillClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding: FragmentGroupDetailBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var groupAvatar: ImageView

    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0

    var offsetStatus = true

    private var billAdapter: FirebaseBillAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: GroupDetailFragmentArgs by navArgs()
        if(savedInstanceState == null) {
            viewModel.setData(args.groupID)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    GroupDetailViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                    GroupDetailViewModel.Event.HideLoading -> {(activity as MainActivity).hideLoading()}
                    GroupDetailViewModel.Event.GroupDeleted -> {}
                }
            }.observeInLifecycle(viewLifecycleOwner)

        viewModel.groupModel.observe(viewLifecycleOwner, Observer { groupData->
            setUIbasedOnStatus()
            setUpRecyclerView()
            billAdapter!!.startListening()
        })


        binding.toolbarGroupDetail.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.manage_members -> {
                    Navigation.findNavController(view).navigate(R.id.action_groupDetailFragment_to_manageMembersFragment)
                }
                R.id.lock_group -> {onLockButtonClicked()}
                R.id.calculate_group -> {}
                R.id.remove_group -> {}
                R.id.leave_group -> {}
            }
            true
        }

        viewModel.isCurrentUserOwner.observe(viewLifecycleOwner, Observer { status->
            setOwnerOptions()
        })
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
            binding.btnBackBottomGroupDetail.visibility = View.VISIBLE
            binding.membersBottomGroupDetail.visibility = View.VISIBLE
            offsetStatus = false
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpGroupDetail.visibility = View.VISIBLE
            binding.btnBackBottomGroupDetail.visibility = View.GONE
            binding.membersBottomGroupDetail.visibility = View.GONE
            offsetStatus = true
        }
    }

    private fun setOwnerOptions() {
        binding.toolbarGroupDetail.menu.getItem(1).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(2).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(3).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(4).isVisible = viewModel.isCurrentUserOwner.value!!
    }

    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS).document(viewModel.groupModel.value!!.id).collection(Constants.DATABASE_BILL).orderBy(Constants.DATABASE_BILL_TIMESTAMP, Query.Direction.DESCENDING)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<BillModel> = FirestoreRecyclerOptions.Builder<BillModel>()
            .setQuery(query, BillModel::class.java)
            .build()

        billAdapter = FirebaseBillAdapter(firestoreRecyclerOptions, this)

        binding.recyclerViewFragmentGroupDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFragmentGroupDetail.adapter = billAdapter
    }

    override fun onBillClick(billID: String) {
        val action = GroupDetailFragmentDirections.actionGroupDetailFragmentToBillDetailFragment(billID)
        findNavController().navigate(action)
        Log.d("BILL", "ID tohoto uctu jest: " + billID)
    }

    private fun setUIbasedOnStatus() {
        val status = viewModel.groupModel.value!!.calculated
        when (status) {
            "" -> {
                binding.FABGroupDetail.show()
                manageLockButton(false)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = true
                binding.ImageVeiwLockStatusGroupDetail.isVisible = false
                binding.btnShowResultsGroupDetail.visibility = View.GONE
                binding.tvSummaryGroupDetail.textSize = 20F
            }
            Constants.DATABASE_GROUPS_STATUS_LOCKED -> {
                binding.FABGroupDetail.hide()
                manageLockButton(true)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = false
                binding.ImageVeiwLockStatusGroupDetail.isVisible = true
                binding.btnShowResultsGroupDetail.visibility = View.GONE
                binding.tvSummaryGroupDetail.textSize = 20F
            }
            else -> {
                binding.FABGroupDetail.hide()
                manageLockButton(true)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = false
                binding.ImageVeiwLockStatusGroupDetail.isVisible = true
                binding.btnShowResultsGroupDetail.visibility = View.VISIBLE
                binding.tvSummaryGroupDetail.textSize = 14F
            }
        }
    }

    private fun manageLockButton(locked: Boolean) {
        if (locked) {
            binding.toolbarGroupDetail.menu.getItem(1).setIcon(R.drawable.ic_baseline_lock_open_24)
        } else {
            binding.toolbarGroupDetail.menu.getItem(1).setIcon(R.drawable.ic_baseline_lock_24)
        }
    }

    private fun onLockButtonClicked() {
        val status = viewModel.groupModel.value!!.calculated
        when (status) {
            "" -> {
                //Zamknout
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle("Uzamknout skupinu")
                dialog.setMessage("Po uzamčení skupiny bude možné položky pouze prohlížet a nebude možné je přidávat ani upravovat. Skupinu je možné kdykoliv opět odemknout. Přejete si pokračovat?")
                dialog.setPositiveButton("Ano") { dialogInterface: DialogInterface, i: Int ->
                    db.collection(Constants.DATABASE_GROUPS)
                        .document(viewModel.groupModel.value!!.id)
                        .update(Constants.DATABASE_GROUPS_STATUS, Constants.DATABASE_GROUPS_STATUS_LOCKED)
                }
                dialog.setNegativeButton("Ne") { dialogInterface: DialogInterface, i: Int ->

                }
                dialog.show()
            }
            Constants.DATABASE_GROUPS_STATUS_LOCKED -> {
                //Odemknout
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle("Odemknout skupinu")
                dialog.setMessage("Skupina bude odemčena a budou možné její úpravy. Ostatní členové budou moci přidávat položky. Přejete si pokračovat?")
                dialog.setPositiveButton("Ano") { dialogInterface: DialogInterface, i: Int ->
                    db.collection(Constants.DATABASE_GROUPS)
                        .document(viewModel.groupModel.value!!.id)
                        .update(Constants.DATABASE_GROUPS_STATUS, "")
                }
                dialog.setNegativeButton("Ne") { dialogInterface: DialogInterface, i: Int ->

                }
                dialog.show()
            }
            else -> {
                //Odemknout odstranit vysledky
            }
        }
    }

}