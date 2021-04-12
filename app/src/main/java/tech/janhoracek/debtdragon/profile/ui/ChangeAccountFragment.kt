package tech.janhoracek.debtdragon.profile.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

/**
 * Change account fragment
 *
 * @constructor Create empty Change account fragment
 */
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

        binding.fabDeleteAccount.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle(getString(R.string.change_account_fragment_delete_account_title))
            dialog.setMessage(getString(R.string.change_account_fragment_delete_account_message))
            dialog.setPositiveButton(getString(R.string.yes)) { dialogInterface: DialogInterface, i: Int ->
                viewModel.deleteAccount()
            }
            dialog.setNegativeButton(getString(R.string.No)) { dialogInterface: DialogInterface, i: Int ->
                //
            }
            dialog.show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    ChangeAccountViewModel.Event.AccountChanged -> {
                        Toast.makeText(context, getString(R.string.change_account_fragment_account_added), Toast.LENGTH_LONG).show()
                        findNavController().navigateUp()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }
}