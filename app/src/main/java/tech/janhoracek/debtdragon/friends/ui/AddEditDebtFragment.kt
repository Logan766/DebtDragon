package tech.janhoracek.debtdragon.friends.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.compose.ui.node.getOrAddAdapter
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.DialogAddFriendBinding
import tech.janhoracek.debtdragon.databinding.FragmentAddEditDebtBinding
import tech.janhoracek.debtdragon.friends.ui.adapters.FirebaseDebtAdapter
import tech.janhoracek.debtdragon.friends.viewmodels.AddEditDebtViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.AddFriendDialogViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle


/**
 * Add edit debt fragment
 *
 * @constructor Create empty Add edit debt fragment
 */
class AddEditDebtFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentAddEditDebtBinding
    private lateinit var viewModel: AddEditDebtViewModel
    private var editStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: AddEditDebtFragmentArgs by navArgs()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")
        val args: AddEditDebtFragmentArgs by navArgs()

        viewModel = ViewModelProvider(this).get(AddEditDebtViewModel::class.java)
        viewModel.setData(args.debtId, args.friendshipData, args.friendName)

        binding = FragmentAddEditDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up app bar
        setTitle(args.debtId)
        setIcons(args.debtId)

        // Set up back button in app bar
        binding.toolbarDebtDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Set up save debt button
        binding.btnSaveAddEditDebtFragment.setOnClickListener {
            (activity as MainActivity).showLoading()
            viewModel.saveToDatabase("Tohle je URL",
                binding.dropdownMenuTextPayerAddEditTask.text.toString(),
                binding.dropdownMenuTextCategoryAddEditTask.text.toString())
        }

        // Set up add debt FAB button
        binding.FABTakePhotoAddEditDebt.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare() //Crop image(Optional), Check Customization for more option
                .compress(1024)    //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Set up event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    AddEditDebtViewModel.Event.NavigateBack -> {
                        (activity as MainActivity).hideLoading()
                        findNavController().navigateUp()
                    }
                    AddEditDebtViewModel.Event.SaveDebt -> {
                    }
                    AddEditDebtViewModel.Event.HideLoading -> {
                        (activity as MainActivity).hideLoading()
                    }
                    AddEditDebtViewModel.Event.Deleted -> {
                        (activity as MainActivity).hideLoading()
                        findNavController().navigateUp()
                    }
                    is AddEditDebtViewModel.Event.SetDropDowns -> {
                        binding.dropdownMenuTextPayerAddEditTask.setText(it.payer, false)
                        binding.dropdownMenuTextCategoryAddEditTask.setText(it.category, false)
                    }
                    //is FriendDetailViewModel.Event.CreateEditDebt -> {}
                }
            }.observeInLifecycle(viewLifecycleOwner)

        // Set up appbar menu
        binding.toolbarDebtDetail.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.edit_debt -> {
                    manageEditButton()
                }
                R.id.delete_debt -> {
                    // Create dialog to delete debt
                    val dialog = AlertDialog.Builder(requireContext())
                    dialog.setTitle(getString(R.string.delete_debt))
                    dialog.setMessage(getString(R.string.delete_debt_message))
                    dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                        (activity as MainActivity).showLoading()
                        viewModel.deleteDebt()
                    }
                    dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
                        //
                    }
                    dialog.show()
                }
            }
            true
        }
    }

    /**
     * Set title of appbar based on new/edit action
     *
     * @param debtId as nullable parameter of debt ID, when null new debt is created otherwise editing action is identified
     */
    private fun setTitle(debtId: String?) {
        if (debtId == null) {
            binding.toolbarDebtDetail.title = getString(R.string.create_new_debt)
        } else {
            binding.toolbarDebtDetail.title = getString(R.string.debt_detail)
        }
    }

    /**
     * Set icons of app bar based on debt ID
     *
     * @param debtId as nullable parameter of debt ID, when null new debt is created otherwise editing action is identified
     */
    private fun setIcons(debtId: String?) {
        if (debtId == null) {
            for (item in 0 until binding.toolbarDebtDetail.menu.size) {
                binding.toolbarDebtDetail.menu.getItem(item).isVisible = false
            }
        } else {
            setEditableFields(false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Callback to Image Picker action
        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {

            // Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            // Sets image to Image View
            binding.debtImageAddEditDebt.setImageURI(fileUri)

            // Get image ready to save to database
            val inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            val byteArray = inputstream!!.readBytes()
            viewModel.debtProfilePhoto.value = byteArray

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).hideLoading()
    }

    /**
     * Manage edit button icon and status
     *
     */
    private fun manageEditButton() {
        editStatus = if(editStatus) {
            binding.toolbarDebtDetail.menu.getItem(0).setIcon(R.drawable.ic_baseline_edit_24)
            setEditableFields(!editStatus)
            false
        } else {
            binding.toolbarDebtDetail.menu.getItem(0).setIcon(R.drawable.ic_baseline_check_24)
            setEditableFields(!editStatus)
            true

        }
    }

    /**
     * Set editable fields based on editing status
     *
     * @param status as status of edit button
     */
    private fun setEditableFields(status: Boolean) {
        binding.textInputNameAddEditDebt.isEnabled = status
        binding.textInputValueAddEditDebt.isEnabled = status
        binding.dropdownMenuTextCategoryAddEditTask.isEnabled = status
        binding.dropdownMenuTextPayerAddEditTask.isEnabled = status
        binding.textInputDescriptionAddEditDebt.isEnabled = status
    }



}