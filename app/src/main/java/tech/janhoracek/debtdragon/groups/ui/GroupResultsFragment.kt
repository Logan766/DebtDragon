package tech.janhoracek.debtdragon.groups.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupResultsBinding
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseResultsAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Group results fragment
 *
 * @constructor Create empty Group results fragment
 */
class GroupResultsFragment : BaseFragment(), FirebaseResultsAdapter.OnCheckboxChangeListener, FirebaseResultsAdapter.OnAddToFriendDebtsListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var binding: FragmentGroupResultsBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private var paymentAdapter: FirebaseResultsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")
        binding = FragmentGroupResultsBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up app bar back button
        binding.groupResultsToolbar.setNavigationOnClickListener {
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
                    is GroupDetailViewModel.Event.AreAllResolved -> {resetGroup(it.status)}
                    GroupDetailViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                    GroupDetailViewModel.Event.HideLoading -> {(activity as MainActivity).hideLoading()}
                    GroupDetailViewModel.Event.NavigateUp -> {findNavController().navigateUp()}
                }
            }.observeInLifecycle(viewLifecycleOwner)

        // Set up recycler view for results
        setUpRecyclerView()
        paymentAdapter!!.startListening()

        // Set up app bar menu
        binding.groupResultsToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.reset_group -> {viewModel.checkIfPaymentsAreResolved()}
            }
            true
        }

        // Set admin options
        viewModel.isCurrentUserOwner.observe(viewLifecycleOwner, Observer { status->
            binding.groupResultsToolbar.menu.getItem(0).isVisible = status
        })

    }

    /**
     * Set up recycler view for group results
     *
     */
    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS)
            .document(viewModel.groupModel.value!!.id)
            .collection(Constants.DATABASE_PAYMENT)

        val options: FirestoreRecyclerOptions<PaymentModel> = FirestoreRecyclerOptions.Builder<PaymentModel>()
            .setQuery(query, PaymentModel::class.java)
            .build()

        paymentAdapter = FirebaseResultsAdapter(options, this, this, viewModel.isCurrentUserOwner.value!! )

        binding.recyclerViewGroupResults.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewGroupResults.adapter = paymentAdapter
    }

    /**
     * On checkbox change interface implementation
     *
     * @param paymentID as payment ID
     */
    override fun onCheckboxChange(paymentID: String) {
        viewModel.resolvePayment(paymentID, true)
    }

    /**
     * On add payment to friends debts button click
     *
     * @param paymentID as payment ID
     * @param value as payment value
     * @param frienshipID as friendship ID
     * @param creditorID as creditor ID
     */
    override fun onAddToFriendsBtnClick(paymentID: String, value: Int, frienshipID: String, creditorID: String) {
        viewModel.resolvePayment(paymentID, true)
        viewModel.createFriendDebt(creditorID, value, frienshipID)
    }

    /**
     * Reset group implementation
     *
     * @param status as all resolved status
     */
    private fun resetGroup(status: Boolean) {
        if(status) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.group_results_reset_group_true_title))
            dialog.setMessage(getString(R.string.group_results_reset_group_true_message))
            dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                viewModel.resetGroup()
            }
            dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

            }
            dialog.show()
        } else {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.group_results_reset_group_false_title))
            dialog.setMessage(getString(R.string.group_results_reset_group_false_message))
            dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                viewModel.resetGroup()
            }
            dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

            }
            dialog.show()
        }
    }

}