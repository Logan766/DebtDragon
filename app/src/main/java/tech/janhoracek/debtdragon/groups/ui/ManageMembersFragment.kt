package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

/**
 * Manage members fragment
 *
 * @constructor Create empty Manage members fragment
 */
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

        // Set up app bar back button
        binding.manageMembersToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.recyclerViewManageMember.layoutManager = LinearLayoutManager(this.context)

        // Observe group and set recycler view to manage memebers
        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            val members = it.members
            binding.recyclerViewManageMember.adapter = MembersAdapter(members, viewModel.groupModel.value!!.owner)
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //viewModel.getGroupMembers()
    }


}