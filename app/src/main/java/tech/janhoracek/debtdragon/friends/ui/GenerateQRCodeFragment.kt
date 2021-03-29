package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.BaseBundle
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.databinding.FragmentGenerateQRCodeBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.GenerateQRCodeViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle


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

        binding.sliderGenerateQR.addOnChangeListener{slider, value, fromUser ->
            viewModel.generateQRvalue.value = value.toInt().toString()
        }

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

        binding.btnCreateGenerateQRFragment.setOnClickListener {
            Log.d("BTC", "Klikas na ulozit platbu")
            (activity as MainActivity).showLoading()
            viewModel.createPaymentClick(binding.sliderGenerateQR.value)
            binding.sliderGenerateQR.value = 0F
        }

        binding.btnCancelGenerateQRCode.setOnClickListener {
            findNavController().navigateUp()
        }

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

    private fun generateQR(data: String) {
        val dataForQR = viewModel.gatherDataForQR(data)
        val qrCode = viewModel.generateQRCode(dataForQR)
        binding.imageViewQR.setImageBitmap(qrCode)
    }

}