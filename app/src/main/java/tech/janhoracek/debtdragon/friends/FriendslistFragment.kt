package tech.janhoracek.debtdragon.friends

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_friendslist.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.BaseFragment


class FriendslistFragment : BaseFragment() {

    private val db = FirebaseFirestore.getInstance()
    private var friendsList: List<FriendModel> = ArrayList()
    private val friendslistAdapter: FriendslistAdapter = FriendslistAdapter(friendsList)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friendslist, container, false)
        loadFriends()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView_friendsListFragment.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = friendslistAdapter
        }
    }


    fun loadFriends() {
        db.collection("Users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                friendsList = task.result!!.toObjects(FriendModel::class.java)
                friendslistAdapter.friendsListItems = friendsList
                friendslistAdapter.notifyDataSetChanged()
            } else {
                Log.d("KIWITKO", "Chyba nacitani useru")
            }
        }
    }
}