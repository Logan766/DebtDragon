package tech.janhoracek.debtdragon.friends

import android.animation.Animator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.dialog_add_friend.*
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.BaseFragment

class AddFriendDialog: BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    //private lateinit var binding: FragmentProfileBinding
    //private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.dialog_add_friend, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_addFriendDialogFragment_cancel.setOnClickListener {
            //Navigation.findNavController(view).navigate(R.id.action_addFriendDialog_to_friendsFragment)
        }

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