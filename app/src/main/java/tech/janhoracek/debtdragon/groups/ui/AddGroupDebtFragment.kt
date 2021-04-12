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
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddGroupDebtBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Add group debt fragment
 *
 * @constructor Create empty Add group debt fragment
 */
class AddGroupDebtFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE

    private lateinit var binding: FragmentAddGroupDebtBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val args: AddGroupDebtFragmentArgs by navArgs()
        if(savedInstanceState == null) {
            viewModel.setDataForAddDebt(args.groupDebtID)
        }
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentAddGroupDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Hide delete button if new group debt is being added
        if (args.groupDebtID != "none") {
            binding.fabDeleteAddGroupDebt.show()
        }

        // Set up delete group debt button
        binding.fabDeleteAddGroupDebt.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.add_edit_group_debt_delete_group_debt))
            dialog.setMessage(getString(R.string.add_edit_group_debt_delete_group_debt_message))
            dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                viewModel.deleteGroupDebt(args.groupDebtID!!)
            }
            dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->

            }
            dialog.show()
        }

        // Text input change listener and setter image for payer
        binding.textInputDebtorAddGroupDebt.doAfterTextChanged {
            if (it.toString() != "") {
                val payerID = viewModel.membersAndNames.value!!.find { it.second == binding.textInputDebtorAddGroupDebt.text.toString() }!!.first
                viewModel.setImageForPayer(payerID)
            } else {
                viewModel.setImageForPayer("")
            }

        }

        // Set up save group debt button
        binding.btnSaveAddGroupDebt.setOnClickListener {
            viewModel.saveGroupDebt(binding.textInputDebtorAddGroupDebt.text.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    GroupDetailViewModel.Event.GroupDebtCreated -> {
                        findNavController().navigateUp()
                    }
                    GroupDetailViewModel.Event.ShowLoading -> {
                        (activity as MainActivity).showLoading()
                    }
                    GroupDetailViewModel.Event.HideLoading -> {
                        (activity as MainActivity).hideLoading()
                    }
                    GroupDetailViewModel.Event.GroupDebtDeleted -> {
                        findNavController().navigateUp()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }


}