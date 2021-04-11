package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import tech.janhoracek.debtdragon.databinding.FragmentFriendslistBinding
import tech.janhoracek.debtdragon.friends.models.FriendModel
import tech.janhoracek.debtdragon.friends.ui.adapters.FirebaseFriendlistAdapter
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants


class FriendslistFragment : BaseFragment() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    //private var friendsList: List<FriendModel> = ArrayList()
    //private val friendslistAdapter: FriendslistAdapter = FriendslistAdapter(friendsList)

    var friendslistAdapter: FirebaseFriendlistAdapter? = null

    private lateinit var binding: FragmentFriendslistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFriendslistBinding.inflate(inflater, container, false)

        // Inflate the layout for this fragment
        //val view = inflater.inflate(R.layout.fragment_friendslist, container, false)
        //loadFriends()
        //return view
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        friendslistAdapter!!.startListening()
        /*recyclerView_friendsListFragment.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = friendslistAdapter
        }*/
    }


    fun loadFriends() {
        /*db.collection("Users").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                friendsList = task.result!!.toObjects(FriendModel::class.java)
                friendslistAdapter.friendsListItems = friendsList
                friendslistAdapter.notifyDataSetChanged()
            } else {
                Log.d("KIWITKO", "Chyba nacitani useru")
            }
        }*/

        /*db.collection("Users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("FRRRR", "Listening failed: " + error)
            }

            if (snapshot != null) {
                friendsList = snapshot.toObjects(FriendModel::class.java)
                friendslistAdapter.friendsListItems = friendsList
                friendslistAdapter.notifyDataSetChanged()
            } else {
                Log.w("FRRRR", "Current data null")
            }

        }*/
    }

    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_FRIENDSHIPS)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<FriendModel> = FirestoreRecyclerOptions.Builder<FriendModel>()
            .setQuery(query, FriendModel::class.java)
            .build()

        friendslistAdapter = FirebaseFriendlistAdapter(firestoreRecyclerOptions)

        binding.recyclerViewFriendsListFragment.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewFriendsListFragment.adapter = friendslistAdapter
    }

    override fun onDestroy() {
        //friendslistAdapter!!.stopListening()
        super.onDestroy()
    }
}