package tech.janhoracek.debtdragon.friends

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.dialog_add_friend.*
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import kotlinx.coroutines.flow.onEach
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.DialogAddFriendBinding
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.observeInLifecycle

class AddFriendDialog: BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: DialogAddFriendBinding
    //private lateinit var binding: FragmentProfileBinding
    //private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogAddFriendBinding.inflate(inflater, container, false)
        val viewModel = ViewModelProvider(requireActivity()).get(AddFriendDialogViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        //val view =  inflater.inflate(R.layout.dialog_add_friend, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_addFriendDialogFragment_cancel.setOnClickListener {
            //findNavController().navigateUp()
            binding.viewmodel!!.friendNameContent.value = ""
            binding.viewmodel!!.friendError.value = ""
            Navigation.findNavController(view).navigate(R.id.action_addFriendDialog_to_friendsOverViewFragment)
        }

        binding.viewmodel!!.eventsFlow
            .onEach {
                when (it) {
                    AddFriendDialogViewModel.Event.NavigateBack -> {Navigation.findNavController(view).navigate(R.id.action_addFriendDialog_to_friendsOverViewFragment)}
                }
            }
            .observeInLifecycle(viewLifecycleOwner)

        btn_addFriendDialogFragment_add.setOnClickListener {
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

            })
        }

    }

    override fun onDestroy() {
        Log.d("BANAN", "JSem znicenej")
        super.onDestroy()
    }

}