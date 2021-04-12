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
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentBillDetailBinding
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseGroupDebtAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Bill detail fragment
 *
 * @constructor Create empty Bill detail fragment
 */
class BillDetailFragment : BaseFragment(), FirebaseGroupDebtAdapter.onGroupDebtClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding: FragmentBillDetailBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private var groupDebtAdapter: FirebaseGroupDebtAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor = Color.parseColor("#83173d")
        // Inflate the layout for this fragment
        val args: BillDetailFragmentArgs by navArgs()

        // Set data for bill detail
        viewModel.getNamesForGroup()
        viewModel.setDataForBillDetail(args.billID)


        binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.toolbarBillDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    GroupDetailViewModel.Event.BillDataSet -> {
                        viewModel.billModel.observe(viewLifecycleOwner, Observer {
                            setUpRecyclerView()
                            setUIBasedOnStatusAndPriv()
                            groupDebtAdapter!!.startListening()
                        })

                        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
                            setUIBasedOnStatusAndPriv()
                        })
                    }
                    GroupDetailViewModel.Event.ShowLoading -> {
                        (activity as MainActivity).showLoading()
                    }
                    GroupDetailViewModel.Event.HideLoading -> {
                        (activity as MainActivity).hideLoading()
                    }
                    GroupDetailViewModel.Event.GroupDeleted -> {
                        findNavController().navigate(R.id.action_global_groupsFragment)
                        Toast.makeText(requireContext(), getString(R.string.bill_detail_fragment_group_deleted), Toast.LENGTH_LONG).show()
                    }
                    GroupDetailViewModel.Event.BillDeleted -> {
                        findNavController().navigate(BillDetailFragmentDirections.actionGlobalGroupDetailFragment(viewModel.groupModel.value!!.id))
                        Toast.makeText(requireContext(), getString(R.string.bill_detail_fragment_bill_deleted), Toast.LENGTH_LONG).show()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)

        // Set up app bar menu
        binding.toolbarBillDetail.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_bill -> {
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(getString(R.string.bill_detail_fargment_delete_bill_dialog_title))
                    dialog.setMessage(getString(R.string.bill_detail_fragment_delete_bill_dialog_message))
                    dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                        (activity as MainActivity).showLoading()
                        viewModel.deleteBill(viewModel.billModel.value!!.id)
                    }
                    dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
                        //
                    }
                    dialog.show()
                }
            }
            true
        }
    }

    /**
     * Set up recycler view for group debts in bill
     *
     */
    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS)
            .document(viewModel.groupModel.value!!.id)
            .collection(Constants.DATABASE_BILL)
            .document(viewModel.billModel.value!!.id)
            .collection(Constants.DATABASE_GROUPDEBT)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<GroupDebtModel> = FirestoreRecyclerOptions.Builder<GroupDebtModel>()
            .setQuery(query, GroupDebtModel::class.java)
            .build()

        groupDebtAdapter = FirebaseGroupDebtAdapter(options, this)

        binding.recyclerViewGroupDebtsBillDetail.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewGroupDebtsBillDetail.adapter = groupDebtAdapter
    }

    /**
     * On group debt click interface implementation
     *
     * @param groupDebtID as group debt ID
     */
    override fun onGroupDebtClick(groupDebtID: String) {
        if(viewModel.groupModel.value!!.calculated == "") {
            if((viewModel.billModel.value!!.payer == auth.currentUser?.uid) || (viewModel.groupModel.value!!.owner == auth.currentUser?.uid)) {
                val action = BillDetailFragmentDirections.actionBillDetailFragmentToAddGroupDebtFragment(groupDebtID)
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), getString(R.string.bill_detail_gdebt_click_only_admin_owner_can_edit), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.bill_detail_gdebt_click_group_is_locked), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Set UI based on status of the group and administrator privileges
     *
     */
    private fun setUIBasedOnStatusAndPriv() {
        if (viewModel.groupModel.value!!.calculated == "") {
            binding.FABBillDetail.show()
            binding.toolbarBillDetail.menu.getItem(0).isVisible =
                (viewModel.billModel.value!!.payer == auth.currentUser?.uid) || (viewModel.groupModel.value!!.owner == auth.currentUser?.uid)
        } else {
            binding.FABBillDetail.hide()
            binding.toolbarBillDetail.menu.getItem(0).isVisible = false
        }
    }

}