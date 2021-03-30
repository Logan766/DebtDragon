package tech.janhoracek.debtdragon.dashboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.ui.adapters.TopCreditorsAdapter
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboradTopCreditorsBinding
import tech.janhoracek.debtdragon.utility.BaseFragment

class DashboradTopCreditors : BaseFragment() {
    private lateinit var binding: FragmentDashboradTopCreditorsBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboradTopCreditorsBinding.inflate(inflater, container, false)
        binding.recyclerViewTopCreditorsDashboard.layoutManager = LinearLayoutManager(requireContext())

        viewModel.topCreditors.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewTopCreditorsDashboard.adapter = TopCreditorsAdapter(it)
        })


        return binding.root
    }

}