package tech.janhoracek.debtdragon.groups.ui

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
import tech.janhoracek.debtdragon.utility.observeInLifecycle

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
            //Log.d("WHO", "Je to null? : " + args.groupDebtID.isNullOrEmpty())
        }
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentAddGroupDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        ///Vyzkouset nutnost!
        //viewModel.setImageForPayer("")

        /*viewModel.debtorName.observe(viewLifecycleOwner, Observer {
            binding.textInputDebtorAddGroupDebt.setText(it)
        })*/


        binding.textInputDebtorAddGroupDebt.doAfterTextChanged {
            if (it.toString() != "") {
                val payerID = viewModel.membersAndNames.value!!.find { it.second == binding.textInputDebtorAddGroupDebt.text.toString() }!!.first
                viewModel.setImageForPayer(payerID)
            } else {
                Log.d("NEDELE" ,"Ted to triglo nic!")
                viewModel.setImageForPayer("")
            }

        }

        binding.btnSaveAddGroupDebt.setOnClickListener {
            viewModel.saveGroupDebt(binding.textInputDebtorAddGroupDebt.text.toString())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }


}