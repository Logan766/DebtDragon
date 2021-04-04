package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentBillDetailBinding
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class BillDetailFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE

    private lateinit var binding: FragmentBillDetailBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)


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
        viewModel.getNamesForGroup()
        viewModel.setDataForBillDetail(args.billID)


        binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root
    }

}