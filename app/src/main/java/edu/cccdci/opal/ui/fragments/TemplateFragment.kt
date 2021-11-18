package edu.cccdci.opal.ui.fragments

import android.app.Dialog
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.DialogProgressBinding

open class TemplateFragment : Fragment() {

    private lateinit var mDialog: Dialog

    // Function to display the Message SnackBar
    fun showMessagePrompt(message: String, error: Boolean) {
        // Prepare the SnackBar
        val msgPrompt = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
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
            ContextCompat.getColor(requireContext(), snackBarColor)
        )

        msgPrompt.show()  // Shows the SnackBar
    }  // end of showMessagePrompt method

    // Function to show the loading dialog
    fun showProgressDialog(message: String) {
        val dialogBind = DialogProgressBinding.inflate(layoutInflater)
        mDialog = Dialog(requireContext())

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
    }  // end of hideProgressDialog method

    // Function to create a Toast message (show for short time by default)
    fun toastMessage(msg: String, showLong: Boolean = false): Toast {
        return if (showLong)
            Toast.makeText(context, msg, Toast.LENGTH_LONG)
        else
            Toast.makeText(context, msg, Toast.LENGTH_SHORT)
    }  // end of toastMessage method

}  // end of TemplateFragment class