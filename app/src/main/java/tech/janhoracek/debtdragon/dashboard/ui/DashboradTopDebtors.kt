package tech.janhoracek.debtdragon.dashboard.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.dashboard.viewmodels.DashboradViewModel
import tech.janhoracek.debtdragon.databinding.FragmentDashboradTopDebtorsBinding
import tech.janhoracek.debtdragon.utility.BaseFragment


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



        return binding.root
    }

    private fun setupRecyclerView() {

    }

}