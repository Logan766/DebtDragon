package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentBillDetailBinding
import tech.janhoracek.debtdragon.groups.models.GroupDebtModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseGroupDebtAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants

class BillDetailFragment : BaseFragment(), FirebaseGroupDebtAdapter.onGroupDebtClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private lateinit var binding: FragmentBillDetailBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private var groupDebtAdapter: FirebaseGroupDebtAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.statusBarColor = Color.parseColor("#83173d")
        // Inflate the layout for this fragment
        val args: BillDetailFragmentArgs by navArgs()
        viewModel.getNamesForGroup()
        viewModel.setDataForBillDetail(args.billID)


        binding = FragmentBillDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.billModel.observe(viewLifecycleOwner, Observer {
            setUpRecyclerView()
            groupDebtAdapter!!.startListening()
        })
    }

    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS)
            .document(viewModel.groupModel.value!!.id)
            .collection(Constants.DATABASE_BILL)
            .document(viewModel.billModel.value!!.id)
            .collection(Constants.DATABASE_GROUPDEBT)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<GroupDebtModel> = FirestoreRecyclerOptions.Builder<GroupDebtModel>()
            .setQuery(query, GroupDebtModel::class.java)
            .build()

        groupDebtAdapter = FirebaseGroupDebtAdapter(options, this)

        binding.recyclerViewGroupDebtsBillDetail.layoutManager = LinearLayoutManager(activity)
        binding.recyclerViewGroupDebtsBillDetail.adapter = groupDebtAdapter
    }

    override fun onGroupDebtClick(groupDebtID: String) {
        Log.d("NEDELE", "ID group debt jest: " + groupDebtID)
        /*GlobalScope.launch(IO) {
            db.collection(Constants.DATABASE_GROUPS)
                .document(viewModel.groupModel.value!!.id)
                .collection(Constants.DATABASE_BILL)
                .document(viewModel.billModel.value!!.id)
                .collection(Constants.DATABASE_GROUPDEBT)
                .document(groupDebtID)
                .get().addOnCompleteListener {
                    it.result
                }
        }*/
        val action = BillDetailFragmentDirections.actionBillDetailFragmentToAddGroupDebtFragment(groupDebtID)
        findNavController().navigate(action)
    }

}