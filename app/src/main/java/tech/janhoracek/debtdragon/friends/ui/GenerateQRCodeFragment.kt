package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.android.synthetic.main.payment_item.*
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGenerateQRCodeBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle


/**
 * Generate QR code fragment
 *
 * @constructor Create empty Generate q r code fragment
 */
class GenerateQRCodeFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentGenerateQRCodeBinding
    val viewModel by navGraphViewModels<FriendDetailViewModel>(R.id.friends)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGenerateQRCodeBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Set up slider value listener and post it to text input and generate QR
        binding.sliderGenerateQR.addOnChangeListener{slider, value, fromUser ->
            viewModel.generateQRvalue.value = value.toInt().toString()
        }

        // Set up text input listener and post it to slider and generate QR, show/hide save QR button
        binding.textInputValueGenerateQR.doAfterTextChanged {
            if(!it.isNullOrEmpty()) {
                binding.imageViewQR.visibility = View.VISIBLE
                binding.FABSaveQr.show()
                if(it.toString().toFloat() > 0 && it.toString().toFloat() <= viewModel.maxValueForSlider.value!!) {
                    binding.sliderGenerateQR.value = it.toString().toFloat()
                    generateQR(binding.sliderGenerateQR.value.toString())
                } else if(it.toString().toFloat() > viewModel.maxValueForSlider.value!!) {
                    binding.textInputValueGenerateQR.setText(viewModel.maxValueForSlider.value.toString())
                    generateQR(binding.sliderGenerateQR.value.toString())
                } else if(it.toString().toFloat() == 0F) {
                    binding.imageViewQR.visibility = View.INVISIBLE
                    binding.FABSaveQr.hide()
                }
            } else {
                binding.imageViewQR.visibility = View.INVISIBLE
                binding.FABSaveQr.hide()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        viewModel.generateQRvalue.value = (0).toString()

        // Set up generate QR button
        binding.btnCreateGenerateQRFragment.setOnClickListener {
            (activity as MainActivity).showLoading()
            viewModel.createPaymentClick(binding.sliderGenerateQR.value)
            binding.sliderGenerateQR.value = 0F
        }

        // Set up cancel button
        binding.btnCancelGenerateQRCode.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.FABSaveQr.setOnClickListener {
            saveQRtoGallery()
        }

        // Set up event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    FriendDetailViewModel.Event.PaymentCreated -> {
                        (activity as MainActivity).hideLoading()
                        findNavController().navigateUp()
                    }
                    FriendDetailViewModel.Event.HideLoading -> {
                        (activity as MainActivity).hideLoading()
                    }

                }
            }.observeInLifecycle(viewLifecycleOwner)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.paymentError.value = ""
    }

    /**
     * Generate QR
     *
     * @param data as raw string to QR
     */
    private fun generateQR(data: String) {
        val dataForQR = viewModel.gatherDataForQR(data)
        val qrCode = viewModel.generateQRCode(dataForQR)
        binding.imageViewQR.setImageBitmap(qrCode)
    }

    /**
     * Save QR to gallery
     *
     */
    private fun saveQRtoGallery() {
        binding.imageViewQR.isDrawingCacheEnabled = true
        val qr = binding.imageViewQR.getDrawingCache()
        MediaStore.Images.Media.insertImage(requireActivity().contentResolver, qr, "QR", "")
        Toast.makeText(activity, getString(R.string.generate_qr_saved_qr), Toast.LENGTH_SHORT).show()
    }

}