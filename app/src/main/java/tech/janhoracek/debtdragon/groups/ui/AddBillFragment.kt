package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.DataBinderMapperImpl
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddBillBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Add bill fragment
 *
 * @constructor Create empty Add bill fragment
 */
class AddBillFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentAddBillBinding

    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")
        binding = FragmentAddBillBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Clear data
        binding.textInputPayerAddBill.setText("", false)
        viewModel.billNameToAdd.value = ""
        viewModel.setImageForPayer("")

        // Observe group data and set member names
        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            viewModel.getNamesForGroup()
        })

        // Change picture based on user selected
        binding.textInputPayerAddBill.doAfterTextChanged {
            val payerID = viewModel.membersAndNames.value!!.find { it.second == binding.textInputPayerAddBill.text.toString() }!!.first
            viewModel.setImageForPayer(payerID)
        }

        // Set up create bill button
        binding.btnAddBill.setOnClickListener {
            viewModel.createBill(binding.textInputPayerAddBill.text.toString())
        }

        // Setup app bar back button
        binding.addBillToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Event observer
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    is GroupDetailViewModel.Event.BillCreated -> {
                        val action = AddBillFragmentDirections.actionAddBillFragmentToBillDetailFragment(it.billID)
                        Navigation.findNavController(view).navigate(action)
                    }
                }

            }.observeInLifecycle(viewLifecycleOwner)
    }
}