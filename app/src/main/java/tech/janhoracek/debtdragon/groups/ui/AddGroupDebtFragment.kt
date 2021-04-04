package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddGroupDebtBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

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
            Log.d("WHO", "Je to null? : " + args.groupDebtID.isNullOrEmpty())
        }
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentAddGroupDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.textInputDebtorAddGroupDebt.doAfterTextChanged {
            val payerID = viewModel.membersAndNames.value!!.find { it.second == binding.textInputDebtorAddGroupDebt.text.toString() }!!.first
            viewModel.setImageForPayer(payerID)
        }

        binding.btnSaveAddGroupDebt.setOnClickListener {
            viewModel.saveGroupDebt(binding.textInputDebtorAddGroupDebt.text.toString())
        }


        return binding.root
    }

}