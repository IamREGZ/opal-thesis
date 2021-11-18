package edu.cccdci.opal.ui.activities

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.DialogProgressBinding

open class TemplateActivity : AppCompatActivity() {

    private lateinit var mDialog: Dialog
    private var backPressedOnce = false

    // Function to display the Message SnackBar
    fun showMessagePrompt(message: String, error: Boolean) {
        // Prepare the SnackBar
        val msgPrompt = Snackbar.make(
            findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG
        )
        val msgPromptView = msgPrompt.view

        // Decides what color of the SnackBar depending on the message
        val snackBarColor = if (error)
            R.color.colorErrorMessage  // Red color if error
        else
            R.color.colorSuccessMessage  // Green color if successful

        // Sets the color of the SnackBar
        msgPromptView.setBackgroundColor(
            ContextCompat.getColor(this@TemplateActivity, snackBarColor)
        )

        msgPrompt.show()  // Shows the SnackBar
    }  // end of showMessagePrompt method

    // Function to show the loading dialog
    fun showProgressDialog(message: String) {
        val dialogBind = DialogProgressBinding.inflate(layoutInflater)
        mDialog = Dialog(this)

        with(mDialog) {
            // Prepares the progress dialog
            setContentView(dialogBind.root)
            dialogBind.tvProgressText.text = message

            // Make the progress dialog non-cancellable
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            show()  // Displays the dialog
        }  // end of with(mDialog)
    }  // end of showProgressDialog method

    // Function to hide the loading dialog
    fun hideProgressDialog() {
        mDialog.dismiss()
    } // end of hideProgressDialog method

    // Function to setup the Action Bar for navigation
    protected fun setupActionBar(tlb: Toolbar, isBlack: Boolean = true) {
        setSupportActionBar(tlb)

        // Customize the navigation icon
        if (supportActionBar != null) {
            // Enables button in the Action Bar
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            // If isBlack is true, use black icon. Otherwise, white.
            val backIcon: Int = if (isBlack) {
                R.drawable.ic_back_nav_black
            } else {
                R.drawable.ic_back_nav_white
            }

            // Sets the icon of the back button
            supportActionBar!!.setHomeAsUpIndicator(backIcon)
        }

        // Add functionality to the button
        tlb.setNavigationOnClickListener { onBackPressed() }
    }  // end of setupActionBar method

    // Function to create a Toast message (show for short time by default)
    fun toastMessage(context: Context, msg: String, showLong: Boolean = false): Toast {
        return if (showLong)
            Toast.makeText(context, msg, Toast.LENGTH_LONG)
        else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
    }  // end of toastMessage method

    // Function to double press back to exit the application
    fun doubleBackToExit() {
        // User pressed back the second time
        if (backPressedOnce) {
            super.onBackPressed()
            return  // exit the function and application
        }

        // When the user pressed back once
        this.backPressedOnce = true

        // Display a Toast message
        toastMessage(
            this, resources.getString(R.string.msg_back_again_to_exit)
        ).show()

        /* If the user has not clicked back twice within 2 seconds,
         * it will not exit.
         */
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }  // end of doubleBackToExit method

}  // end of TemplateActivity class