package edu.cccdci.opal.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.cccdci.opal.R

open class MessageActivity : AppCompatActivity() {

    //Function to display the Message snackbar
    fun showMessagePrompt(message: String, error: Boolean) {
        val msgPrompt = Snackbar.make(
            findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG
        )
        val msgPromptView = msgPrompt.view

        if (error) {
            //Display a red snackbar if there's an error
            msgPromptView.setBackgroundColor(
                ContextCompat.getColor(
                    this@MessageActivity,
                    R.color.colorErrorMessage
                )
            )
        } else {
            //Display a green snackbar if there's an error
            msgPromptView.setBackgroundColor(
                ContextCompat.getColor(
                    this@MessageActivity,
                    R.color.colorSuccessMessage
                )
            )
        }
        msgPrompt.show() //Shows the snackbar
    }

}