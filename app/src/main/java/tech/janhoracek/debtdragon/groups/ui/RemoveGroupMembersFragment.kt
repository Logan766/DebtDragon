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
import com.google.firebase.auth.FirebaseAuth
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentRemoveGroupMembersBinding
import tech.janhoracek.debtdragon.groups.ui.adapters.AddRemoveGroupMemeberAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class RemoveGroupMembersFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentRemoveGroupMembersBinding

    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("PICA", "Na remove je velikost groupy: " + viewModel.groupModel.value!!.members.size)
        // Inflate the layout for this fragment
        binding = FragmentRemoveGroupMembersBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerViewRemoveGroupMembers.layoutManager = LinearLayoutManager(this.context)

        viewModel.groupModel.observe(viewLifecycleOwner, Observer {
            val memberWithoutMe = it.members.toMutableList()
            memberWithoutMe.remove(auth.currentUser.uid)
            val adapter = AddRemoveGroupMemeberAdapter(memberWithoutMe)
            binding.recyclerViewRemoveGroupMembers.adapter = adapter
        })

        binding.FABRemoveMembers.setOnClickListener {
            val membersToRemove = binding.recyclerViewRemoveGroupMembers.adapter as AddRemoveGroupMemeberAdapter
            if(membersToRemove.checkedFriends.size == 0) {
                Toast.makeText(requireContext(), "Nejsou vybráni žádní uživatelé", Toast.LENGTH_LONG).show()
            } else {
                viewModel.removeMembers(membersToRemove.checkedFriends)
                findNavController().navigateUp()
                Toast.makeText(requireContext(), "Uživatelé odebráni", Toast.LENGTH_LONG).show()
            }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.removeGroupMembersToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

}