package tech.janhoracek.debtdragon.signinguser

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_forgotten_password.view.*
import kotlinx.coroutines.*
import tech.janhoracek.debtdragon.MainActivity
import tech.janhoracek.debtdragon.R
import tech.janhoracek.debtdragon.databinding.ActivityLoginBinding


/**
 * Login activity
 *
 * @constructor Create empty Login activity
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val RC_SIGN_IN = 120
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var loginViewModel: LoginViewModel

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginViewModel = ViewModelProviders.of(this)
            .get(LoginViewModel::class.java)

        DataBindingUtil.setContentView<ActivityLoginBinding>(
            this, R.layout.activity_login
        ).apply {
            this.setLifecycleOwner(this@LoginActivity)
            this.viewmodel = loginViewModel
        }

        // Create Google sign in intent
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        auth = FirebaseAuth.getInstance()

        btn_google_sign_in.setOnClickListener {
            showLoading()
            signIn()
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

        val callback = onBackPressedDispatcher.addCallback(this) {
            //
        }

        btn_LoginActivity_register.setOnClickListener {
            btn_LoginActivity_register.setTextColor(ContextCompat.getColor(this, R.color.main));
            val registerActivityIntent = Intent(this, RegisterActivity::class.java)
            startActivity(registerActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btn_LoginActivity_ForgotPassword.setOnClickListener {
            forgotPasswordDialogShow()
        }
    }

    /**
     * Sign in with Google
     *
     */
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Call back from Google sign in
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
                    loginViewModel.firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("SignInWithGoogle", "Google sign in failed", e)
                }
            } else {
                Log.w("SignInWithGoogle", exception.toString())

            }

        }

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val fileUri = data?.data
            //imgProfile.setImageURI(fileUri)

            //You can get File object from intent
            //val file: File = ImagePicker.getFile(data)!!

            //You can also get File Path from intent
            //val filePath:String = ImagePicker.getFilePath(data)!!
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Show loading overlay
     *
     */
    private fun showLoading() {
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        loadingCover.visibility = VISIBLE
    }

    /**
     * Hide loading overlay
     *
     */
    private fun hideLoading() {
        loadingCover.visibility = GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Forgot password dialog show
     *
     */
    private fun forgotPasswordDialogShow() {
        val factory = LayoutInflater.from(this)
        val deleteDialogView: View = factory.inflate(R.layout.dialog_forgotten_password, null)
        val deleteDialog: AlertDialog = AlertDialog.Builder(this).create()
        deleteDialog.setView(deleteDialogView)
        deleteDialogView.btn_cancel_forgotten_password_dialog.setOnClickListener {
            deleteDialog.dismiss()
        }
        deleteDialogView.btn_reset_forgotten_password_dialog.setOnClickListener {
            val email = deleteDialogView.textInputEditText_forgotten_password_dialog.text.toString()
            val validation = loginViewModel.validateResetEmail(email)
            if(validation.first) {
                deleteDialogView.textInputLayout_forgotten_password_dialog.error = validation.second
                loginViewModel.sendResetPassword(email)
                deleteDialog.dismiss()
                Toast.makeText(this, getString(R.string.email_for_password_reset_sent), Toast.LENGTH_LONG).show()
            } else {
                deleteDialogView.textInputLayout_forgotten_password_dialog.error = validation.second
            }
        }

        deleteDialog.show()
    }

}