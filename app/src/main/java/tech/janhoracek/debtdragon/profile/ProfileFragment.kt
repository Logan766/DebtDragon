package tech.janhoracek.debtdragon.profile

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.navGraphViewModels
import kotlinx.coroutines.cancel
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.FragmentProfileBinding
import tech.janhoracek.debtdragon.signinguser.LoginActivity

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by navGraphViewModels(R.id.app_nav)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        viewModel.logOutStatus.observe(this, Observer { status ->
            if (status == true) {
                val intentLoginActivity = Intent(activity, LoginActivity::class.java)
                requireActivity().finish()
                startActivity(intentLoginActivity)
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        //viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        binding.viewmodel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.statusBarColor = Color.parseColor("#120f38");
    }

    override fun onDestroy() {
        Log.d("PIRAT", "FRAGMENT JE ZNICENEJ!")
        viewModel.onCleared()
        super.onDestroy()
    }

}