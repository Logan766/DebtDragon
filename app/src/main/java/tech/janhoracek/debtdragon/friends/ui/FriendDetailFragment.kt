package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import tech.janhoracek.debtdragon.databinding.FragmentFriendDetailBinding
import tech.janhoracek.debtdragon.friends.viewmodels.FriendDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment


class FriendDetailFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    private lateinit var binding: FragmentFriendDetailBinding
    private lateinit var viewModel: FriendDetailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val args: FriendDetailFragmentArgs by navArgs()
            viewModel = ViewModelProvider(requireActivity()).get(FriendDetailViewModel::class.java)
            viewModel.setArguments(args.userId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendDetailBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        //val view =  inflater.inflate(R.layout.fragment_friend_detail, container, false)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val args: FriendDetailFragmentArgs by navArgs()
        //binding.tvMyGreetings.text = args.userId

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myView = binding.mainCollapsing
        val appbar = binding.materialupAppbar


        /*
        myView.setTag(myView.getVisibility())
        myView.getViewTreeObserver().addOnGlobalLayoutListener(OnGlobalLayoutListener {
            if (myView.getTag() == View.INVISIBLE) {
                Log.d("TRPOS", "Jsem neviditelnej!")
            } else if(myView.getTag() == View.GONE) {
                Log.d("TRPOS", "Jsem fuc")
            } else {
                Log.d("TRPOS", "Jsem tadyyyy!")
            }
        })*/

        /*btn_FriendDetailFragment_back.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_friendsOverViewFragment)
        }*/
    }

}