package tech.janhoracek.debtdragon.friends.ui

import android.os.BaseBundle
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.databinding.FragmentGenerateQRCodeBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.friends.viewmodels.GenerateQRCodeViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


class GenerateQRCodeFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentGenerateQRCodeBinding
    private lateinit var viewModel: GenerateQRCodeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(GenerateQRCodeViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGenerateQRCodeBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvQRTest.setOnClickListener {
            val action = GenerateQRCodeFragmentDirections.actionGenerateQRCodeFragmentToFriendDetailFragment("itLjZnZ9JsZDtSPlJyD9kMxknbF3")
            Navigation.findNavController(view).navigate(action)
        }

    }

}