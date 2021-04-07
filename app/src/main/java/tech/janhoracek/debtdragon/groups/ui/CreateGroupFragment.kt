package tech.janhoracek.debtdragon.groups.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentCreateGroupBinding
import tech.janhoracek.debtdragon.groups.viewmodels.CreateGroupViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

class CreateGroupFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentCreateGroupBinding
    val viewModel by viewModels<CreateGroupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val args: CreateGroupFragmentArgs by navArgs()

        binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        viewModel.setData(args.groupData)

        binding.FABTakePhotoCreateGroup.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.toolbarCreateGroup.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    CreateGroupViewModel.Event.NavigateBack -> {findNavController().navigateUp()}
                    is CreateGroupViewModel.Event.GroupCreated -> {findNavController().navigate(CreateGroupFragmentDirections.actionCreateGroupFragmentToGroupDetailFragment(it.groupID))}
                    CreateGroupViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                    CreateGroupViewModel.Event.HideLoading -> {(activity as MainActivity).hideLoading()}
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //zobrazí loading overlay
        //(activity as MainActivity).showLoading()

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {

            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            //nastaví obrázek
            binding.groupImageCreateGroup.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            viewModel.groupProfilePhoto.value = byteArray

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }
    }

}