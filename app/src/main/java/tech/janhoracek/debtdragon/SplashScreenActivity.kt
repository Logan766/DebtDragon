package tech.janhoracek.debtdragon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        checkIfAlreadyLoggedIn()
        //animace here


    }

    /**
     * Kontroluje zda li je uzivatel prihlasen
     * ano - pokracuje na hlavni aktivitu
     * ne - presmerovan na prihlasovani
     */
    private fun checkIfAlreadyLoggedIn() {
        mAuth = FirebaseAuth.getInstance()

        val user = mAuth.currentUser

        Handler(Looper.getMainLooper()).postDelayed({
            if(user != null) {
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
                finish()
            } else {
                val loginActivityIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginActivityIntent)
                finish()
            }
        }, 1000)

    }
}