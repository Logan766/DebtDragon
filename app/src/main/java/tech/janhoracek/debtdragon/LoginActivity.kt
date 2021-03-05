package tech.janhoracek.debtdragon

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var  googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        mAuth = FirebaseAuth.getInstance()

        btn_google_sign_in.setOnClickListener{
            signIn()
        }

        btn_LoginActivity_SignIn.setOnClickListener {
            val emailInput = textInput_LoginActivity_EmailInput.text.toString()
            val passwordInput = textInput_LoginActivity_password.text.toString()

            textInputLayout_LoginActivity_password.error = null
            textInputLayout_LoginActivity_EmailInput.error = null

            Log.d("LOGUJ", "email jest: " + emailInput)
            Log.d("LOGUJ", "password jest: " + passwordInput)

            if(emailInput.isEmpty()) {
                Log.d("LOGUJ", "prazdny mail")
                textInputLayout_LoginActivity_EmailInput.error = "Zadejte e-mail"
            } else if (!isValidEmail(emailInput)){
                Log.d("LOGUJ", "neni validni mail")
                textInputLayout_LoginActivity_EmailInput.error = getString(R.string.mail_is_not_in_form)
            } else if (passwordInput.isEmpty()){
                Log.d("LOGUJ", "prazdny heslo")
                textInputLayout_LoginActivity_password.error = "Heslo je prázdné"
            } else {
                Log.d("LOGUJ", "je to ok mail")
                signInWithEmailPassword(emailInput, passwordInput, mAuth)
            }

        }

        btn_LoginActivity_register.setOnClickListener {
            btn_LoginActivity_register.setTextColor(ContextCompat.getColor(this, R.color.main));
            val RegisterActivityIntent = Intent(this, RegisterActivity::class.java)
            startActivity(RegisterActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btn_LoginActivity_ForgotPassword.setOnClickListener {
            Firebase.auth.sendPasswordResetEmail("vmjcdzprjudzamdlwr@niwghx.com").addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Log.d("MEJL", "Odeslano")
                }
            }
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful){
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d("SignInWithGoogle", "firebaseAuthWithGoogle:" + account.id)
                    //TODO Loguj uzivatele jmeno
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("SignInWithGoogle", "Google sign in failed", e)
                    // ...
                }
            } else{
                Log.w("SignInWithGoogle", exception.toString())
            }

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) { // Přihlášení pomocí googlu proběhlo úspěšně
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT).show()
                        val intentMainActivity = Intent(this, MainActivity::class.java)
                        startActivity(intentMainActivity)
                        finish()
                    } else { // Přihlášení pomocí googlu neproběhlo úspěšně
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun signInWithEmailPassword(email: String, password: String, mAuth: FirebaseAuth) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) { // Přihlášení pomocí emailu a hesla proběhlo úspěšně
                val firebaseUser = task.result!!.user!!
                Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT)
                    .show()

                // Odstraní activity běžící na pozadí ve stacku, pomocí extra předá user_id a email, přejde na hlavní aktivitu a ukončí tuto aktivitu
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("user_id", mAuth.currentUser!!.uid)
                intent.putExtra("email_id", mAuth.currentUser!!.email)
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()

            } else { // Přihlášení pomocí emailu a hesla neproběhlo úspěšně
                Toast.makeText(this, task.exception!!.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //Validace e-mailu
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}