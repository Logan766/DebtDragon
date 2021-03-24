package tech.janhoracek.debtdragon.friends.ui

import android.app.Activity
import android.content.Intent
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


class AddEditDebtFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentAddEditDebtBinding
    private lateinit var viewModel: AddEditDebtViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: AddEditDebtFragmentArgs by navArgs()
            /*viewModel = ViewModelProvider(this).get(AddEditDebtViewModel::class.java)
            Log.d("NOC", "Priletel mi model a data jsou:")
            Log.d("NOC", "Member 1:" + args.friendshipData.member1)
            Log.d("NOC", "Member 2:" + args.friendshipData.member2)
            Log.d("NOC", "Friendship UID:" + args.friendshipData.uid)
            Log.d("NOC", "Jmeno kamosa je: " + args.friendName)
            viewModel.setData(args.debtId, args.friendshipData, args.friendName)*/
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: AddEditDebtFragmentArgs by navArgs()

        viewModel = ViewModelProvider(this).get(AddEditDebtViewModel::class.java)
        Log.d("NOC", "Priletel mi model a data jsou:")
        Log.d("NOC", "Member 1:" + args.friendshipData.member1)
        Log.d("NOC", "Member 2:" + args.friendshipData.member2)
        Log.d("NOC", "Friendship UID:" + args.friendshipData.uid)
        Log.d("NOC", "Jmeno kamosa je: " + args.friendName)
        viewModel.setData(args.debtId, args.friendshipData, args.friendName)

        binding = FragmentAddEditDebtBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setTitle(args.debtId)
        setIcons(args.debtId)

        binding.toolbarDebtDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSaveAddEditDebtFragment.setOnClickListener {
            (activity as MainActivity).showLoading()
            viewModel.saveToDatabase("Tohle je URL",
                binding.dropdownMenuTextPayerAddEditTask.text.toString(),
                binding.dropdownMenuTextCategoryAddEditTask.text.toString())

            /*Log.d("NOC", "Text je: " + binding.dropdownMenuTextPayerAddEditTask.text.toString().isNullOrEmpty())
                binding.dropdownMenuTextPayerAddEditTask.setText("AHOJ", false)
                //binding.dropdownMenuTextPayerAddEditTask.setSelection(2)*/
        }

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
                    //is FriendDetailViewModel.Event.CreateEditDebt -> {}
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

    private fun setTitle(debtId: String?) {
        if (debtId == null) {
            binding.toolbarDebtDetail.title = "Založit dluh"
        } else {
            binding.toolbarDebtDetail.title = "Detail dluhu"
        }
    }

    private fun setIcons(debtId: String?) {
        if (debtId == null) {
            for (item in 0 until binding.toolbarDebtDetail.menu.size) {
                binding.toolbarDebtDetail.menu.getItem(item).isVisible = false
            }
        } else {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //zobrazí loading overlay
        //(activity as MainActivity).showLoading()

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {

            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            //nastaví obrá
            binding.debtImageAddEditDebt.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            viewModel.debtProfilePhoto.value = byteArray

//            You can get File object from intent
//            val file:File = ImagePicker.getFile(data)!!
//
//            You can also get File Path from intent
//            val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }

        //skryje loading overlay
        //(activity as MainActivity).hideLoading()
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).hideLoading()
    }



}