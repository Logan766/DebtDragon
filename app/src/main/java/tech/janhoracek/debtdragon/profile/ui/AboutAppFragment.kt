package tech.janhoracek.debtdragon.profile.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_about_app.view.*
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.utility.Constants

class AboutAppFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_about_app, container, false)

        view.web_view_about_app.loadUrl(Constants.ABOUT_APP_LINK)

        return view
    }

}