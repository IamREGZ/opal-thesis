package edu.cccdci.opal.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import edu.cccdci.opal.R

/**
 * A class for validating form fields in different activities/fragments.
 */
class FormValidation(private val activity: Activity) : UtilityClass() {

    private lateinit var message: String  // Stores message for SnackBar

    // Function to validate edit text fields for person's name
    internal fun validatePersonName(editText: EditText): Boolean {
        return when {
            // Person's name must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // First Name from Register & User Profile Editor Activity
                    R.id.et_register_first_name,
                    R.id.et_profile_first_name -> activity
                        .getString(R.string.err_blank_first_name)

                    // Last Name from Register & User Profile Editor Activity
                    R.id.et_register_last_name,
                    R.id.et_profile_last_name -> activity
                        .getString(R.string.err_blank_last_name)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_person_name)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Person's name length must be 2 to 30 characters long
            editText.text.length !in 2..30 -> {
                // Display an error message
                message = when (editText.id) {
                    // First Name from Register & User Profile Editor Activity
                    R.id.et_register_first_name,
                    R.id.et_profile_first_name -> activity
                        .getString(R.string.err_first_name_length)

                    // Last Name from Register & User Profile Editor Activity
                    R.id.et_register_last_name,
                    R.id.et_profile_last_name -> activity
                        .getString(R.string.err_last_name_length)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_person_name)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validatePersonName method

    // Function to validate edit text fields for full person's name
    internal fun validateFullName(editText: EditText): Boolean {
        return when {
            // Full person's name must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = activity.getString(R.string.err_blank_full_name)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Full person's name length must be 3 to 60 characters long
            editText.text.length !in 3..60 -> {
                // Display an error message
                message = activity.getString(R.string.err_full_name_length)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true // All conditions are valid
        }  // end of when

    }  // end of validateFullName method

    // Function to validate edit text fields for email address
    internal fun validateEmail(editText: EditText): Boolean {
        return when {
            // Email address must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = activity.getString(R.string.err_blank_email)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Email address must be in a correct format (e.g., abc@xyz.com)
            !Constants.emailValidator(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = activity.getString(R.string.err_wrong_email_format)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateEmail method

    // Function to validate edit text fields for phone number
    internal fun validatePhoneNumber(editText: EditText): Boolean {
        return when {
            // Phone number must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = activity.getString(R.string.err_blank_phone)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Phone number must be in a correct format (i.e., 09XXXXXXXXX or +63XXXXXXXXX)
            !Constants.phoneNumberValidator(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = activity.getString(R.string.err_wrong_phone_format)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validatePhoneNumber method

    // Function to validate edit text fields for username
    internal fun validateUsername(editText: EditText): Boolean {
        return when {
            // Username must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = activity.getString(R.string.err_blank_username)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Username must not be "admin" because it is reserved
            editText.text.toString().trim { it <= ' ' } == "admin" -> {
                // Display an error message
                message = activity.getString(R.string.err_admin_username)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Username length must be 6 to 20 characters long
            editText.text.length !in 6..20 -> {
                // Display an error message
                message = activity.getString(R.string.err_username_length)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            /* Username must only contain alphanumeric characters (A-Z, a-z or 0-9),
             * and non-consecutive special characters such as underscore (_),
             * period (.), and dash (-)
             */
            !Constants.usernameValidator(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = activity.getString(R.string.err_invalid_username)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateUsername method

    // Function to validate edit text fields for password authentication
    internal fun validateAuthPassword(editText: EditText): Boolean {
        // Password must not be empty
        return if (editText.text.isEmpty()) {
            // Display an error message
            message = if (editText.id == R.id.et_current_pass) {
                // Password field from Change Password Fragment
                activity.getString(R.string.err_blank_current_password)
            } else {
                // Password field from other activities/fragments
                activity.getString(R.string.err_blank_password)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }  // end of if-else

    }  // end of validateAuthPassword method

    // Function to observe changes in the password field
    internal fun passwordObserver(passField: EditText, indicators: List<ImageView>) {
        // Get the first 5 elements (ImageViews)
        indicators.take(5).let {
            /* Make sure the indicators must be equal to 4 in order to make
             * the observer work
             */
            if (it.size == 5) {
                passField.addTextChangedListener(object : TextWatcher {
                    // Events during changes in the text field
                    override fun onTextChanged(
                        text: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                        strongPasswordIndicator(text.toString(), it)
                    }

                    // Events before the text field is changed
                    // NOTE: Empty function, do not add codes
                    override fun beforeTextChanged(
                        text: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    // Events after the text field was changed
                    // NOTE: Empty function, do not add codes
                    override fun afterTextChanged(field: Editable?) {}

                })  // end of addTextChangedListener
            }  // end of if

        }  // end of let

    }  // end of passwordObserver method

    /* Function to change the strong password indicator colors based on its
     * respective requirements
     */
    @SuppressLint("ResourceType")
    private fun strongPasswordIndicator(pass: String, indicators: List<ImageView>) {
        // At least eight (8) characters
        indicators[0].setColorFilter(
            Color.parseColor(
                if (pass.length >= 8)
                    activity.getString(R.color.secondaryColorTheme)
                else
                    activity.getString(R.color.medium_gray)
            )
        )

        // At least one (1) lowercase letter
        indicators[1].setColorFilter(
            Color.parseColor(
                if (pass.matches(Constants.HAS_LOWERCASE.toRegex()))
                    activity.getString(R.color.secondaryColorTheme)
                else
                    activity.getString(R.color.medium_gray)
            )
        )

        // At least one (1) uppercase letter
        indicators[2].setColorFilter(
            Color.parseColor(
                if (pass.matches(Constants.HAS_UPPERCASE.toRegex()))
                    activity.getString(R.color.secondaryColorTheme)
                else
                    activity.getString(R.color.medium_gray)
            )
        )

        // At least one (1) digit
        indicators[3].setColorFilter(
            Color.parseColor(
                if (pass.matches(Constants.HAS_DIGIT.toRegex()))
                    activity.getString(R.color.secondaryColorTheme)
                else
                    activity.getString(R.color.medium_gray)
            )
        )

        // At least one (1) special character
        indicators[4].setColorFilter(
            Color.parseColor(
                if (pass.matches(Constants.HAS_SPECIAL_CHAR.toRegex()))
                    activity.getString(R.color.secondaryColorTheme)
                else
                    activity.getString(R.color.medium_gray)
            )
        )

    }  // end of strongPasswordIndicator method

    // Function to validate edit text fields for setting a password
    internal fun validatePassword(editText: EditText): Boolean {
        return when {
            // Password must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Password from Register Activity
                    R.id.et_register_pass -> activity
                        .getString(R.string.err_blank_password)

                    // Password form Change Password Fragment
                    R.id.et_new_pass -> activity
                        .getString(R.string.err_blank_new_password)

                    // Default error message
                    else -> activity
                        .getString(R.string.unk_err_password)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            /* Password must have at least 8 characters, one digit, one uppercase
             * letter, and one special character
             */
            editText.text.length < 8 || !Constants.strongPassword(
                editText.text.toString().trim { it <= ' ' }
            ) -> {
                // Display an error message
                message = activity.getString(R.string.err_password_not_strong)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validatePassword method

    // Function to validate and confirm matching passwords
    internal fun confirmPassword(pwd1: EditText, pwd2: EditText): Boolean {
        return when {
            // Confirm password field must not be empty
            pwd2.text.isEmpty() -> {
                // Display an error message
                message = activity.getString(R.string.err_blank_confirm_password)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Both passwords must match
            pwd1.text.toString().trim { it <= ' ' } !=
                    pwd2.text.toString().trim { it <= ' ' } -> {
                // Display an error message
                message = activity.getString(R.string.err_passwords_not_match)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of confirmPassword method

    // Function to validate edit text fields for postal code
    internal fun validatePostalCode(editText: EditText): Boolean {
        return when {
            // Postal code must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Postal code from Address Edit Activity
                    R.id.et_addr_postal -> activity.getString(R.string.err_blank_postal)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_postal)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Postal code must have exactly 4 digits
            editText.text.length != 4 -> {
                // Display an error message
                message = activity.getString(R.string.err_postal_length)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validatePostalCode method

    // Function to validate edit text fields for detailed address
    internal fun validateDetailedAddress(editText: EditText): Boolean {
        return when {
            // Detailed address must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Detailed address from Address Edit Activity
                    R.id.et_addr_details -> activity.getString(R.string.err_blank_detailed)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_detailed_address)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Detailed address must be 10 to 100 characters long
            editText.text.length !in 10..100 -> {
                // Display an error message
                message = activity.getString(R.string.err_detailed_length)
                    .also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateDetailedAddress method

    // Function to display an error message if the required checkbox is not checked
    internal fun requiredCheckbox(checkBox: CheckBox): Boolean {
        return if (!checkBox.isChecked) {
            message = when (checkBox.id) {
                // Account Registration T&C Checkbox
                R.id.cb_terms_and_conditions -> activity
                    .getString(R.string.err_unchecked_tac)

                // Default error message
                else -> activity.getString(R.string.unk_err_checkbox)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }  // end of if-else

    }  // end of requiredCheckbox method

    // Function to display an error message if no selection was made for the radio group
    internal fun checkRadioSelection(radioGroup: RadioGroup): Boolean {
        return if (radioGroup.checkedRadioButtonId == -1) {
            message = when (radioGroup.id) {
                // Gender Selection from User Profile Editor Activity
                R.id.rg_profile_gender -> activity
                    .getString(R.string.err_no_gender_selected)

                // Default error message
                else -> activity.getString(R.string.unk_err_radio_group)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }  // end of if-else

    }  // end of checkRadioSelection method

    // Function to display an error message if no selection was made for the ACTV spinner
    internal fun checkSpinnerSelection(actv: AutoCompleteTextView): Boolean {
        return if (actv.text.isEmpty()) {
            message = when (actv.id) {
                // Province from Address Edit Activity
                R.id.actv_addr_province -> activity.getString(R.string.err_blank_province)

                // City/Municipality from Address Edit Activity
                R.id.actv_addr_ctm -> activity.getString(R.string.err_blank_city)

                // Barangay from Address Edit Activity
                R.id.actv_addr_brgy -> activity.getString(R.string.err_blank_brgy)

                // Default error message
                else -> activity.getString(R.string.unk_err_actv_spinner)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true // Valid
        }  // end of if-else

    }  // end of checkSpinnerSelection method

}  // end of FormValidation class
