package tech.janhoracek.debtdragon.profile.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentChangeNameBinding
import tech.janhoracek.debtdragon.profile.viewmodels.ChangeNameViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Change name fragment
 *
 * @constructor Create empty Change name fragment
 */
class ChangeNameFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentChangeNameBinding
    val viewModel by viewModels<ChangeNameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentChangeNameBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    ChangeNameViewModel.Event.NameChanged -> {
                        (activity as MainActivity).hideLoading()
                        Toast.makeText(requireContext(), getString(R.string.change_name_fragment_name_changed), Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                    ChangeNameViewModel.Event.ShowLoading -> {(activity as MainActivity).showLoading()}
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }

}