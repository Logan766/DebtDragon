package tech.janhoracek.debtdragon.signinguser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R

/**
 * Splash screen activity
 *
 * @constructor Create empty Splash screen activity
 */
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) //turn off dark mode

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        checkIfAlreadyLoggedIn()
    }

    /**
     * Check if user is already logged in and reacts on that
     *
     */
    private fun checkIfAlreadyLoggedIn() {
        mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if(user != null) {
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginActivityIntent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
        }, 2050)

    }
}