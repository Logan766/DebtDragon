package tech.janhoracek.debtdragon.groups.ui

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.marginTop
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentGroupDetailBinding
import tech.janhoracek.debtdragon.groups.models.BillModel
import tech.janhoracek.debtdragon.groups.ui.adapters.FirebaseBillAdapter
import tech.janhoracek.debtdragon.groups.viewmodels.GroupDetailViewModel
import tech.janhoracek.debtdragon.utility.BaseFragment
import tech.janhoracek.debtdragon.utility.Constants
import kotlin.math.abs

class GroupDetailFragment : BaseFragment(), FirebaseBillAdapter.OnBillClickListener {
    override var bottomNavigationViewVisibility = View.GONE
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding: FragmentGroupDetailBinding
    val viewModel by navGraphViewModels<GroupDetailViewModel>(R.id.groups)

    private lateinit var appBarLayout: AppBarLayout
    private lateinit var groupAvatar: ImageView

    private var avatar_normalwidth = 0
    private var avatar_normalHeight = 0
    private var avatar_normalTopMargin = 0

    var offsetStatus = true

    private var billAdapter: FirebaseBillAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: GroupDetailFragmentArgs by navArgs()
        if(savedInstanceState == null) {
            viewModel.setData(args.groupID)
        }

        binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        appBarLayout = binding.appbarGroupdetail
        groupAvatar = binding.profileImageGroupDetail

        avatar_normalwidth = groupAvatar.layoutParams.width
        avatar_normalHeight = groupAvatar.layoutParams.height
        avatar_normalTopMargin = groupAvatar.marginTop

        appBarLayout.setExpanded(offsetStatus, false)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val offset = abs(verticalOffset / appBarLayout.totalScrollRange.toFloat())
            updateUI(offset)
        })

        binding.lottieArrowUpGroupDetail.setOnClickListener {
            appBarLayout.setExpanded(false, true)
        }

        binding.toolbarGroupDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.groupModel.observe(viewLifecycleOwner, Observer { groupData->
            setUpRecyclerView()
            billAdapter!!.startListening()
        })


        binding.toolbarGroupDetail.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.manage_members -> {
                    Navigation.findNavController(view).navigate(R.id.action_groupDetailFragment_to_manageMembersFragment)
                }
            }
            true
        }

        viewModel.isCurrentUserOwner.observe(viewLifecycleOwner, Observer { status->
            setOwnerOptions()
        })
    }

    private fun updateUI(offset: Float) {
        groupAvatar.apply {
            this.layoutParams.also {
                it.height = (avatar_normalHeight - (offset * 150)).toInt()
                it.width = (avatar_normalwidth - (offset * 150)).toInt()
                this.layoutParams = it
            }
        }
        if (offset > 0.5) {
            requireActivity().window.statusBarColor = Color.parseColor("#120f38")
            binding.lottieArrowUpGroupDetail.visibility = View.INVISIBLE
            binding.btnBackBottomGroupDetail.visibility = View.VISIBLE
            binding.membersBottomGroupDetail.visibility = View.VISIBLE
            offsetStatus = false
        } else {
            requireActivity().window.statusBarColor = Color.parseColor("#83173d")
            binding.lottieArrowUpGroupDetail.visibility = View.VISIBLE
            binding.btnBackBottomGroupDetail.visibility = View.GONE
            binding.membersBottomGroupDetail.visibility = View.GONE
            offsetStatus = true
        }
    }

    private fun setOwnerOptions() {
        binding.toolbarGroupDetail.menu.getItem(1).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(2).isVisible = viewModel.isCurrentUserOwner.value!!
        binding.toolbarGroupDetail.menu.getItem(3).isVisible = viewModel.isCurrentUserOwner.value!!
    }

    private fun setUpRecyclerView() {
        val query = db.collection(Constants.DATABASE_GROUPS).document(viewModel.groupModel.value!!.id).collection(Constants.DATABASE_BILL).orderBy(Constants.DATABASE_BILL_TIMESTAMP, Query.Direction.DESCENDING)
        val firestoreRecyclerOptions: FirestoreRecyclerOptions<BillModel> = FirestoreRecyclerOptions.Builder<BillModel>()
            .setQuery(query, BillModel::class.java)
            .build()

        billAdapter = FirebaseBillAdapter(firestoreRecyclerOptions, this)

        binding.recyclerViewFragmentGroupDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFragmentGroupDetail.adapter = billAdapter
    }

    override fun onDebtClick(billID: String) {
        Log.d("BILL", "ID tohoto uctu jest: " + billID)
    }

}