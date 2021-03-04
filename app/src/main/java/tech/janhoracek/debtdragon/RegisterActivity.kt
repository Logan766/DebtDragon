package tech.janhoracek.debtdragon

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        checkBox_RegisterActivity_Terms.setOnCheckedChangeListener { buttonView, isChecked ->
            btn_RegisterActivity_Register.isEnabled = isChecked
        }
        
        
        btn_RegisterActvitiy_AlreadyAccount.setOnClickListener {
            btn_RegisterActvitiy_AlreadyAccount.setTextColor(ContextCompat.getColor(this, R.color.main));
            val loginActivityIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginActivityIntent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}