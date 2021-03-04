package tech.janhoracek.debtdragon

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    lateinit var emailInput: String
    lateinit var nameInput: String
    lateinit var password1Input: String
    lateinit var password2Input: String

    private lateinit var mAuth: FirebaseAuth

    val passwordLength: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

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

        btn_RegisterActivity_Register.setOnClickListener {
            emailInput = textInput_RegisterActivity_EmailInput.text.toString()
            nameInput = textInput_RegisterActivity_NameInput.text.toString()
            password1Input = textInput_RegisterActivity_Password1.text.toString()
            password2Input = textInput_RegisterActivity_Password2.text.toString()
            Log.d("VALIK", "Klikas na cudlik")
            Log.d("VALIK", "Email: " + emailInput)
            Log.d("VALIK", "Name: " + nameInput)
            Log.d("VALIK", "Password1: " + password1Input)
            Log.d("VALIK", "Password2: " + password2Input)
            Log.d("VALIK", "Validace hesel stejne: " + isPasswordSame(password1Input,
                password2Input))
            Log.d("VALIK",
                "Delka hesla: " + password1Input.length + "Validace delky: " + isPasswordLongEnough(
                    password1Input))

            textInputLayout_RegisterActivity_Password2.error = null
            textInputLayout_RegisterActivity_Email.error = null
            textInputLayout_RegisterActivity_Name.error = null

            if(validateAllInputs(emailInput, password1Input, password2Input, nameInput)) {
                mAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(emailInput, password1Input)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = task.result!!.user!!
                            val intent = Intent(this@RegisterActivity, MainActivity::class.java)

                            updateProfileName(nameInput)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            intent.putExtra("user_id", firebaseUser.uid)
                            intent.putExtra("email_id", mAuth.currentUser?.email)
                            startActivity(intent)
                            overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out)
                            finish()
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(baseContext, "Chyba",
                                Toast.LENGTH_SHORT).show()
                        }

                    }
            } else {
                Toast.makeText(baseContext, "Neznámá chyba",
                    Toast.LENGTH_SHORT).show()
            }

        }





    }



    //overeni rovnosti hesel
    private fun isPasswordSame(password1: String, password2: String): Boolean {
        return password1 == password2
    }

    //overeni delky hesla
    private fun isPasswordLongEnough(password1: String): Boolean {
        return password1.length >= passwordLength
    }

    //Validace e-mailu
    private fun isValidEmail(email: String): Boolean =
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    //Validace poli uzivatele
    private fun validateAllInputs(email: String, password1: String, password2: String, name: String): Boolean {
        if(name.isEmpty()) {
            Log.d("VALIK", "Jmeno je prazdne")
            textInputLayout_RegisterActivity_Name.error = getString(R.string.type_in_name)
            return false
        }else if (!isValidEmail(email)){
            Log.d("VALIK", "neni validni mail")
            Log.d("VALIK", "mail jest: $email")
            Log.d("VALIK", "Vraci: " + android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            textInputLayout_RegisterActivity_Email.error = getString(R.string.mail_is_not_in_form)
            return false
        } else if (!isPasswordLongEnough(password1)) {
            textInputLayout_RegisterActivity_Password2.error = getString(R.string.passwor_must_have) + passwordLength + getString(
                R.string.number_of_characters)
            Log.d("VALIK", "hesla jsou kratky")
            return false
        } else if (!isPasswordSame(password1, password2)){
            textInputLayout_RegisterActivity_Password2.error = getString(R.string.password_not_same)
            return false
        } else {
            Log.d("VALIK", "je validni!")
            return true
        }
    }

    private fun updateProfileName(name: String) {
        val user = Firebase.auth.currentUser

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name).build()

        user!!.updateProfile(profileUpdates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("JMENO", "Nove hotovo")
            }

        }
    }
}