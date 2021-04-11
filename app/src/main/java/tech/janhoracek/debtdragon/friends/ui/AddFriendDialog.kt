package tech.janhoracek.debtdragon.friends.ui

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.dialog_add_friend.*
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.DialogAddFriendBinding
import tech.janhoracek.debtdragon.friends.viewmodels.AddFriendDialogViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

/**
 * Add friend dialog
 *
 * @constructor Create empty Add friend dialog
 */
class AddFriendDialog: BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: DialogAddFriendBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddFriendBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(requireActivity()).get(AddFriendDialogViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up cancel button
        btn_addFriendDialogFragment_cancel.setOnClickListener {
            binding.viewmodel!!.friendNameContent.value = ""
            binding.viewmodel!!.friendError.value = ""
            Navigation.findNavController(view).navigate(R.id.action_addFriendDialog_to_friendsOverViewFragment)
        }

        // Set up event listener
        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    AddFriendDialogViewModel.Event.NavigateBack -> {
                        binding.textInputLayoutAddFriendFragment.clearFocus()
                        Toast.makeText(requireContext(), getString(R.string.sent), Toast.LENGTH_LONG).show()
                        lottie_addFriendDialog.playAnimation()
                        lottie_addFriendDialog.addAnimatorListener(object: Animator.AnimatorListener{
                            override fun onAnimationStart(animation: Animator?) {
                                //
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                Navigation.findNavController(view).navigate(R.id.action_addFriendDialog_to_friendsOverViewFragment)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                                //
                            }

                            override fun onAnimationRepeat(animation: Animator?) {
                                //
                            }

                        })}
                }
            }
            .observeInLifecycle(viewLifecycleOwner)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}