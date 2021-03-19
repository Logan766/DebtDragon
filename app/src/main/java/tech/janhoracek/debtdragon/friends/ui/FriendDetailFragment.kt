package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_friend_detail.*
import tech.janhoracek.debtdragon.R


class FriendDetailFragment : Fragment() {

    //val args: FriendDetailFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_friend_detail, container, false)





        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val myGreetings = args.hello
        //tv_MyGreetings.text = myGreetings

        btn_FriendDetailFragment_back.setOnClickListener {
            //Navigation.findNavController(view).navigate(R.id.action_friendDetailFragment_to_friendsFragment)
        }
    }

}