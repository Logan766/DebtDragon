package tech.janhoracek.debtdragon.profile.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentChangeAccountBinding
import tech.janhoracek.debtdragon.profile.viewmodels.ChangeAccountViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

class ChangeAccountFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentChangeAccountBinding
    val viewModel by viewModels<ChangeAccountViewModel>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentChangeAccountBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    ChangeAccountViewModel.Event.AccountChanged -> {
                        Toast.makeText(context, "Účet uložen", Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }
}