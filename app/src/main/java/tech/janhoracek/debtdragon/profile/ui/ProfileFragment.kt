package tech.janhoracek.debtdragon.profile.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.dhaval2404.imagepicker.ImagePicker
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentProfileBinding
import tech.janhoracek.debtdragon.profile.viewmodels.ProfileViewModel
import tech.janhoracek.debtdragon.signinguser.LoginActivity
import tech.janhoracek.debtdragon.utility.BaseFragment

class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel
    //private val viewModel: ProfileViewModel by navGraphViewModels(R.id.profile)
    //private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.logOutStatus.observe(viewLifecycleOwner, Observer { status ->
            if (status == true) {
                val intentLoginActivity = Intent(activity, LoginActivity::class.java)
                startActivity(intentLoginActivity)
                requireActivity().viewModelStore.clear()
                requireActivity().finish()

            }
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#120f38")


        binding.fabChangeProfilePic.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare() //Crop image(Optional), Check Customization for more option
                //.compress(1024)	//Final image size will be less than 1 MB(Optional)
                //.maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            //Image Uri will not be null for RESULT_OK
            Log.d("DOMBY", "Result jest ok")
            val fileUri = data?.data
            //nastaví obrá
            //ci_WarehouseProfileImage_FragmentCreateWarehouse.setImageURI(fileUri)

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            Log.d("DOMBY", "Udelal jsem stream")
            viewModel.saveProfileImage(byteArray)

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

    }


    override fun onDestroy() {
        Log.d("PIRAT", "FRAGMENT JE ZNICENEJ!")
        //viewModel.onCleared()
        super.onDestroy()
    }



}