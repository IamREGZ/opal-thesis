package edu.cccdci.opal.utils

import android.app.Activity
import android.app.AlertDialog
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
import edu.cccdci.opal.ui.activities.MainActivity
import edu.cccdci.opal.ui.activities.UserProfileActivity

/**
 * A class where widgets, dialogs, and other Android utilities are stored.
 */
open class UtilityClass : AppCompatActivity() {

    private lateinit var mProgDialog: Dialog
    private var backPressedOnce = false

    // Function to display the Message SnackBar
    fun showSnackBar(activity: Activity, message: String, error: Boolean) {
        // Prepare the SnackBar
        val msgPrompt = Snackbar.make(
            activity.findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        val msgPromptView = msgPrompt.view

        // Decides what color of the SnackBar depending on the message
        val snackBarColor = if (error)
            R.color.colorErrorMessage  // Red color if error
        else
            R.color.colorSuccessMessage  // Green color if successful

        // Sets the color of the SnackBar
        msgPromptView.setBackgroundColor(
            ContextCompat.getColor(activity.applicationContext, snackBarColor)
        )

        msgPrompt.show()  // Shows the SnackBar
    }  // end of showMessagePrompt method

    // Function to show the loading dialog
    fun showProgressDialog(context: Context, activity: Activity, message: String) {
        // Inflate the Dialog Progress Layout
        val dialogBind = DialogProgressBinding.inflate(activity.layoutInflater)
        mProgDialog = Dialog(context)  // Create the Dialog object

        with(mProgDialog) {
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
        mProgDialog.dismiss()
    } // end of hideProgressDialog method

    // Function to show the alert dialog
    fun showAlertDialog(
        context: Context, title: String, message: String,
        withCancel: Boolean = false, yesVal: String = "Yes", noVal: String = "No"
    ) {
        // Create a builder object for constructing dialogs
        val builder = AlertDialog.Builder(context)

        builder.setTitle(title)  // Set the title of a dialog
        builder.setMessage(message)  // Set the message of a dialog

        // Sets the actions for positive (yes) value
        builder.setPositiveButton(yesVal) { dialogInterface, _ ->
            when (context) {
                is MainActivity -> context.signOutUser()
                is UserProfileActivity -> context.saveUserInfoChanges()
            }
            dialogInterface.dismiss()
        }

        // Sets the actions for negative (no) value
        builder.setNegativeButton(noVal) { dialogInterface, _ ->
            when (context) {
                is UserProfileActivity -> super.onBackPressed()
            }
            dialogInterface.dismiss()
        }

        // Enable neutral (cancel) button if withCancel parameter is true
        if (withCancel) {
            // Sets the actions for neutral (cancel) value
            builder.setNeutralButton(
                resources.getString(R.string.dialog_cancel)
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
        }

        // Creates the alert dialog
        val alertDialog: AlertDialog = builder.create()

        // To ensure that the dialog will not disappear when clicked outside the dialog
        alertDialog.setCancelable(false)
        alertDialog.show()  // Displays the dialog
    }  // end of showAlertDialog method

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
    fun toastMessage(context: Context, msg: String, showLong: Boolean = false) {
        val length = if (showLong)
            Toast.LENGTH_LONG
        else
            Toast.LENGTH_SHORT

        Toast.makeText(context, msg, length).show()
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
            this,
            resources.getString(R.string.msg_back_again_to_exit)
        )

        /* If the user has not clicked back twice within 2 seconds,
         * it will not exit.
         */
        Handler(Looper.getMainLooper()).postDelayed({
            backPressedOnce = false
        }, 2000)
    }  // end of doubleBackToExit method

}  // end of UtilityClass