package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentManageMembersBinding
import tech.janhoracek.debtdragon.groups.ui.adapters.MembersAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class ManageMembersFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentManageMembersBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    var groupMembersAdapter: MembersAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")
        binding = FragmentManageMembersBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.manageMembersToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewManageMember.layoutManager = LinearLayoutManager(requireContext())

        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewManageMember.adapter = MembersAdapter(it.members, viewModel.groupModel.value!!.owner)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getGroupMembers()
    }


}