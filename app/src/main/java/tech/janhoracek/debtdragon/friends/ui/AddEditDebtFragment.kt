package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.compose.ui.node.getOrAddAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.DialogAddFriendBinding
import tech.janhoracek.debtdragon.databinding.FragmentAddEditDebtBinding
import tech.janhoracek.debtdragon.friends.viewmodels.AddEditDebtViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.AddFriendDialogViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


class AddEditDebtFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentAddEditDebtBinding
    private lateinit var viewModel: AddEditDebtViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: AddEditDebtFragmentArgs by navArgs()
            viewModel = ViewModelProvider(requireActivity()).get(AddEditDebtViewModel::class.java)
            Log.d("NOC", "Priletel mi model a data jsou:")
            Log.d("NOC", "Member 1:" + args.friendshipData.member1)
            Log.d("NOC", "Member 2:" + args.friendshipData.member2)
            Log.d("NOC", "Friendship UID:" + args.friendshipData.uid)
            Log.d("NOC", "Jmeno kamosa je: " + args.friendName)
            viewModel.setData(args.debtId, args.friendshipData, args.friendName)
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddEditDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val items = listOf("Option 1", "Option 2", "Option 3", "Option 4")

        /*val adapteros = ArrayAdapter(requireContext(), R.layout.list_item, items)
        binding.dropdownMenuTextPayerAddEditTask.setAdapter(adapteros)*/


        binding.btnSaveAddEditDebtFragment.setOnClickListener {
            Log.d("NOC", "Text je: " + binding.dropdownMenuTextPayerAddEditTask.text.toString().isNullOrEmpty())
            binding.dropdownMenuTextPayerAddEditTask.setText("AHOJ", false)
            //binding.dropdownMenuTextPayerAddEditTask.setSelection(2)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}