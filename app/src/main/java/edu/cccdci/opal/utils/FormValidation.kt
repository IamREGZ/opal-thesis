package edu.cccdci.opal.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import edu.cccdci.opal.R

class FormValidation(private val activity: Activity) : UtilityClass() {

    private lateinit var message: String  // Stores message for SnackBar

    // Function to validate edit text fields for person's name
    internal fun validatePersonName(editText: EditText): Boolean {
        return when {
            // Person's name must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // First Name from Register Page
                    R.id.et_register_first_name -> activity
                        .getString(R.string.err_blank_first_name)

                    // Last Name from Register Page
                    R.id.et_register_last_name -> activity
                        .getString(R.string.err_blank_last_name)

                    else -> "Invalid person name"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Person's name length must be 2 to 30 characters long
            editText.text.length !in 2..30 -> {
                // Display an error message
                message = when (editText.id) {
                    // First Name from Register Page
                    R.id.et_register_first_name -> activity
                        .getString(R.string.err_first_name_length)

                    // Last Name from Register Page
                    R.id.et_register_last_name -> activity
                        .getString(R.string.err_last_name_length)

                    else -> "Invalid person name"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validatePersonName method

    // Function to validate edit text fields for email address
    internal fun validateEmail(editText: EditText): Boolean {
        return when {
            // Email address must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Email from Login, Register & Forgot Password Page
                    R.id.et_login_email,
                    R.id.et_register_email,
                    R.id.et_recovery_email -> activity
                        .getString(R.string.err_blank_email)

                    else -> "Invalid email address"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Email address must be in a correct format (e.g., abc@xyz.com)
            !Constants.emailValidator(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = when (editText.id) {
                    // Email from Login, Register & Forgot Password Page
                    R.id.et_login_email,
                    R.id.et_register_email,
                    R.id.et_recovery_email -> activity
                        .getString(R.string.err_wrong_email_format)

                    else -> "Invalid email address"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateEmail method

    // Function to validate edit text fields for username
    internal fun validateUsername(editText: EditText): Boolean {
        return when {
            // Username must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Username from Register Page
                    R.id.et_register_user -> activity
                        .getString(R.string.err_blank_username)

                    else -> "Invalid username"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Username must not be "admin" because it is reserved
            editText.text.toString().trim { it <= ' ' } == "admin" -> {
                // Display an error message
                message = when (editText.id) {
                    // Username from Register Page
                    R.id.et_register_user -> activity
                        .getString(R.string.err_admin_username)

                    else -> "Invalid username"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Username length must be 6 to 20 characters long
            editText.text.length !in 6..20 -> {
                // Display an error message
                message = when (editText.id) {
                    // Username from Register Page
                    R.id.et_register_user -> activity
                        .getString(R.string.err_username_length)

                    else -> "Invalid username"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            /* Username must only contain alphanumeric characters (A-Z, a-z or 0-9),
             * and non-consecutive special characters such as underscore (_),
             * period (.), and dash (-)
             */
            !Constants.usernameValidator(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = when (editText.id) {
                    // Username from Register Page
                    R.id.et_register_user -> activity
                        .getString(R.string.err_invalid_username)

                    else -> "Invalid username"  // Default
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateUsername method

    // Function to validate edit text fields for password authentication
    internal fun validateAuthPassword(editText: EditText): Boolean {
        // Password must not be empty
        return if (editText.text.isEmpty()) {
            message = when (editText.id) {
                // Password from Login Page
                R.id.et_login_pass -> activity
                    .getString(R.string.err_blank_password)

                else -> "Invalid password"  // Default
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }  // end of if-else

    }  // end of validateAuthPassword method

    // Function to observe changes in the password field
    internal fun passwordObserver(passField: EditText, indicators: List<ImageView>) {
        // Get the first 4 elements (ImageViews)
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
                    // Password from Register Page
                    R.id.et_register_pass -> activity
                        .getString(R.string.err_blank_password)

                    else -> "Invalid password"  // Default
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
                message = when (editText.id) {
                    // Password from Register Page
                    R.id.et_register_pass -> activity
                        .getString(R.string.err_password_not_strong)

                    else -> "Invalid password"  // Default
                }.also { showSnackBar(activity, it, true) }

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

    // Function to display an error message if the required checkbox is not checked
    internal fun requiredCheckbox(checkBox: CheckBox): Boolean {
        return if (!checkBox.isChecked) {
            message = when (checkBox.id) {
                R.id.cb_terms_and_conditions -> activity
                    .getString(R.string.err_unchecked_tac)

                else -> "Please check all of the required checkboxes."  // Default
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }  // end of if-else

    }  // end of requiredCheckbox method

}  // end of FormValidation class