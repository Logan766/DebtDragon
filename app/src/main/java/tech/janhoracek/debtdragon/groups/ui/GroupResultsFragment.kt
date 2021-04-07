package tech.janhoracek.debtdragon.groups.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupResultsBinding
import tech.janhoracek.debtdragon.groups.models.PaymentModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseResultsAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants

class GroupResultsFragment : BaseFragment() {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var binding: FragmentGroupResultsBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private var paymentAdapter: FirebaseResultsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGroupResultsBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.groupResultsToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()
        paymentAdapter!!.startListening()


    }

    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS)
            .document(viewModel.groupModel.value!!.id)
            .collection(Constants.DATABASE_PAYMENT)

        val options: FirestoreRecyclerOptions<PaymentModel> = FirestoreRecyclerOptions.Builder<PaymentModel>()
            .setQuery(query, PaymentModel::class.java)
            .build()

        paymentAdapter = FirebaseResultsAdapter(options)

        binding.recyclerViewGroupResults.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewGroupResults.adapter = paymentAdapter
    }

}