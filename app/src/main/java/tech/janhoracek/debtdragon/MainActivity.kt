package tech.janhoracek.debtdragon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Handler

private lateinit var mAuth: FirebaseAuth
private lateinit var username: String

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

      val bottomNavigationView =  bottomNavigationViewMain //bottomNavigationViewMain
      val navController = findNavController(R.id.fragment)


      bottomNavigationView.setupWithNavController(navController)
    }
}