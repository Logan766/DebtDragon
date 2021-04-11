package tech.janhoracek.debtdragon.dashboard.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.ui.adapters.TopDebtorsAdapter
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboradTopDebtorsBinding
import tech.janhoracek.debtdragon.utility.BaseFragment


/**
 * Dashborad top debtors
 *
 * @constructor Create empty Dashborad top debtors
 */
class DashboradTopDebtors : BaseFragment() {
    private lateinit var binding: FragmentDashboradTopDebtorsBinding
    val viewModel by navGraphViewModels<DashboradViewModel>(R.id.dashborad)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDashboradTopDebtorsBinding.inflate(inflater, container, false)
        binding.recyclerViewTopDebtorsDashboard.layoutManager = LinearLayoutManager(requireContext())

        // Observing data for top debtors
        viewModel.topDebtors.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewTopDebtorsDashboard.adapter = TopDebtorsAdapter(it)
        })

        return binding.root
    }
}