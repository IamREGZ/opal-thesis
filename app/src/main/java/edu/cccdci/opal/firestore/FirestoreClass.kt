package edu.cccdci.opal.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.cccdci.opal.dataclasses.City
import edu.cccdci.opal.dataclasses.CityBarangay
import edu.cccdci.opal.dataclasses.Province
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.ui.activities.*
import edu.cccdci.opal.ui.fragments.AddressInfoFragment
import edu.cccdci.opal.ui.fragments.DeleteAccountFragment
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.UtilityClass

class FirestoreClass {

    // Create an instance for Cloud Firestore application
    private val mFSInstance = FirebaseFirestore.getInstance()

    // Function to store registration data to Cloud Firestore
    fun registerUser(activity: RegisterActivity, userInfo: User) {
        // Access the collection named users. If none, Firestore will create it for you.
        mFSInstance.collection(Constants.USERS)
            // Access the document named by user id. If none, Firestore will create it for you.
            .document(userInfo.id)
            // Sets the values in the user id document and merge with the current object
            .set(userInfo, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Prompt the user that he/she is registered
                activity.registerSuccessPrompt()
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog() // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error registering the user.",
                    e
                )
            }  // end of mFSInstance

    }  // end of registerUser method

    // Function to return the current user id. If it is null, return empty string.
    private fun getCurrentUserID(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }  // end of getCurrentUserID method

    // Function to retrieve user information from Cloud Firestore
    fun getUserDetails(activity: Activity) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the user details
                Log.i(activity.javaClass.simpleName, document.toString())

                // Convert the retrieved document to object
                val user = document.toObject(User::class.java)!!

                // Creates the Shared Preferences
                val sharedPrefs = activity.getSharedPreferences(
                    Constants.OPAL_PREFERENCES,
                    Context.MODE_PRIVATE
                )

                // Create the editor for Shared Preferences
                val spEditor: SharedPreferences.Editor = sharedPrefs.edit()

                // Shared Preference for User's Full Name
                spEditor.putString(
                    Constants.SIGNED_IN_FULL_NAME,
                    "${user.firstName} ${user.lastName}"
                ).apply()

                // Shared Preference for User's Username
                spEditor.putString(
                    Constants.SIGNED_IN_USERNAME, user.userName
                ).apply()

                // Shared Preference for User's Profile Picture
                spEditor.putString(
                    Constants.SIGNED_IN_PROFILE_PIC, user.profilePic
                ).apply()

                // Shared Preference for User's Role
                spEditor.putBoolean(
                    Constants.SIGNED_IN_USER_ROLE, user.vendor
                ).apply()

                when (activity) {
                    // In Login Activity, it sends the user to the home activity
                    is LoginActivity -> activity.logInSuccessPrompt()

                    // In Register Activity, it sends the user to the home activity also
                    is RegisterActivity -> activity.firstLogInPrompt()

                    /* In Main Activity, it sets the placeholder values in
                     * the sidebar header to the user's information.
                     */
                    is MainActivity -> activity.setSideNavProfileHeader(
                        sharedPrefs, user
                    )
                }
            }
            // If failed
            .addOnFailureListener { e ->
                /* Closes the loading message in the Login Activity
                 * or Register Activity
                 */
                when (activity) {
                    is LoginActivity -> activity.hideProgressDialog()
                    is RegisterActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error getting the user details.",
                    e
                )
            }  // end of mFSInstance

    }  // end of getUserDetails method

    // Function to update user profile data from Cloud Firestore
    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Update the values of the specified field
            .update(userHashMap)
            // If it is successful
            .addOnSuccessListener {
                when (activity) {
                    // In User Profile Activity, it sends the user to the home page
                    is UserProfileActivity -> activity.userInfoChangedPrompt()

                    // Same goes with Become Vendor Activity
                    is BecomeVendorActivity -> activity.upgradedToVendorPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                /* Closes the loading message in the User Profile Activity
                 * or Become Vendor Activity
                 */
                when (activity) {
                    is UserProfileActivity -> activity.hideProgressDialog()
                    is BecomeVendorActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error updating the user details.",
                    e
                )
            }  // end of mFSInstance

    }  // end of updateUserProfileData method

    // Function to delete user data during account deletion process
    fun deleteUserData(fragment: Fragment, util: UtilityClass) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Delete the entire document
            .delete()
            // If it is successful
            .addOnSuccessListener {
                // In Delete Account Fragment, it sends the user to Login Screen
                if (fragment is DeleteAccountFragment) {
                    fragment.deleteUserAccount()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                // Closes the loading message in the Delete Account Fragment
                if (fragment is DeleteAccountFragment) {
                    util.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    fragment.javaClass.simpleName,
                    "There was an error deleting the user data.",
                    e
                )
            }  // end of mFSInstance

    }  // end of deleteUserData method

    // Function to upload the image to Cloud Storage
    fun uploadImageToCloud(activity: Activity, imageFileURI: Uri?) {
        // Create a file name for the image being uploaded to Cloud Storage
        val sRef: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                Constants.USER_PROFILE_IMAGE_TEMP + System.currentTimeMillis()
                        + "." + Constants.getFileExtension(activity, imageFileURI)
            )

        // Attempts to upload the image file
        sRef.putFile(imageFileURI!!)
            // If it is successful
            .addOnSuccessListener { taskSnapshot ->
                // Log the Firebase Image URL
                Log.e(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                // Get the downloadable image URL
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { uri ->
                        // Log the URL if it is successful
                        Log.e("Downloadable Image URL", uri.toString())

                        /* In the User Profile Activity, the URI of the image
                         * will be stored in the variable mUserProfileImageURL.
                         */
                        if (activity is UserProfileActivity) {
                            activity.imageUploadSuccess(uri.toString())
                        }
                    }
            }
            // If failed
            .addOnFailureListener { exception ->
                // Closes the loading message in the User Profile Activity
                if (activity is UserProfileActivity) {
                    activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            } // end of sRef

    }  // end of uploadImageToCloud method

    // Function to retrieve province list data from Cloud Firestore
    fun getProvinces(fragment: Fragment) {
        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document that serves as container for province data (DOC-PRV)
            .document(Constants.PRV_DOC)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the province list data
                Log.i(fragment.javaClass.simpleName, document.toString())

                // Retrieve the result. If the document is null, return an empty list.
                val provinces = if (document != null)
                    document.toObject(Province::class.java)!!.provinceList
                else
                    listOf()

                when (fragment) {
                    /* In Address Info Fragment, it stores the retrieved
                     * list of Province Data to its respective Spinner
                     */
                    is AddressInfoFragment -> fragment.retrieveProvinces(
                        provinces
                    )
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    fragment.javaClass.simpleName,
                    "There was an error retrieving province data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of getProvinces method

    // Function to retrieve city/municipality list data from Cloud Firestore
    fun getCities(fragment: Fragment, provID: String) {
        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document named by selected province data
            .document(provID)
            // Access the collection named cities
            .collection(Constants.CITY_COL)
            // Access the document that serves as container for city data (DOC-CT)
            .document(Constants.CT_DOC)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the city/municipality list data
                Log.i(fragment.javaClass.simpleName, document.toString())

                // Retrieve the result. If the document is null, return an empty list.
                val cities = if (document != null)
                    document.toObject(City::class.java)!!.cityList
                else
                    listOf()

                when (fragment) {
                    /* In Address Info Fragment, it stores the retrieved
                     * list of City/Municipality Data to its respective
                     * Spinner
                     */
                    is AddressInfoFragment -> fragment.retrieveCities(
                        cities
                    )
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    fragment.javaClass.simpleName,
                    "There was an error retrieving city data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of getCities

    // Function to retrieve barangay list data from Cloud Firestore
    fun getBarangays(
        context: Context, provID: String, cityID: String
    ): List<String> {
        // A return variable that stores list of barangay data
        val barangays: MutableList<String> = mutableListOf()

        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document named by selected province data
            .document(provID)
            // Access the collection named cities
            .collection(Constants.CITY_COL)
            // Access the document named by selected city/municipality data
            .document(cityID)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the retrieved documents
                Log.i(context.javaClass.simpleName, document.toString())

                // Store the barangay list if the result is not null
                if (document != null) {
                    barangays.addAll(
                        document.toObject(CityBarangay::class.java)!!.barangays
                    )
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    context.javaClass.simpleName,
                    "There was an error retrieving barangay data.",
                    e
                )
            }  // end of mFSInstance

        return barangays  // The list value will be returned
    }  // end of getBarangays method

}  // end of FirestoreClass