package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentCreateGroupBinding
import tech.janhoracek.debtdragon.groups.viewmodels.CreateGroupViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment

class CreateGroupFragment : BaseFragment() {

    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentCreateGroupBinding
    val viewModel by viewModels<CreateGroupViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")

        super.onViewCreated(view, savedInstanceState)
    }

}