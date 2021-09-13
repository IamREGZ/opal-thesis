package edu.cccdci.opal.firestore

import android.app.Activity
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import edu.cccdci.opal.activities.LoginActivity
import edu.cccdci.opal.activities.RegisterActivity
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.utils.Constants

class FirestoreClass {

    //Create an instance for Firestore application
    private val fsInstance = FirebaseFirestore.getInstance()

    //Function to store registration data to Firestore
    fun registerUser(activity: RegisterActivity, userInfo: User) {

        //Access the collection named users. If none, Firestore will create it for you.
        fsInstance.collection(Constants.USERS)
            //Access the document named by user id. If none, Firestore will create it for you.
            .document(userInfo.id)
            //Sets the values in the user id document and merge the placeholder values as well
            .set(userInfo, SetOptions.merge())
            //If it is successful
            .addOnSuccessListener {
                //Prompt the user that he/she is registered
                activity.registerSuccessPrompt()
            }
            //If failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog() //Hide the loading message

                //Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error registering the user.",
                    e
                )
            } //end of fsInstance

    } //end of registerUser method

    //Function to return the current user id. If it is null, return empty string.
    private fun getCurrentUserID(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: ""

    //Function to retrieve user information in the Firestore database
    fun getUserDetails(activity: Activity) {

        //Access the collection named users
        fsInstance.collection(Constants.USERS)
            //Access the document named by the current user id
            .document(getCurrentUserID())
            //Get the field values in the current document
            .get()
            //If it is successful
            .addOnSuccessListener { document ->
                Log.i(activity.javaClass.simpleName, document.toString())

                val user = document.toObject(User::class.java)!!

                //In Login Activity, it sends the user to the home page
                if (activity is LoginActivity) {
                    activity.logInSuccessPrompt(user)
                }
            }
            //If failed
            .addOnFailureListener { e ->
                //Closes the loading message in the Login Activity
                if (activity is LoginActivity) {
                    activity.hideProgressDialog()
                }

                //Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error getting the user details.",
                    e
                )
            } //end of fsInstance

    } //end of getUserDetails method

} //end of FirestoreClass