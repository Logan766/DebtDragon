package tech.janhoracek.debtdragon.groups.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        binding.groupResultsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    is GroupDetailViewModel.Event.areAllResolved -> {resetGroup(it.status)}
                    GroupDetailViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                    GroupDetailViewModel.Event.HideLoading -> {(activity as MainActivity).hideLoading()}
                    GroupDetailViewModel.Event.NavigateUp -> {findNavController().navigateUp()}
                }
            }.observeInLifecycle(viewLifecycleOwner)

        setUpRecyclerView()
        paymentAdapter!!.startListening()

        binding.groupResultsToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.reset_group -> {viewModel.checkIfPaymentsAreResolved()}

            }
            true
        }


    }

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

    override fun onCheckboxChange(paymentID: String) {
        Log.d("CTVRTEK", "Id tohohle paymentu jest: " + paymentID)
        viewModel.resolvePayment(paymentID, true)
        //db.collection(Constants.DATABASE_GROUPS).document(viewModel.groupModel.value!!.id).collection(Constants.DATABASE_PAYMENT).document(paymentID).update("resolved", true)
    }

    override fun onAddToFriendsBtnClick(paymentID: String, value: Int, frienshipID: String, creditorID: String) {
        viewModel.resolvePayment(paymentID, true)
        viewModel.createFriendDebt(creditorID, value, frienshipID)
    }

    private fun resetGroup(status: Boolean) {
        if(status) {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Resetovat skpuinu")
            dialog.setMessage("Skupina bude resetována - budou smazány všechny účty a jejich položky. Seznam členů se nezmění. Přejete si pokračovat?")
            dialog.setPositiveButton("Ano") { dialogInterface: DialogInterface, i: Int ->
                viewModel.resetGroup()
            }
            dialog.setNegativeButton("Ne") { dialogInterface: DialogInterface, i: Int ->

            }
            dialog.show()
        } else {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Resetovat skupinu")
            dialog.setMessage("Některé položky rozpočítání dluhů ještě nebyly zaplaceny. Resetováním skupiny budou smazány všechny účty a jejich položky. Seznam členů se nezmění. Přejete si přesto pokračovat?")
            dialog.setPositiveButton("Ano") { dialogInterface: DialogInterface, i: Int ->
                viewModel.resetGroup()
            }
            dialog.setNegativeButton("Ne") { dialogInterface: DialogInterface, i: Int ->

            }
            dialog.show()
        }
    }

}