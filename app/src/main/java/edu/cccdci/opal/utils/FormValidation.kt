package edu.cccdci.opal.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.core.text.isDigitsOnly
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.*

/**
 * A class for validating form fields in different activities/fragments.
 */
class FormValidation(private val activity: Activity) : UtilityClass() {

    private lateinit var message: String  // Stores message for SnackBar

    // Function to validate edit text fields for names (person, market & product)
    internal fun validateName(editText: EditText): Boolean {
        // Valid character count, depending on activity origin
        val validRange = when (activity) {
            is RegisterActivity,
            is UserProfileActivity -> 2..30
            is AddressEditActivity -> 3..60
            is MarketEditorActivity -> 5..50
            is ProductEditorActivity -> 4..50
            else -> 0..0
        }

        return when {
            // Name must not be empty
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

                    // Full Name from Address Editor Activity
                    R.id.et_addr_full_name -> activity
                        .getString(R.string.err_blank_full_name)

                    // Market Name from Market Editor Activity
                    R.id.et_mkt_edit_name -> activity
                        .getString(R.string.err_blank_market_name)
                    // Parent Wet Market from Market Editor Activity
                    R.id.et_mkt_edit_wet_mkt -> activity
                        .getString(R.string.err_blank_wet_market)

                    // Product Name from Product Editor Activity
                    R.id.et_prod_edit_name -> activity
                        .getString(R.string.err_blank_product_name)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_name)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Name length must be within valid range, depending on the activity
            editText.text.length !in validRange -> {
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

                    // Full Name from Address Editor Activity
                    R.id.et_addr_full_name -> activity
                        .getString(R.string.err_full_name_length)

                    // Market Name from Market Editor Activity
                    R.id.et_mkt_edit_name -> activity
                        .getString(R.string.err_market_name_length)
                    // Parent Wet Market from Market Editor Activity
                    R.id.et_mkt_edit_wet_mkt -> activity
                        .getString(R.string.err_wet_market_length)

                    // Product Name from Product Editor Activity
                    R.id.et_prod_edit_name -> activity
                        .getString(R.string.err_product_name_length)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_name)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateName method

    // Function to validate edit text fields for long texts (Text Areas)
    internal fun validateLongTexts(editText: EditText): Boolean {
        // Valid character count, depending on activity origin
        val validRange = when (activity) {
            is AddressEditActivity,
            is MarketEditorActivity -> 10..100
            is ProductEditorActivity -> 10..400
            else -> 0..0
        }

        return when {
            // Long text must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Detailed Address from Address Edit Activity
                    R.id.et_addr_details -> activity.getString(R.string.err_blank_detailed)

                    // Detailed Address from Market Editor Activity
                    R.id.et_mkt_edit_details -> activity
                        .getString(R.string.err_blank_mkt_detailed)

                    // Product Description from Product Editor Activity
                    R.id.et_prod_edit_desc -> activity
                        .getString(R.string.err_blank_product_desc)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_long_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Long text must be 10 to 100 characters long
            editText.text.length !in validRange -> {
                // Display an error message
                message = when (editText.id) {
                    // Detailed Address from Address Edit or Market Editor Activity
                    R.id.et_addr_details,
                    R.id.et_mkt_edit_details -> activity
                        .getString(R.string.err_detailed_length)

                    // Product Description from Product Editor Activity
                    R.id.et_prod_edit_desc -> activity
                        .getString(R.string.err_product_desc_length)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_long_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All conditions are valid
        }  // end of when

    }  // end of validateLongTexts method

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

                    // Postal code from Market Editor Activity
                    R.id.et_mkt_edit_postal -> activity
                        .getString(R.string.err_blank_mkt_postal)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_postal)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Postal code must contain only digits
            !editText.text.isDigitsOnly() -> {
                // Display an error message
                message = activity.getString(R.string.err_postal_format)
                    .also { showSnackBar(activity, it, true) }

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

    // Function to display an error message if the required checkbox is not checked
    internal fun requiredCheckbox(checkBox: CheckBox): Boolean {
        return if (!checkBox.isChecked) {
            message = when (checkBox.id) {
                // Account Registration T&C Checkbox
                R.id.cb_terms_and_conditions -> activity
                    .getString(R.string.err_unchecked_tac)

                // Vendor Registration T&C Checkbox
                R.id.cb_vendor_t_and_c -> activity
                    .getString(R.string.err_unchecked_vendor_tac)

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
                // Province from Market Editor Activity
                R.id.actv_mkt_edit_province -> activity
                    .getString(R.string.err_blank_mkt_province)

                // City/Municipality from Address Edit Activity
                R.id.actv_addr_ctm -> activity.getString(R.string.err_blank_city)
                // City/Municipality from Market Editor Activity
                R.id.actv_mkt_edit_ctm -> activity
                    .getString(R.string.err_blank_mkt_city)

                // Barangay from Address Edit Activity
                R.id.actv_addr_brgy -> activity.getString(R.string.err_blank_brgy)
                // Barangay from Market Editor Activity
                R.id.actv_mkt_edit_brgy -> activity
                    .getString(R.string.err_blank_mkt_brgy)

                // Market Category from Market Editor Activity
                R.id.actv_mkt_edit_category -> activity
                    .getString(R.string.err_no_mkt_category_selected)

                // Product Unit from Product Editor Activity
                R.id.actv_prod_edit_unit -> activity
                    .getString(R.string.err_no_unit_selected)

                // Default error message
                else -> activity.getString(R.string.unk_err_actv_spinner)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true // Valid
        }  // end of if-else

    }  // end of checkSpinnerSelection method

    // Function to validate edit text fields for specifying other answers
    internal fun validateSpecifyOthers(editText: EditText): Boolean {
        // Valid character count, depending on activity origin
        val validRange: IntRange = when (activity) {
            is MarketEditorActivity -> 4..20
            is ProductEditorActivity -> 2..15
            else -> 0..0
        }

        return when {
            // Specific category must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Specify Category from Market Editor Activity
                    R.id.et_mkt_edit_other_spec -> activity
                        .getString(R.string.err_blank_custom_category)

                    // Custom Unit from Product Editor Activity
                    R.id.et_prod_edit_custom_unit -> activity
                        .getString(R.string.err_blank_custom_unit)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_specific_other)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Specific category must be within valid range, depending on the activity
            editText.text.length !in validRange -> {
                // Display an error message
                message = when (editText.id) {
                    // Specify Category from Market Editor Activity (4-20)
                    R.id.et_mkt_edit_other_spec -> activity
                        .getString(R.string.err_custom_category_length)

                    // Custom Unit from Product Editor Activity (2-15)
                    R.id.et_prod_edit_custom_unit -> activity
                        .getString(R.string.err_custom_unit_length)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_specific_other)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All fields are valid
        }  // end of when

    }  // end of validateSpecifyOthers

    // Function to check if numeric text field is greater than 0
    internal fun isGreaterThanZero(editText: EditText): Boolean {
        return when {
            // Numeric text field must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Price from Product Editor Activity
                    R.id.et_prod_edit_price -> activity
                        .getString(R.string.err_blank_product_price)

                    // Product Weight from Product Editor Activity
                    R.id.et_prod_edit_weight -> activity
                        .getString(R.string.err_blank_product_weight)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Make sure text field contains only numeric values (e.g., 3, 2.5, 0.75, etc.)
            !Constants.isNumeric(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Price from Product Editor Activity
                    R.id.et_prod_edit_price -> activity
                        .getString(R.string.err_product_price_format)

                    // Product Weight from Product Editor Activity
                    R.id.et_prod_edit_weight -> activity
                        .getString(R.string.err_product_weight_format)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Numeric text field must be greater than 0
            editText.text.toString().trim { it <= ' ' }.toDouble() <= 0 -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Price from Product Editor Activity
                    R.id.et_prod_edit_price -> activity
                        .getString(R.string.err_zero_or_negative_price)

                    // Product Weight from Product Editor Activity
                    R.id.et_prod_edit_weight -> activity
                        .getString(R.string.err_zero_or_negative_weight)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All fields are valid
        }  // end of when

    }  // end of isGreaterThanZero method

    // Function to check if numeric text field is a non-negative number
    internal fun isNonNegativeNumber(editText: EditText): Boolean {
        return when {
            // Numeric text field must not be empty
            editText.text.isEmpty() -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Stock from Product Editor Activity
                    R.id.et_prod_edit_stock -> activity
                        .getString(R.string.err_blank_product_stock)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Make sure text field contains only numeric values (e.g., 3, 2.5, 0.75, etc.)
            !Constants.isNumeric(editText.text.toString().trim { it <= ' ' }) -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Stock from Product Editor Activity
                    R.id.et_prod_edit_stock -> activity
                        .getString(R.string.err_product_stock_format)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            // Numeric text field must be greater than or equal to 0 (non-negative number)
            editText.text.toString().trim { it <= ' ' }.toDouble() < 0 -> {
                // Display an error message
                message = when (editText.id) {
                    // Product Stock from Product Editor Activity
                    R.id.et_prod_edit_stock -> activity
                        .getString(R.string.err_negative_stock)

                    // Default error message
                    else -> activity.getString(R.string.unk_err_numeric_text)
                }.also { showSnackBar(activity, it, true) }

                false  // Return false
            }

            else -> true  // All fields are valid
        }  // end of when

    }  // end of isNonNegativeNumber method

    // Function to display an error message if no image was selected
    internal fun requiredImage(
        src: ImageView, imageURI: Uri?, tempImageURL: String
    ): Boolean {
        return if (imageURI == null && tempImageURL.isEmpty()) {
            message = when (src.id) {
                // Product Image from Product Editor Activity
                R.id.iv_product_image -> activity
                    .getString(R.string.err_no_product_image_selected)

                // Default error message
                else -> activity
                    .getString(R.string.unk_err_image)
            }.also { showSnackBar(activity, it, true) }

            false  // Return false
        } else {
            true  // Valid
        }
    }  // end of requiredImage method

}  // end of FormValidation class
