package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityProductEditorBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.*
import java.io.IOException

class ProductEditorActivity : UtilityClass(), View.OnClickListener,
    View.OnLongClickListener {

    private lateinit var binding: ActivityProductEditorBinding
    private lateinit var mUnitList: List<String>
    private var mProdInfo: Product? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mMarketID: String? = null
    private var mTempProductImageURL: String = ""
    private var mProductHashMap: HashMap<String, Any> = hashMapOf()
    private var mIsNewProduct: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Force disable dark mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        binding = ActivityProductEditorBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)

            // Setups the Action Bar of the current activity
            setupActionBar(tlbProdEditActivity, false)

            // Get the string array of measurement units
            mUnitList = resources.getStringArray(R.array.measurement_units).toList()

            // Check if there's an existing string extra info
            if (intent.hasExtra(Constants.MARKET_ID_DATA)) {
                // Get the string data from intent
                mMarketID = intent.getStringExtra(Constants.MARKET_ID_DATA)
            }

            // Check if there's an existing parcelable extra info
            if (intent.hasExtra(Constants.PRODUCT_DESCRIPTION)) {
                // Get data in the parcelable class
                mProdInfo = intent.getParcelableExtra(Constants.PRODUCT_DESCRIPTION)

                setSelectedProductValues()  // Fill up the available fields
            }  // end of if

            // To indicate that this is for new products
            if (mProdInfo == null) mIsNewProduct = true

            // Prepare the drop down values for measurement units
            actvProdEditUnit.setAdapter(
                ArrayAdapter(
                    this@ProductEditorActivity, R.layout.spinner_item, mUnitList
                )
            )

            // Actions when one of the measurement unit items is selected
            actvProdEditUnit.setOnItemClickListener { _, _, position, _ ->
                /* If the Others is selected, display the Custom Unit text
                 * field to specify the measurement
                 */
                if (mUnitList[position] == Constants.ITEM_OTHERS) {
                    tilProdEditCustomUnit.visibility = View.VISIBLE
                }
                // Hide the text field and clear the text
                else {
                    etProdEditCustomUnit.text!!.clear()
                    tilProdEditCustomUnit.visibility = View.GONE
                }
            }

            // Click event for Product Image Selector ImageView
            ivSelectProductImage.setOnClickListener(this@ProductEditorActivity)
            // Click event for Product ImageView
            ivProductImage.setOnClickListener(this@ProductEditorActivity)
            // Long click event for Product ImageView
            ivProductImage.setOnLongClickListener(this@ProductEditorActivity)
            // Click event for Submit Product Button
            btnSubmitProduct.setOnClickListener(this@ProductEditorActivity)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Change the product image
                R.id.iv_product_image,
                R.id.iv_select_product_image -> {
                    // If storage permission access is already granted
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Launch the Image Selection
                        Constants.showImageSelection(this@ProductEditorActivity)
                    } else {
                        // Request a storage access permission to the device
                        ActivityCompat.requestPermissions(
                            this@ProductEditorActivity,
                            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }  // end of if-else
                }

                // Saves product information
                R.id.btn_submit_product -> {
                    // Stores modified information, if any
                    storeProductInfoChanges()

                    if (mIsNewProduct || mProductHashMap.isNotEmpty()) {
                        // If there are any changes made, save product info
                        saveProductChanges()
                    } else {
                        /* Exit the activity if there are no changes made.
                         * This is to prevent unnecessary reads and writes
                         * in Cloud Firestore.
                         */

                        // Displays the Toast message
                        toastMessage(
                            this@ProductEditorActivity,
                            getString(R.string.msg_no_product_info_changed)
                        )

                        finish()  // Closes the current activity
                    }  // end of if-else
                }
            }  // end of when
        }  // end of if

    }  // end of onClick method

    // onLongClick events are declared here
    override fun onLongClick(view: View?): Boolean {
        if (view != null) {
            when (view.id) {
                // Clear the product image
                R.id.iv_product_image -> {
                    if (mTempProductImageURL.isNotEmpty()) {
                        /* Display an alert dialog with two action buttons
                         * (Remove & Cancel)
                         */
                        DialogClass(this@ProductEditorActivity).alertDialog(
                            getString(R.string.dialog_product_pic_remove_title),
                            getString(R.string.dialog_product_pic_remove_message),
                            getString(R.string.dialog_btn_remove),
                            getString(R.string.dialog_btn_cancel),
                            Constants.DELETE_PRODUCT_IMAGE_ACTION
                        )
                    } else {
                        // Display an error message
                        showSnackBar(
                            this@ProductEditorActivity,
                            getString(R.string.err_no_product_pic_to_remove),
                            true
                        )
                    }  // end of if-else
                }

            }  // end of when
        }  // end of if

        return true
    }  // end of onLongClick method

    // Override the back function
    override fun onBackPressed() {
        storeProductInfoChanges()  // Stores modified information, if any

        // If there are any changes to the product information (Add Product)
        if (mIsNewProduct && mProductHashMap.isNotEmpty()) {
            // Display an alert dialog with two action buttons (Exit & Continue)
            DialogClass(this@ProductEditorActivity).alertDialog(
                getString(R.string.dialog_add_product_title),
                getString(R.string.dialog_add_product_message),
                getString(R.string.dialog_btn_exit),
                getString(R.string.dialog_btn_continue),
                Constants.EXIT_PRODUCT_ACTION
            )
        }
        // If there are any changes to the product information (Edit Product)
        else if (mProductHashMap.isNotEmpty()) {
            /* Display an alert dialog with three action buttons
             * (Save, Don't Save & Cancel)
             */
            DialogClass(this@ProductEditorActivity).alertDialog(
                getString(R.string.dialog_edit_product_title),
                getString(R.string.dialog_edit_product_message),
                getString(R.string.dialog_btn_save),
                getString(R.string.dialog_btn_dont_save),
                getString(R.string.dialog_btn_cancel)
            )
        } else {
            super.onBackPressed()
        }  // end of if-else if-else

    }  // end of onBackPressed method

    // Function to check if storage permission is granted or denied
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            // If the user grants storage permission access
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                // Launch the Image Selection
                Constants.showImageSelection(this@ProductEditorActivity)
            } else {
                // If the user denies the permission access
                showSnackBar(
                    this@ProductEditorActivity,
                    getString(R.string.err_storage_permission_denied),
                    true
                )
            }  // end of if-else

        }  // end of if
    }  // end of onRequestPermissionsResult method

    // Function to change image in the product ImageView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.SELECT_IMAGE_REQUEST_CODE
            && data != null
        ) {
            try {
                with(binding) {
                    // URI of selected image file
                    mSelectedImageFileURI = data.data!!

                    // Sets the ImageView to the selected image file
                    GlideLoader(this@ProductEditorActivity)
                        .loadImage(mSelectedImageFileURI!!, ivProductImage)

                    // Set the temporary image URL to the URI of selected image
                    mTempProductImageURL = mSelectedImageFileURI.toString()

                    // Make the "Long Press to Remove" instruction visible
                    tvDeleteProductPicLabel.visibility = View.VISIBLE

                    /* Make the select product image icon from bottom right
                     * not visible if there's any image selected.
                     */
                    if (ivSelectProductImage.isVisible)
                        ivSelectProductImage.visibility = View.GONE
                }
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@ProductEditorActivity,
                    getString(R.string.err_image_selection_failed)
                )
            } // end of try-catch
        } // end of if

    } // end of onActivityResult method

    // Function to store existing product data in the respective fields
    private fun setSelectedProductValues() {
        with(binding) {
            mProdInfo?.let {
                // Product Details (Name, Description, Price, Weight, Stock)
                etProdEditName.setText(it.name)
                etProdEditDesc.setText(it.description)
                etProdEditPrice.setText(it.price.toString())
                etProdEditWeight.setText(it.weight.toString())
                etProdEditStock.setText(it.stock.toString())

                // If the unit is in the list of predefined list
                if (it.unit in mUnitList) {
                    actvProdEditUnit.setText(it.unit)
                } else {
                    // If not in the list, set the spinner text into "Others"
                    actvProdEditUnit.setText(Constants.ITEM_OTHERS)
                    // Make the EditText visible
                    tilProdEditCustomUnit.visibility = View.VISIBLE
                    // Place the custom unit here
                    etProdEditCustomUnit.setText(it.unit)
                }

                // Load the product image
                GlideLoader(this@ProductEditorActivity).loadImage(
                    it.image, ivProductImage
                )

                // Set the temporary product image URL for validation
                mTempProductImageURL = it.image

                // If there's any image uploaded
                if (mTempProductImageURL.isNotEmpty()) {
                    // Make "Long Press to Remove" instruction visible
                    tvDeleteProductPicLabel.visibility = View.VISIBLE
                    // Make the select product image icon from bottom right not visible
                    ivSelectProductImage.visibility = View.GONE
                }

                // Change the header of the Toolbar
                tvProdEditTitle.text = getString(R.string.tlb_title_edit_prod)
            }  // end of let
        }  // end of with(binding)

    }  // end of setSelectedProductValues method

    // Function to remove product image
    internal fun removeProductImage() {
        // Set the product image to default placeholder
        GlideLoader(this@ProductEditorActivity)
            .loadImage("", binding.ivProductImage)

        // Clear all image selection values
        mTempProductImageURL = ""
        // Make the select product image icon from bottom right visible
        mSelectedImageFileURI = null

        // Make the "Long Press to Remove" instruction not visible
        binding.tvDeleteProductPicLabel.visibility = View.GONE
        binding.ivSelectProductImage.visibility = View.VISIBLE

        // Display a success message
        showSnackBar(
            this@ProductEditorActivity, getString(R.string.msg_product_pic_removed),
            false
        )
    }  // end of removeMarketImage method

    // Function to validate product information
    private fun validateProduct(): Boolean {
        with(binding) {
            return FormValidation(this@ProductEditorActivity).run {
                when {
                    // Product Image
                    !requiredImage(
                        ivProductImage, mSelectedImageFileURI, mTempProductImageURL
                    ) -> false
                    // Product Name
                    !validateName(etProdEditName) -> false
                    // Product Description
                    !validateLongTexts(etProdEditDesc) -> false
                    // Price (Double)
                    !isGreaterThanZero(etProdEditPrice) -> false
                    // Unit
                    !checkSpinnerSelection(actvProdEditUnit) -> false
                    // Custom Unit (Unit must be Others)
                    actvProdEditUnit.text.toString()
                        .trim { it <= ' ' } == Constants.ITEM_OTHERS &&
                            !validateSpecifyOthers(etProdEditCustomUnit) -> false
                    // Weight (Double)
                    !isGreaterThanZero(etProdEditWeight) -> false
                    // Stock (Int)
                    !isNonNegativeNumber(etProdEditStock) -> false
                    // When all fields are valid
                    else -> true
                }
            }  // end of run
        }  // end of with(binding)

    }  // end of validateProduct method

    // Function to proceed with saving product information
    internal fun saveProductChanges() {
        // Validate first the product inputs
        if (validateProduct()) {
            /* Display the loading message. "Please wait..." - Add Product.
             * "Saving changes..." - Edit Product.
             */
            showProgressDialog(
                this@ProductEditorActivity, this@ProductEditorActivity,
                if (mProdInfo != null) getString(R.string.msg_saving_changes)
                else getString(R.string.msg_please_wait)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@ProductEditorActivity, mSelectedImageFileURI
                )
            } else {
                // If the user didn't upload the image
                addOrUpdateProduct()
            }  // end of if-else

        }  // end of if

    }  // end of saveProductChanges method

    // Function to add (null mProdInfo) or update product information in the Firestore
    internal fun addOrUpdateProduct(imageURL: String? = null) {
        with(binding) {
            if (mProdInfo == null) {
                // Get the document reference for the new product
                val productRef = FirestoreClass().getProductReference()

                /* Get the unit measurement. If it is "Others", get from the
                 * Specify Custom Unit field
                 */
                val unitMeasurement = if (actvProdEditUnit.text
                        .toString().trim { it <= ' ' } != Constants.ITEM_OTHERS
                )
                    actvProdEditUnit.text.toString().trim { it <= ' ' }
                else
                    etProdEditCustomUnit.text.toString().trim { it <= ' ' }

                // Object to store product data
                val product = Product(
                    Constants.PRODUCT_ID_TEMP + productRef.id,
                    etProdEditName.text.toString().trim { it <= ' ' },
                    imageURL ?: "",
                    etProdEditDesc.text.toString().trim { it <= ' ' },
                    etProdEditPrice.text.toString().trim { it <= ' ' }.toDouble(),
                    unitMeasurement,
                    etProdEditWeight.text.toString().trim { it <= ' ' }.toDouble(),
                    etProdEditStock.text.toString().trim { it <= ' ' }.toInt(),
                    FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    mMarketID ?: ""
                )

                // Adds the product data in the Firestore Database
                FirestoreClass().addProduct(this@ProductEditorActivity, product)
            } else {
                // Overwrite product image URL, if the user uploaded the image
                if (imageURL != null)
                    mProductHashMap[Constants.IMAGE] = imageURL

                // Proceed to update fields in the Cloud Firestore
                FirestoreClass().updateProduct(
                    this@ProductEditorActivity, mProdInfo!!.id, mProductHashMap
                )
            }  // end of if-else

        }  // end of with(binding)

    }  // end of addProductInfo method

    // Function to store modified product information
    private fun storeProductInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mProductHashMap.clear()

        /* Store temporary object for comparison purposes if the page is for
         * adding new products
         */
        if (mIsNewProduct) mProdInfo = Product()

        with(binding) {
            mProdInfo?.let { prod ->
                val productName = etProdEditName.text.toString().trim { it <= ' ' }
                // Save the new product name if it is different from previous product name
                if (productName != prod.name)
                    mProductHashMap[Constants.NAME] = productName

                val productDesc = etProdEditDesc.text.toString().trim { it <= ' ' }
                /* Save the new product description if it is different from previous
                 * product description
                 */
                if (productDesc != prod.description)
                    mProductHashMap[Constants.DESCRIPTION] = productDesc

                val productPrice = if (etProdEditPrice.text!!.isNotEmpty())
                    etProdEditPrice.text.toString().trim { it <= ' ' }.toDouble()
                else
                    0.0  // Default value to prevent NumberFormatException
                // Save the new product price if it is different from previous product price
                if (productPrice != prod.price)
                    mProductHashMap[Constants.PRICE] = productPrice

                /* Stores product unit. If the user selects "Others" in the Product
                 * unit spinner, store the text in Custom Unit field. Otherwise,
                 * store the text in Product Unit spinner.
                 */
                val productUnit = if (actvProdEditUnit.text.toString()
                        .trim { it <= ' ' } == Constants.ITEM_OTHERS
                )
                    etProdEditCustomUnit.text.toString().trim { it <= ' ' }
                else
                    actvProdEditUnit.text.toString().trim { it <= ' ' }
                // Save the new product unit if it is different from previous product unit
                if (productUnit != prod.unit)
                    mProductHashMap[Constants.UNIT] = productUnit

                val productWeight = if (etProdEditWeight.text!!.isNotEmpty())
                    etProdEditWeight.text.toString().trim { it <= ' ' }.toDouble()
                else
                    0.0  // Default value to prevent NumberFormatException
                // Save the new product weight if it is different from previous product weight
                if (productWeight != prod.weight)
                    mProductHashMap[Constants.WEIGHT] = productWeight

                val productStock = if (etProdEditStock.text!!.isNotEmpty())
                    etProdEditStock.text.toString().trim { it <= ' ' }.toInt()
                else
                    0  // Default value to prevent NumberFormatException
                // Save the new product stock if it is different from previous product stock
                if (productStock != prod.stock)
                    mProductHashMap[Constants.STOCK] = productStock

                // Check if a user has uploaded a new image, add a temporary value
                if (mTempProductImageURL != prod.image)
                    mProductHashMap[Constants.IMAGE] = mTempProductImageURL
            }  // end of let
        }  // end of with(binding)

        // Revert back to null if the page is for vendor registration
        if (mIsNewProduct) mProdInfo = null

    }  // end of storeProductInfoChanges method

    // Function to prompt that the product was saved
    fun productSavedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Displays the Toast message
        toastMessage(
            this@ProductEditorActivity, getString(R.string.msg_product_saved)
        )

        finish() // Closes the current activity
    }  // end of productSavedPrompt method

}  // end of ProductEditorActivity class
