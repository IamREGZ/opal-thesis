package edu.cccdci.opal.utils

import android.app.Activity
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

/**
 * A class where widgets, dialogs, and other Android utilities are stored.
 */
open class UtilityClass : AppCompatActivity() {

    private lateinit var mProgDialog: Dialog
    private var backPressedOnce = false

    // Function to display the Message SnackBar
    fun showSnackBar(activity: Activity, message: String, error: Boolean) {
        // Decides what color of the SnackBar depending on the message
        val snackBarColor = if (error)
            R.color.colorErrorMessage  // Red color if error
        else
            R.color.colorSuccessMessage  // Green color if successful

        // Prepare the SnackBar
        Snackbar.make(
            activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG
        ).run {
            // Sets the color of the SnackBar
            view.setBackgroundColor(
                ContextCompat.getColor(activity.applicationContext, snackBarColor)
            )

            show()  // Shows the SnackBar
        }  // end of run
    }  // end of showMessagePrompt method

    // Function to show the loading dialog
    fun showProgressDialog(context: Context, activity: Activity, message: String) {
        // Inflate the Dialog Progress Layout
        val dialogBind = DialogProgressBinding.inflate(activity.layoutInflater)
        mProgDialog = Dialog(context)

        // Create the Dialog object
        mProgDialog.apply {
            // Prepares the progress dialog
            setContentView(dialogBind.root)
            dialogBind.tvProgressText.text = message

            // Make the progress dialog non-cancellable
            setCancelable(false)
            setCanceledOnTouchOutside(false)

            show()  // Displays the dialog
        }  // end of apply
    }  // end of showProgressDialog method

    // Function to hide the loading dialog
    fun hideProgressDialog() {
        mProgDialog.dismiss()
    } // end of hideProgressDialog method

    // Function to setup the Action Bar for navigation
    protected fun setupActionBar(tlb: Toolbar, isBlack: Boolean = true) {
        setSupportActionBar(tlb)

        // Customize the navigation icon
        if (supportActionBar != null) {
            // Enables button in the Action Bar
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)

            // If isBlack is true, use black icon. Otherwise, white.
            val backIcon: Int = if (isBlack)
                R.drawable.ic_back_nav_black
            else
                R.drawable.ic_back_nav_white

            // Sets the icon of the back button
            supportActionBar!!.setHomeAsUpIndicator(backIcon)
        }

        // Add functionality to the button
        tlb.setNavigationOnClickListener { onBackPressed() }
    }  // end of setupActionBar method

    // Function to create a Toast message (show for short time by default)
    fun toastMessage(context: Context, msg: String, showLong: Boolean = false) {
        val length = if (showLong)
            Toast.LENGTH_LONG
        else
            Toast.LENGTH_SHORT

        Toast.makeText(context, msg, length).show()
    }  // end of toastMessage method

    // Function to double press back to exit the application
    protected fun doubleBackToExit() {
        // User pressed back the second time
        if (backPressedOnce) {
            super.onBackPressed()
            return  // Exit the function and application
        }

        // When the user pressed back once
        this.backPressedOnce = true

        // Display a Toast message
        toastMessage(
            this, getString(R.string.msg_back_again_to_exit)
        )

        /* If the user has not clicked back twice within 2 seconds,
         * it will not exit.
         */
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }  // end of doubleBackToExit method

}  // end of UtilityClass
