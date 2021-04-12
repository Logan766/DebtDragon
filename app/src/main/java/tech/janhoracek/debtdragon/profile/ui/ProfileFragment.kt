package tech.janhoracek.debtdragon.profile.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ceylonlabs.imageviewpopup.ImagePopup
import com.github.dhaval2404.imagepicker.ImagePicker
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentProfileBinding
import tech.janhoracek.debtdragon.profile.viewmodels.ProfileViewModel
import tech.janhoracek.debtdragon.signinguser.LoginActivity
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants

/**
 * Profile fragment
 *
 * @constructor Create empty Profile fragment
 */
class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        // Logout button
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

        binding.contactAuthorProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.CONTACT_DEV_LINK))
            startActivity(intent)
        }

        binding.btnAboutApplicationProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.ABOUT_APP_LINK))
            startActivity(intent)
        }

        val imagePopup = ImagePopup(requireContext())
        imagePopup.windowHeight = 800
        imagePopup.windowWidth = 800

        binding.imageViewProfileFragment.setOnClickListener {
            imagePopup.initiatePopup(binding.imageViewProfileFragment.drawable)
            imagePopup.viewPopup()
        }

    }

    // Callback for Image Picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data

            var inputstream = requireContext().contentResolver.openInputStream(fileUri!!)
            var byteArray = inputstream!!.readBytes()
            viewModel.saveProfileImage(byteArray)

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), getString(R.string.Canceled), Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroy() {
        //viewModel.onCleared()
        super.onDestroy()
    }




}