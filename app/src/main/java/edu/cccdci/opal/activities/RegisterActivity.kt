package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cccdci.opal.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            tvSignin.setOnClickListener {
                startActivity(
                    Intent(this@RegisterActivity, LoginActivity::class.java)
                )
                finish()
            }
        }

    }
}