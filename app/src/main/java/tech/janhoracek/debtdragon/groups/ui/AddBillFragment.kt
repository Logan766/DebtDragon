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
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.DataBinderMapperImpl
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddBillBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

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

        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            viewModel.getNamesForGroup()
        })

        binding.textInputPayerAddBill.doAfterTextChanged {
            val payerID = viewModel.membersAndNames.value!!.find { it.second == binding.textInputPayerAddBill.text.toString() }!!.first
            viewModel.setImageForPayer(payerID)
        }

        binding.btnAddBill.setOnClickListener {
            viewModel.createBill(binding.textInputPayerAddBill.text.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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