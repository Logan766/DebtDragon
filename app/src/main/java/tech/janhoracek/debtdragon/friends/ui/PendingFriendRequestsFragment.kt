package tech.janhoracek.debtdragon.friends.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_friendslist.*
import kotlinx.android.synthetic.main.fragment_pending_friend_requests.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.friends.models.RequestModel
import tech.janhoracek.debtdragon.friends.ui.adapters.FirebaseRequestAdapter
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants


/**
 * Pending friend requests fragment
 *
 * @constructor Create empty Pending friend requests fragment
 */
class PendingFriendRequestsFragment : BaseFragment() {
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var requestAdapter: FirebaseRequestAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending_friend_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up recycler view
        setUpRecyclerView()
        requestAdapter!!.startListening()

        // Set up FAB button
        floatingActionButton.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_friendsOverViewFragment_to_addFriendDialog)
        }

    }

    /**
     * Set up recycler view for friend requests
     *
     */
    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_USERS).document(auth.currentUser.uid).collection(Constants.DATABASE_REQUESTS)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<RequestModel> = FirestoreRecyclerOptions.Builder<RequestModel>()
            .setQuery(query, RequestModel::class.java)
            .build()

        requestAdapter = FirebaseRequestAdapter(firestoreRecyclerOptions)

        RecyclerView_PendingRequests.layoutManager = LinearLayoutManager(activity)
        RecyclerView_PendingRequests.adapter = requestAdapter
    }


    override fun onDestroy() {
        requestAdapter?.stopListening()
        super.onDestroy()
    }


}