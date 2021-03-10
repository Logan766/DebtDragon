package tech.janhoracek.debtdragon.signinguser

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private val passwordLength: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_register
        val registerViewModel = ViewModelProviders.of(this)
            .get(RegisterViewModel::class.java)

        DataBindingUtil.setContentView<ActivityRegisterBinding>(
            this, R.layout.activity_register
        ).apply {
            this.setLifecycleOwner(this@RegisterActivity)
            this.viewmodel = registerViewModel
        }

        registerViewModel.registerResult.observe(this, Observer { result ->

            if (result == getString(R.string.registration_succesful)) {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
        })

        checkBox_RegisterActivity_Terms.setOnCheckedChangeListener { buttonView, isChecked ->
            btn_RegisterActivity_Register.isEnabled = isChecked
        }

        btn_RegisterActvitiy_AlreadyAccount.setOnClickListener {
            btn_RegisterActvitiy_AlreadyAccount.setTextColor(ContextCompat.getColor(this,
                R.color.main));
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}