package tech.janhoracek.debtdragon.friends.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentCreatePaymentBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Create payment fragment
 *
 * @constructor Create empty Create payment fragment
 */
class CreatePaymentFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentCreatePaymentBinding
    val viewModel by navGraphViewModels<FriendDetailViewModel>(R.id.friends)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreatePaymentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        // Clear payment value
        viewModel.createPaymentValue.value = "0"

        // Set slider on change listener to send data to text input
        binding.sliderCreatePaymentFragment.addOnChangeListener {slider, value, fromUser ->
            viewModel.createPaymentValue.value = value.toInt().toString()
        }

        // Set text input listener to send data to slider
        binding.textInputValueCreatePayment.doAfterTextChanged {
            if(!it.isNullOrEmpty()) {
                if( it.toString().toFloat() > 0 && it.toString().toFloat() <= viewModel.maxValueForSlider.value!!) {
                    if(!binding.lottieCreatePaymentFragment.isAnimating) {binding.lottieCreatePaymentFragment.playAnimation()}
                    binding.sliderCreatePaymentFragment.value = it.toString().toFloat()
                } else if (it.toString().toFloat() > viewModel.maxValueForSlider.value!!) {
                    binding.textInputValueCreatePayment.setText(viewModel.maxValueForSlider.value.toString())
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        // Set back button
        binding.btnCancelCreatePaymentFragment.setOnClickListener {
            findNavController().navigateUp()
        }

        // Set up create payment button
        binding.btnCreateCreatePaymentFragment.setOnClickListener {
            (activity as MainActivity).showLoading()
            viewModel.createPaymentClick(binding.sliderCreatePaymentFragment.value)
            // Set slider to 0 (otherwise causing errors
            binding.sliderCreatePaymentFragment.value = 0F
        }

        // Set up event listeners
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
        // Clear error
        viewModel.paymentError.value = ""
    }

}