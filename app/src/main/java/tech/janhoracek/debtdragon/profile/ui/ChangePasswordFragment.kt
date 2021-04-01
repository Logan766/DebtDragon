package tech.janhoracek.debtdragon.profile.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentChangePasswordBinding
import tech.janhoracek.debtdragon.profile.viewmodels.ChangePasswordViewModel
import tech.janhoracek.debtdragon.signinguser.LoginActivity
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

class ChangePasswordFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentChangePasswordBinding
    val viewModel by viewModels<ChangePasswordViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        requireActivity().window.statusBarColor = Color.parseColor("#FFFFFF")
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        binding.btnChangePasswordOk.setOnClickListener {
            //viewModel.tryToChangePassword()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewmodel!!.eventsFlow
            .onEach {
                when(it) {
                    ChangePasswordViewModel.Event.ShowLoading -> { (activity as MainActivity).showLoading() }
                    ChangePasswordViewModel.Event.HideLoading -> { (activity as MainActivity).hideLoading()}
                    ChangePasswordViewModel.Event.PasswordChanged -> {
                        (activity as MainActivity).hideLoading()
                        val intentLoginActivity = Intent(activity, LoginActivity::class.java)
                        startActivity(intentLoginActivity)
                        requireActivity().viewModelStore.clear()
                        requireActivity().finish()}
                    is ChangePasswordViewModel.Event.ShowToast -> { Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show() }
                    ChangePasswordViewModel.Event.ReadyToChange -> {
                        val dialog = AlertDialog.Builder(requireContext())
                        dialog.setTitle("Změnit heslo")
                        dialog.setMessage("Po změně hesla budete odhlášeni a bude nutné se znovu přihlásit novým heslem. Přejete si pokračovat?")
                        dialog.setPositiveButton("Ano") { dialogInterface: DialogInterface, i: Int ->
                            Log.d("CAJ", "Na ano!")
                            viewModel.changePassword()
                        }
                        dialog.setNegativeButton("Ne") { dialogInterface: DialogInterface, i: Int ->
                            Log.d("CAJ", "Nope")
                        }
                        dialog.show()
                    }
                }
            }.observeInLifecycle(viewLifecycleOwner)
    }
}