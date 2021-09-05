package edu.cccdci.opal.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.cccdci.opal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            tvRegister.setOnClickListener {
                //Launch the Register Activity
                startActivity(
                    Intent(this@LoginActivity, RegisterActivity::class.java)
                )
            }
        }

    }
}