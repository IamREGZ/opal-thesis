package edu.cccdci.opal.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.DialogProgressBinding

open class MessageActivity : AppCompatActivity() {

    private lateinit var contentProgDialog: Dialog

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
            //Display a green snackbar if the task is successful
            msgPromptView.setBackgroundColor(
                ContextCompat.getColor(
                    this@MessageActivity,
                    R.color.colorSuccessMessage
                )
            )
        }
        msgPrompt.show() //Shows the snackbar
    }

    //Function to show the loading dialog
    fun showProgressDialog(message: String) {
        val dialogBind = DialogProgressBinding.inflate(layoutInflater)
        contentProgDialog = Dialog(this)

        with(contentProgDialog) {
            setContentView(dialogBind.root)

            dialogBind.tvProgressText.text = message
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            show()
        }
    }

    //Function to hide the loading dialog
    fun hideProgressDialog() {
        contentProgDialog.dismiss()
    }

}