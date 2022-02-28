package edu.cccdci.opal.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityProductEditorBinding
import edu.cccdci.opal.dataclasses.Product
import edu.cccdci.opal.firestore.FirestoreClass
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GlideLoader
import edu.cccdci.opal.utils.UtilityClass
import java.io.IOException

class ProductEditorActivity : UtilityClass(), View.OnClickListener {

    private lateinit var binding: ActivityProductEditorBinding
    private lateinit var mUnitList: List<String>
    private var mProdInfo: Product? = null
    private var mSelectedImageFileURI: Uri? = null
    private var mMarketID: String? = null
    private var mTempProductImageURL: String = ""
    private var mProductImageURL: String = ""
    private var mProductHashMap: HashMap<String, Any> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
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
                mProdInfo = intent.getParcelableExtra(Constants.PRODUCT_DESCRIPTION)!!

                // Change the header of the Toolbar
                tvProdEditTitle.text = resources.getString(R.string.tlb_title_edit_prod)

                // To prevent NullPointerException
                if (mProdInfo != null)
                    setSelectedProductValues()  // Fill up the available fields
            }  // end of if

            // Prepare the drop down values for measurement units
            val unitAdapter = ArrayAdapter(
                this@ProductEditorActivity, R.layout.spinner_item, mUnitList
            )
            actvProdEditUnit.setAdapter(unitAdapter)

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
            // Click event for Submit Product Button
            btnSubmitProduct.setOnClickListener(this@ProductEditorActivity)

        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                // Change the product image
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
                R.id.btn_submit_product -> saveProductChanges()
            }  // end of when

        }  // end of if

    }  // end of onClick method

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
                    resources.getString(R.string.err_permission_denied),
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
                // URI of selected image file
                mSelectedImageFileURI = data.data!!

                // Sets the ImageView to the selected image file
                GlideLoader(this@ProductEditorActivity)
                    .loadImage(
                        mSelectedImageFileURI!!, binding.ivProductImage
                    )
            } catch (e: IOException) {
                e.printStackTrace()

                // Display an error Toast message
                toastMessage(
                    this@ProductEditorActivity,
                    resources.getString(R.string.err_image_selection_failed)
                )
            } // end of try-catch

        } // end of if

    } // end of onActivityResult method

    // Function to store existing product data in the respective fields
    private fun setSelectedProductValues() {
        with(binding) {
            // Product Details (Name, Description, Price, Weight, Stock)
            etProdEditName.setText(mProdInfo!!.name)
            etProdEditDesc.setText(mProdInfo!!.description)
            etProdEditPrice.setText(mProdInfo!!.price.toString())
            etProdEditWeight.setText(mProdInfo!!.weight.toString())
            etProdEditStock.setText(mProdInfo!!.stock.toString())

            // If the unit is in the list of predefined list
            if (mProdInfo!!.unit in mUnitList) {
                actvProdEditUnit.setText(mProdInfo!!.unit)
            } else {
                // If not in the list, set the spinner text into "Others"
                actvProdEditUnit.setText(Constants.ITEM_OTHERS)
                // Make the EditText visible
                tilProdEditCustomUnit.visibility = View.VISIBLE
                // Place the custom unit here
                etProdEditCustomUnit.setText(mProdInfo!!.unit)
            }

            // Load the product image
            GlideLoader(this@ProductEditorActivity).loadImage(
                mProdInfo!!.image, ivProductImage
            )

            // Set the temporary product image URL for validation
            mTempProductImageURL = mProdInfo!!.image
        }  // end of with(binding)
    }  // end of setSelectedProductValues method

    // Function to validate product information
    private fun validateProduct(): Boolean {
        with(binding) {
            return when {
                // If no Product Image is selected
                mSelectedImageFileURI == null && mTempProductImageURL.isEmpty() -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_no_product_image_selected),
                        true
                    )
                    false  // return false
                }

                // If the Product Name field is empty
                TextUtils.isEmpty(etProdEditName.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_product_name),
                        true
                    )
                    false  // return false
                }

                // If the Product Description field is empty
                TextUtils.isEmpty(etProdEditDesc.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_product_desc),
                        true
                    )
                    false  // return false
                }

                // If the Product Price field is empty
                TextUtils.isEmpty(etProdEditPrice.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_product_price),
                        true
                    )
                    false  // return false
                }

                // If the Product Price is zero or negative
                etProdEditPrice.text.toString().trim { it <= ' ' }
                    .toDouble() <= 0 -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_zero_or_negative_price),
                        true
                    )
                    false  // return false
                }

                // If no Product Measurement Unit is selected
                TextUtils.isEmpty(actvProdEditUnit.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_no_unit_selected),
                        true
                    )
                    false  // return false
                }


                /* If the user selects Others in the Measurement Unit and
                 * the Custom Unit field is empty
                 */
                actvProdEditUnit.text.toString()
                    .trim { it <= ' ' } == Constants.ITEM_OTHERS &&
                        TextUtils.isEmpty(etProdEditCustomUnit.text.toString()
                            .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_custom_unit),
                        true
                    )
                    false  // return false
                }

                // If the Product Weight field is empty
                TextUtils.isEmpty(etProdEditWeight.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_product_weight),
                        true
                    )
                    false  // return false
                }

                // If the Product Weight is zero or negative
                etProdEditWeight.text.toString().trim { it <= ' ' }
                    .toDouble() <= 0 -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_zero_or_negative_weight),
                        true
                    )
                    false  // return false
                }

                // If the Product Stock field is empty
                TextUtils.isEmpty(etProdEditStock.text.toString()
                    .trim { it <= ' ' }) -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_blank_product_stock),
                        true
                    )
                    false  // return false
                }

                // If the Product Stock is negative
                etProdEditStock.text.toString().trim { it <= ' ' }
                    .toInt() < 0 -> {
                    // Display an error message
                    showSnackBar(
                        this@ProductEditorActivity,
                        resources.getString(R.string.err_negative_stock),
                        true
                    )
                    false  // return false
                }

                else -> true  // If all inputs are valid
            }  // end of when

        }  // end of with(binding)

    }  // end of validateProduct method

    // Function to proceed with saving product information
    private fun saveProductChanges() {
        // Validate first the product inputs
        if (validateProduct()) {
            // Display the loading message
            showProgressDialog(
                this@ProductEditorActivity, this@ProductEditorActivity,
                resources.getString(R.string.msg_please_wait)
            )

            // If the user uploaded the image
            if (mSelectedImageFileURI != null) {
                // Proceed to upload image to Cloud Storage
                FirestoreClass().uploadImageToCloud(
                    this@ProductEditorActivity, mSelectedImageFileURI
                )
            } else {
                // If the user didn't upload the image
                if (mProdInfo == null) {
                    addProductInfo()  // Add product data if mProdInfo is null
                } else {
                    // Update product data if mProdInfo contains any information
                    updateProductInfo()
                }

            }  // end of if-else

        }
    }  // end of saveProductChanges method

    /* Function to prompt that the user has successfully uploaded the image.
     * And then adds the rest of the product information.
     */
    fun imageUploadSuccess(imageURL: String) {
        // Store the image URL of Product Image
        mProductImageURL = imageURL

        if (mProdInfo == null) {
            // Proceed to add the rest of the product information if mProdInfo is null
            addProductInfo()
        } else {
            /* Proceed to update the rest of the product information if
             * mProdInfo contains any information
             */
            updateProductInfo()
        }
    }  // end of imageUploadSuccess method

    // Function to add product information in the Firestore
    private fun addProductInfo() {
        with(binding) {
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
                mProductImageURL,
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
        }  // end of with(binding)
    }  // end of addProductInfo method

    // Function to store modified information to Firestore (if any)
    private fun updateProductInfo() {
        storeProductInfoChanges()  // Stores modified information, if any

        // Overwrite product image URL, if the user uploaded the image
        if (mProductImageURL.isNotEmpty())
            mProductHashMap[Constants.IMAGE] = mProductImageURL

        // Proceed to update fields in the Cloud Firestore
        FirestoreClass().updateProduct(
            this@ProductEditorActivity, mProdInfo!!.id, mProductHashMap
        )
    }  // end of updateProductInfo method

    // Function to store modified product information
    private fun storeProductInfoChanges() {
        // Clear the HashMap first for a new batch of modified information
        mProductHashMap.clear()

        with(binding) {
            val productName = etProdEditName.text.toString().trim { it <= ' ' }
            // Save the new product name if it is different from previous product name
            if (productName != mProdInfo!!.name)
                mProductHashMap[Constants.NAME] = productName

            val productDesc = etProdEditDesc.text.toString().trim { it <= ' ' }
            /* Save the new product description if it is different from previous
             * product description
             */
            if (productDesc != mProdInfo!!.description)
                mProductHashMap[Constants.DESCRIPTION] = productDesc

            val productPrice = etProdEditPrice.text.toString().trim { it <= ' ' }.toDouble()
            // Save the new product price if it is different from previous product price
            if (productPrice != mProdInfo!!.price)
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
            if (productUnit != mProdInfo!!.unit)
                mProductHashMap[Constants.UNIT] = productUnit

            val productWeight = etProdEditWeight.text.toString().trim { it <= ' ' }.toDouble()
            // Save the new product weight if it is different from previous product weight
            if (productWeight != mProdInfo!!.weight)
                mProductHashMap[Constants.WEIGHT] = productWeight

            val productStock = etProdEditStock.text.toString().trim { it <= ' ' }.toInt()
            // Save the new product stock if it is different from previous product stock
            if (productStock != mProdInfo!!.stock)
                mProductHashMap[Constants.STOCK] = productStock

            // Check if a user has uploaded a new image, add a temporary value
            if (mSelectedImageFileURI != null)
                mProductHashMap[Constants.IMAGE] = "0"
        }  // end of with(binding)
    }  // end of storeProductInfoChanges method

    // Function to prompt that the product was saved
    fun productSavedPrompt() {
        hideProgressDialog()  // Hide the loading message

        // Displays the Toast message
        toastMessage(
            this@ProductEditorActivity,
            resources.getString(R.string.msg_product_saved)
        )

        finish() // Closes the current activity
    }  // end of productSavedPrompt method

}  // end of ProductEditorActivity class