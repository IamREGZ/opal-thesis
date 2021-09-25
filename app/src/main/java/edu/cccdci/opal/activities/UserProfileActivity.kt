package edu.cccdci.opal.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityUserProfileBinding
import edu.cccdci.opal.dataclasses.User
import edu.cccdci.opal.utils.Constants
import java.io.IOException

class UserProfileActivity : TemplateActivity(), View.OnClickListener {

    private lateinit var binding: ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            //Setups the Action Bar of the current activity
            setupActionBar(tlbUserProfileActivity)

            var userInfo = User()

            //Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.EXTRA_USER_INFO)) {
                userInfo = intent.getParcelableExtra(Constants.EXTRA_USER_INFO)!!
            }

            //Sets the attributes of each EditText
            with(userInfo) {
                etProfileFname.isEnabled = false
                etProfileFname.setText(firstName)

                etProfileLname.isEnabled = false
                etProfileLname.setText(lastName)

                etProfileEmail.isEnabled = false
                etProfileEmail.setText(emailAdd)

                etProfileUsername.isEnabled = false
                etProfileUsername.setText(userName)
            } //end of with(userInfo)

            //Click event for User Profile Photo ImageView
            ivUserProfilePhoto.setOnClickListener(this@UserProfileActivity)
        } //end of with(binding)

    } //end of onCreate method

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                //Change the user profile photo
                R.id.iv_user_profile_photo -> {
                    //If storage permission access is already granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //Launch the Image Selection
                        Constants.showImageSelection(this@UserProfileActivity)

                    } else {
                        //Request a storage access permission to the device
                        ActivityCompat.requestPermissions(
                            this@UserProfileActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    } //end of if-else

                } //end of R.id.iv_user_profile_photo

            } //end of when

        } //end of if

    } //end of onClick method

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {

            //If the user grants storage permission access
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                //Launch the Image Selection
                Constants.showImageSelection(this@UserProfileActivity)

            } else {
                //If the user denies the permission access
                showMessagePrompt(
                    resources.getString(R.string.err_permission_denied), true
                )

            } //end of if-else

        } //end of if

    } //end of onRequestPermissionsResult method

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.SELECT_IMAGE_REQUEST_CODE
            && data != null
        ) {
            try {
                //Uri of selected image file
                val selectedImageFileURI = data.data!!

                //Sets the ImageView to the selected image file
                binding.ivUserProfilePhoto.setImageURI(selectedImageFileURI)
            } catch (e: IOException) {
                e.printStackTrace()

                //Display an error Toast message
                shortToastMessage(
                    this@UserProfileActivity,
                    resources.getString(R.string.err_image_selection_failed)
                ).show()
            } //end of try-catch

        } //end of if

    } //end of onActivityResult method

} //end of UserProfileActivity class