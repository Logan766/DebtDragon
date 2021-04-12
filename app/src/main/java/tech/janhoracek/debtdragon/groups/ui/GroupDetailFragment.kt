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

/**
 * Group detail fragment
 *
 * @constructor Create empty Group detail fragment
 */
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

        // Get avatar parameters
        avatar_normalwidth = groupAvatar.layoutParams.width
        avatar_normalHeight = groupAvatar.layoutParams.height
        avatar_normalTopMargin = groupAvatar.marginTop

        appBarLayout.setExpanded(offsetStatus, false)

        // Offset change listener that update UI accordingly
        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val offset = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            updateUI(offset)
        })

        // Set up button to expand upper layout
        binding.lottieArrowUpGroupDetail.setOnClickListener {
            appBarLayout.setExpanded(false, true)
        }

        // Set up app bar back button
        binding.toolbarGroupDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    GroupDetailViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                    GroupDetailViewModel.Event.HideLoading -> {(activity as MainActivity).hideLoading()}
                    GroupDetailViewModel.Event.GroupDeleted -> {}
                    is GroupDetailViewModel.Event.EditGroup -> {findNavController().navigate(GroupDetailFragmentDirections.actionGroupDetailFragmentToCreateGroupFragment(it.groupData))}
                    GroupDetailViewModel.Event.NavigateUp -> {findNavController().navigateUp()}
                }
            }.observeInLifecycle(viewLifecycleOwner)

        // Set data based on group data
        viewModel.groupModel.observe(viewLifecycleOwner, Observer { groupData->
            setUIbasedOnStatus()
            setUpRecyclerView()
            billAdapter!!.startListening()
        })

        // Set up app bar menu
        binding.toolbarGroupDetail.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.manage_members -> {
                    Navigation.findNavController(view).navigate(R.id.action_groupDetailFragment_to_manageMembersFragment)
                }
                R.id.lock_group -> {onLockButtonClicked()}
                R.id.calculate_group -> {calculateGroupDebts()}
                R.id.remove_group -> {deleteGroup()}
                R.id.leave_group -> {leaveGroup()}
                R.id.edit_group -> {viewModel.editGroup()}
            }
            true
        }

        // Update UI based on owner status
        viewModel.isCurrentUserOwner.observe(viewLifecycleOwner, Observer { status->
            setOwnerOptions()
        })
    }

    /**
     * Update UI based on offset position
     *
     * @param offset
     */
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
            if(viewModel.groupModel.value!!.calculated == "") {
                binding.membersBottomGroupDetail.visibility = View.VISIBLE
            } else {
                binding.membersBottomGroupDetail.visibility = View.GONE
            }

            offsetStatus = false
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpGroupDetail.visibility = View.VISIBLE
            binding.btnBackBottomGroupDetail.visibility = View.GONE
            binding.membersBottomGroupDetail.visibility = View.GONE
            offsetStatus = true
        }
    }

    /**
     * Set owner options
     *
     */
    private fun setOwnerOptions() {
        binding.toolbarGroupDetail.menu.getItem(1).isVisible = viewModel.isCurrentUserOwner.value!!
        if(viewModel.groupModel.value!!.calculated != Constants.DATABASE_GROUPS_STATUS_CALCULATED) {
            binding.toolbarGroupDetail.menu.getItem(2).isVisible = viewModel.isCurrentUserOwner.value!!
        } else {
            binding.toolbarGroupDetail.menu.getItem(2).isVisible = false
        }

        binding.toolbarGroupDetail.menu.getItem(3).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(4).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(5).isVisible = !viewModel.isCurrentUserOwner.value!!
    }

    /**
     * Set up recycler view for group bills
     *
     */
    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS).document(viewModel.groupModel.value!!.id).collection(Constants.DATABASE_BILL).orderBy(Constants.DATABASE_BILL_TIMESTAMP, Query.Direction.DESCENDING)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<BillModel> = FirestoreRecyclerOptions.Builder<BillModel>()
            .setQuery(query, BillModel::class.java)
            .build()

        billAdapter = FirebaseBillAdapter(firestoreRecyclerOptions, this)

        binding.recyclerViewFragmentGroupDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFragmentGroupDetail.adapter = billAdapter
    }

    /**
     * On bill click interface implementation
     *
     * @param billID as bill ID
     */
    override fun onBillClick(billID: String) {
        val action = GroupDetailFragmentDirections.actionGroupDetailFragmentToBillDetailFragment(billID)
        findNavController().navigate(action)
    }

    /**
     * Set UI based on group status
     *
     */
    private fun setUIbasedOnStatus() {
        val status = viewModel.groupModel.value!!.calculated
        when (status) {
            "" -> {
                // Open group
                binding.FABGroupDetail.show()
                manageLockButton(false)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = true
                binding.ImageVeiwLockStatusGroupDetail.isVisible = false
                binding.btnShowResultsGroupDetail.visibility = View.GONE
                binding.tvSummaryGroupDetail.textSize = 20F
                //binding.toolbarGroupDetail.menu.getItem(2).isVisible = true
            }
            Constants.DATABASE_GROUPS_STATUS_LOCKED -> {
                // Locked group
                binding.FABGroupDetail.hide()
                manageLockButton(true)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = false
                binding.ImageVeiwLockStatusGroupDetail.isVisible = true
                binding.btnShowResultsGroupDetail.visibility = View.GONE
                binding.tvSummaryGroupDetail.textSize = 20F
                //binding.toolbarGroupDetail.menu.getItem(2).isVisible = true
            }
            else -> {
                // Calculated group
                binding.FABGroupDetail.hide()
                manageLockButton(true)
                binding.toolbarGroupDetail.menu.getItem(0).isVisible = false
                binding.ImageVeiwLockStatusGroupDetail.isVisible = true
                binding.btnShowResultsGroupDetail.visibility = View.VISIBLE
                binding.tvSummaryGroupDetail.textSize = 14F
                //binding.toolbarGroupDetail.menu.getItem(2).isVisible = false
            }
        }
    }

    /**
     * Manages lock button status and icon
     *
     * @param locked as group lock status
     */
    private fun manageLockButton(locked: Boolean) {
        if (locked) {
            binding.toolbarGroupDetail.menu.getItem(1).setIcon(R.drawable.ic_baseline_lock_open_24)
        } else {
            binding.toolbarGroupDetail.menu.getItem(1).setIcon(R.drawable.ic_baseline_lock_24)
        }
    }

    /**
     * On lock button clicked implementation
     *
     */
    private fun onLockButtonClicked() {
        val status = viewModel.groupModel.value!!.calculated
        when (status) {
            "" -> {
                // Lock group
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.group_detail_lock_group_dialog_title))
                dialog.setMessage(getString(R.string.group_detail_lock_group_dialog_message))
                dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                    db.collection(Constants.DATABASE_GROUPS)
                        .document(viewModel.groupModel.value!!.id)
                        .update(Constants.DATABASE_GROUPS_STATUS, Constants.DATABASE_GROUPS_STATUS_LOCKED)
                }
                dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

                }
                dialog.show()
            }
            Constants.DATABASE_GROUPS_STATUS_LOCKED -> {
                // Unlock group
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.group_detail_unlock_group_dialog_title))
                dialog.setMessage(getString(R.string.group_detail_unlock_group_dialog_message))
                dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                    db.collection(Constants.DATABASE_GROUPS)
                        .document(viewModel.groupModel.value!!.id)
                        .update(Constants.DATABASE_GROUPS_STATUS, "")
                }
                dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

                }
                dialog.show()
            }
            else -> {
                // Unlock group and remove payments
                val dialog = AlertDialog.Builder(requireContext())
                dialog.setTitle(getString(R.string.group_detail_unlock_group_with_payments_dialog_title))
                dialog.setMessage(getString(R.string.group_detail_unlock_group_with_payments_dialog_message))
                dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                    viewModel.unlockCalculatedGroup()
                }
                dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

                }
                dialog.show()
            }
        }
    }

    /**
     * On calculate group debts clicked
     *
     */
    private fun calculateGroupDebts() {
        if(viewModel.groupModel.value!!.calculated == "") {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.group_detail_calculate_group_dialog_title))
            dialog.setMessage(getString(R.string.group_detail_calculate_group_dialog_message))
            dialog.setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, i: Int ->
                viewModel.calculateGroup()
            }
            dialog.setNegativeButton(R.string.No) { dialogInterface: DialogInterface, i: Int ->
                //
            }
            dialog.show()
        } else {
            viewModel.calculateGroup()
        }

    }

    /**
     * Leave group button click
     *
     */
    private fun leaveGroup() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.group_detail_fragment_leave_group_dialog_title))
        dialog.setMessage(getString(R.string.group_detail_fragment_leave_group_dialog_message))
        dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
            viewModel.leaveGroup()
        }
        dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
            //
        }
        dialog.show()
    }

    /**
     * Delete group - deletes current group
     *
     */
    private fun deleteGroup() {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(getString(R.string.group_detail_fragment_delete_group_dialog_title))
        dialog.setMessage(getString(R.string.group_detail_fragment_delete_group_dialog_message))
        dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
            viewModel.deleteGroup()
        }
        dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
            //
        }
        dialog.show()
    }

}