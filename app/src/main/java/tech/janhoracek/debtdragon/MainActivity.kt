package tech.janhoracek.debtdragon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btMainActivityTestovaci.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intentLoginActivity = Intent(this, LoginActivity::class.java)
            startActivity(intentLoginActivity)
            finish()
        }

        btMainActivityTestovaciDarkMode.setOnClickListener{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

    }
}