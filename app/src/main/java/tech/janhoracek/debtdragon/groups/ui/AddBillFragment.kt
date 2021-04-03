package tech.janhoracek.debtdragon.groups.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import tech.janhoracek.debtdragon.DataBinderMapperImpl
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddBillBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

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

        return binding.root
    }
}