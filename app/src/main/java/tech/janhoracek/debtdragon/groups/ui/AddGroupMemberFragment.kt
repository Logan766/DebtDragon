package tech.janhoracek.debtdragon.groups.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentAddEditDebtBinding
import tech.janhoracek.debtdragon.databinding.FragmentAddGroupMemberBinding
import tech.janhoracek.debtdragon.groups.ui.adapters.AddRemoveGroupMemeberAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class AddGroupMemberFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentAddGroupMemberBinding

    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    override fun onCreate(savedInstanceState: Bundle?) {
        //viewModel.getMembers()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddGroupMemberBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerViewAddGroupMembers.layoutManager = LinearLayoutManager(this.context)


        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            viewModel.getMembers()
        })

        viewModel.friendsToAdd.observe(viewLifecycleOwner, Observer {
            binding.recyclerViewAddGroupMembers.adapter = AddRemoveGroupMemeberAdapter(it)
        })

        binding.FABAddMembers.setOnClickListener {
            val friendsToAdd = binding.recyclerViewAddGroupMembers.adapter as AddRemoveGroupMemeberAdapter
            if (friendsToAdd.checkedFriends.size == 0) {
                Toast.makeText(requireContext(), "Nejsou vybráni žádní uživatelé", Toast.LENGTH_LONG).show()
            } else {
                viewModel.addMembers(friendsToAdd.checkedFriends)
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Uživatelé přidáni", Toast.LENGTH_LONG).show()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addGroupMembersToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

    }


}