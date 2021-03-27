package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentCreatePaymentBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class CreatePaymentFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentCreatePaymentBinding
    val viewModel by viewModels<FriendDetailViewModel>({requireParentFragment()})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreatePaymentBinding.inflate(inflater, container, false)

        binding.sliderCreatePaymentFragment.valueTo = 300F

        binding.sliderCreatePaymentFragment.value

        binding.btnCreateCreatePaymentFragment.setOnClickListener {
            Log.d("HILL", "Slider value jest: " + binding.sliderCreatePaymentFragment.value)
            Log.d("HILL", "testovaci value jest: " + viewModel.testovaci.value)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancelCreatePaymentFragment.setOnClickListener {
            findNavController().navigateUp()
        }
    }

}