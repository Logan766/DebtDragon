package tech.janhoracek.debtdragon.signinguser

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.icu.number.NumberFormatter.with
import android.icu.number.NumberRangeFormatter.with
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.ActivityLoginBinding
import tech.janhoracek.debtdragon.localized
import java.io.ByteArrayOutputStream
import java.lang.Error


class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loginViewModel: LoginViewModel

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var loading = MutableLiveData<String>("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_login)

        loginViewModel = ViewModelProviders.of(this)
            .get(LoginViewModel::class.java)

        DataBindingUtil.setContentView<ActivityLoginBinding>(
            this, R.layout.activity_login
        ).apply {
            this.setLifecycleOwner(this@LoginActivity)
            this.viewmodel = loginViewModel
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        btn_google_sign_in.setOnClickListener {
            showLoading()
            Toast.makeText(this, "COVER JE TU", Toast.LENGTH_LONG).show()
            signIn()
            Toast.makeText(this, "COVER JE PRYC", Toast.LENGTH_LONG).show()
        }

        loginViewModel.loginResult.observe(this, Observer { result ->
            showLoading()
            if (result == getString(R.string.log_in_successful)) {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                this.finish()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
            hideLoading()
        })

        /*loading.observe(this, Observer { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })*/

        val callback = onBackPressedDispatcher.addCallback(this) {
            Toast.makeText(applicationContext, "Mackas back", Toast.LENGTH_LONG).show()
        }

        btn_LoginActivity_register.setOnClickListener {
            btn_LoginActivity_register.setTextColor(ContextCompat.getColor(this, R.color.main));
            val registerActivityIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btn_LoginActivity_ForgotPassword.setOnClickListener {
            //loadingCover.visibility = VISIBLE
            /*Firebase.auth.sendPasswordResetEmail("vmjcdzprjudzamdlwr@niwghx.com")
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("MEJL", "Odeslano")
                        }
                    }*/
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
            if (task.isSuccessful) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    val account = task.getResult(ApiException::class.java)!!
                    //TODO Loguj uzivatele jmeno
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Log.w("SignInWithGoogle", "Google sign in failed", e)
                    // ...
                }
            } else {
                Log.w("SignInWithGoogle", exception.toString())

            }

        }
    }

    /*
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { // Přihlášení pomocí googlu proběhlo úspěšně
                    createUserDataInDatabase()
                } else { // Přihlášení pomocí googlu neproběhlo úspěšně
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this,
                        task.exception!!.message.toString(),
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createUserDataInDatabase() {
        val user = HashMap<String, String>()
        user["name"] = auth.currentUser.displayName
        user["email"] = auth.currentUser.email

        Log.d("OBRAZEK", auth.currentUser.photoUrl.toString())
        var userImageURL = auth.currentUser.photoUrl.toString()
        var storageRef = storage.reference
        var photoRef = storageRef.child("images/profile.jpg")

        //val picture = Picasso.get().load(userImageURL).get()

        CoroutineScope(IO).launch {
            val picture = Picasso.get().load(userImageURL).get()
            val baos = ByteArrayOutputStream()
            picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()
            photoRef.putBytes(data).addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("OBRAZEK", "Uspesne nahrano")
                } else {
                    Log.d("OBRAZEK", it.exception!!.message.toString())
                }
            }
        }







        db.collection("Users").document(auth.currentUser.uid).set(user)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.LoginSuccessful), Toast.LENGTH_SHORT).show()
                val intentMainActivity = Intent(this, MainActivity::class.java)
                startActivity(intentMainActivity)
                finish()
                Log.d("TIGER", "Success creating user in database")
            }
            .addOnFailureListener {
                //_registerResult.value = "Registrace neúspěšná"
                //_registerResult.value = localized(R.string.registration_failed)
                Toast.makeText(this, getString(R.string.registration_failed), Toast.LENGTH_SHORT)
                    .show()
                auth.currentUser.delete()
                Log.d("TIGER", "Failure creating user in database")
            }
    }*/

    private fun firebaseAuthWithGoogle(idToken: String) {
        //showLoading()
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Log.d("LADIME", "Jdeme prihlasovat")
        val job = GlobalScope.launch(IO) {
            try {
                auth.signInWithCredential(credential).await()
                Log.d("LADIME", "Ted se prihlasuju pres google")
            } catch (e: FirebaseAuthException) {
                Log.d("LADIME", "Google spadnul")
                withContext(Main) {
                }
                return@launch
            }

            var docRef = db.collection("Users").document(auth.currentUser.uid)
            val doesExists = docRef.get().await()
            if (doesExists.exists()) {
                Log.d("LADIME", "Skipuj vsechno")
                next()
                return@launch
            }

            try {
                saveUserProfilePhotoFromGoogleAuth().await()
                Log.d("LADIME", "Ted nahravam obrazek")
            } catch (e: StorageException) {
                Log.d("LADIME", "Obrazek spadnul")
                //Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG).show()
                auth.currentUser.delete()
                return@launch
            }

            try {
                createUserInDatabase().await()
                Log.d("LADIME", "Ted se vyvarim zaznam v databazi")
            } catch (e: FirebaseFirestoreException) {
                Log.d("LADIME", "Databaze spadla")
                //Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_LONG).show()
                auth.currentUser.delete()
                return@launch
            }
            Log.d("LADIME", "Probehlo to")
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            this@LoginActivity.finish()
        }

        GlobalScope.launch(Main) {
            job.join()
            Log.d("LADIME", "jobDone")
            hideLoading()
        }

    }

    private fun createUserInDatabase(): Task<Void> {
        val user = HashMap<String, String>()
        user["name"] = auth.currentUser.displayName
        user["email"] = auth.currentUser.email
        return db.collection("Users").document(auth.currentUser.uid).set(user)
    }

    private fun saveUserProfilePhotoFromGoogleAuth(): UploadTask {
        var userImageURL = auth.currentUser.photoUrl.toString()
        var storageRef = storage.reference
        var photoRef = storageRef.child("images/" + auth.currentUser.uid + "/profile.jpg")
        val picture = Picasso.get().load(userImageURL).get()
        val baos = ByteArrayOutputStream()
        picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return photoRef.putBytes(data)
    }

    private fun showLoading() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loadingCover.visibility = VISIBLE
    }

    private fun hideLoading() {
        loadingCover.visibility = GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun next() {
        Log.d("LADIME", "Jdeme dal")
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(intent)
        this@LoginActivity.finish()
    }
}