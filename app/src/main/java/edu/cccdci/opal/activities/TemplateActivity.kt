package edu.cccdci.opal.activities

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.DialogProgressBinding

open class TemplateActivity : AppCompatActivity() {

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
                    this@TemplateActivity,
                    R.color.colorErrorMessage
                )
            )
        } else {
            //Display a green snackbar if the task is successful
            msgPromptView.setBackgroundColor(
                ContextCompat.getColor(
                    this@TemplateActivity,
                    R.color.colorSuccessMessage
                )
            )
        }

        msgPrompt.show() //Shows the snackbar
    } //end of showMessagePrompt method

    //Function to show the loading dialog
    fun showProgressDialog(message: String) {
        val dialogBind = DialogProgressBinding.inflate(layoutInflater)
        contentProgDialog = Dialog(this)

        with(contentProgDialog) {

            with(dialogBind) {
                setContentView(root)
                tvProgressText.text = message
            } //end of with(dialogBind)

            setCancelable(false)
            setCanceledOnTouchOutside(false)

            show() //Displays the dialog
        } //end of with(contentProgDialog)

    } //end of showProgressDialog method

    //Function to hide the loading dialog
    fun hideProgressDialog() {
        contentProgDialog.dismiss()
    } //end of hideProgressDialog method

    //Function to setup the Action Bar for navigation
    protected fun setupActionBar(tlb: Toolbar) {
        setSupportActionBar(tlb)
        val actionBar = supportActionBar

        //Customize the navigation icon
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back_nav_black)
        }

        //Add functionality to the button
        tlb.setNavigationOnClickListener { onBackPressed() }
    } //end of setupActionBar method

    //Function to create a Toast Message that displays for a long time
    fun longToastMessage(context: Context, msg: String): Toast =
        Toast.makeText(context, msg, Toast.LENGTH_LONG)

} //end of TemplateActivity class