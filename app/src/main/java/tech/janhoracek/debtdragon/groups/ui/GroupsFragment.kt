package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupsBinding
import tech.janhoracek.debtdragon.friends.models.DebtModel
import tech.janhoracek.debtdragon.groups.models.GroupModel
import tech.janhoracek.debtdragon.groups.ui.adapters.GroupsFirebaseAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupsViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import tech.janhoracek.debtdragon.utility.UserObject


/**
 * Groups fragment
 *
 * @constructor Create empty Groups fragment
 */
class GroupsFragment : BaseFragment(), GroupsFirebaseAdapter.OnGroupClickListener {

    private lateinit var binding: FragmentGroupsBinding
    val viewModel by viewModels<GroupsViewModel>()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var groupAdapter: GroupsFirebaseAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGroupsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        setUpRecyclerView()
        groupAdapter!!.startListening()
    }

    /**
     * Set up recycler view for groups
     *
     */
    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS)
            .whereArrayContains("members", UserObject.uid.toString())
            .orderBy("name")
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<GroupModel> = FirestoreRecyclerOptions.Builder<GroupModel>()
            .setQuery(query, GroupModel::class.java)
            .build()

        groupAdapter = GroupsFirebaseAdapter(firestoreRecyclerOptions, this)
        binding.recyclerViewGroups.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGroups.adapter = groupAdapter
    }

    /**
     * On group click interface implementation
     *
     * @param groupID
     */
    override fun onGroupClick(groupID: String) {
        val action = GroupsFragmentDirections.actionGroupsFragmentToGroupDetailFragment(groupID)
        findNavController().navigate(action)
    }

}