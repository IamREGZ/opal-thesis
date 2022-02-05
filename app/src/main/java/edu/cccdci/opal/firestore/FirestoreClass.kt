package edu.cccdci.opal.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import edu.cccdci.opal.R
import edu.cccdci.opal.adapters.CartAdapter
import edu.cccdci.opal.dataclasses.*
import edu.cccdci.opal.ui.activities.*
import edu.cccdci.opal.ui.fragments.*
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
            // Sets the values in the user document and merge with the current object
            .set(userInfo, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Prompt the user that he/she is registered
                activity.registerSuccessPrompt()
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

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
                    is MainActivity -> activity.setNavigationAttributes(
                        sharedPrefs, user
                    )

                    /* In Product Description Activity, it sets the current
                     * user details to check if the product is in the user's
                     * cart.
                     */
                    is ProductDescActivity -> activity.setCurrentUserDetails(user)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                /* Closes the loading message in the Login, Register
                 * or Main Activity
                 */
                when (activity) {
                    is LoginActivity -> activity.hideProgressDialog()
                    is RegisterActivity -> activity.hideProgressDialog()
                    is MainActivity -> activity.hideProgressDialog()
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

                    // Same goes with Market Editor Activity (from addMarket method)
                    is MarketEditorActivity -> activity.marketSavedPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                /* Closes the loading message in the User Profile Activity
                 * or Market Editor Activity
                 */
                when (activity) {
                    is UserProfileActivity -> activity.hideProgressDialog()
                    is MarketEditorActivity -> activity.hideProgressDialog()
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
        // Store the string template for image file, depending on the origin activity
        val template: String = when (activity) {
            is UserProfileActivity -> Constants.USER_PROFILE_IMAGE_TEMP
            is ProductEditorActivity -> Constants.PRODUCT_IMAGE_TEMP
            is MarketEditorActivity -> Constants.MARKET_IMAGE_TEMP
            else -> ""
        }

        // Create a file name for the image being uploaded to Cloud Storage
        val sRef: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                template + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(activity, imageFileURI)
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

                        when (activity) {
                            /* In the User Profile Activity, the URI of the image
                             * will be stored in the variable mUserProfileImageURL.
                             */
                            is UserProfileActivity -> activity
                                .imageUploadSuccess(uri.toString())

                            /* In the Product Editor Activity, it will be stored
                             * in mProductImageURL.
                             */
                            is ProductEditorActivity -> activity
                                .imageUploadSuccess(uri.toString())

                            /* In the Market Editor Activity, it will be stored
                             * in mMarketImageURL.
                             */
                            is MarketEditorActivity -> activity
                                .imageUploadSuccess(uri.toString())
                        }
                    }
            }
            // If failed
            .addOnFailureListener { exception ->
                /* Closes the loading message in the User Profile Activity,
                 * Product Editor Activity, or Market Editor Activity
                 */
                when (activity) {
                    is UserProfileActivity -> activity.hideProgressDialog()
                    is ProductEditorActivity -> activity.hideProgressDialog()
                    is MarketEditorActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName, exception.message,
                    exception
                )
            } // end of sRef

    }  // end of uploadImageToCloud method

    // Function to retrieve province list data from Cloud Firestore
    fun getProvinces(activity: Activity) {
        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document that serves as container for province data (DOC-PRV)
            .document(Constants.PRV_DOC)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the province list data
                Log.i(activity.javaClass.simpleName, document.toString())

                // Retrieve the result. If the document is null, return an empty list.
                val provinces = if (document != null && document.exists())
                    document.toObject(Province::class.java)!!.provinceList
                else
                    listOf()

                // Proceed to store the retrieved list of provinces
                when (activity) {
                    is AddressEditActivity -> activity.retrieveProvinces(provinces)

                    is MarketEditorActivity -> activity.retrieveProvinces(provinces)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving province data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of getProvinces method

    // Function to retrieve city/municipality list data from Cloud Firestore
    fun getCities(activity: Activity, provID: String) {
        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document named by selected province ID
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
                Log.i(activity.javaClass.simpleName, document.toString())

                // Retrieve the result. If the document is null, return an empty list.
                val cities = if (document != null && document.exists())
                    document.toObject(City::class.java)!!.cityList
                else
                    listOf()

                // Proceed to store the retrieved list of cities/municipalities
                when (activity) {
                    is AddressEditActivity -> activity.retrieveCities(cities)

                    is MarketEditorActivity -> activity.retrieveCities(cities)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving city data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of getCities

    // Function to retrieve barangay list data from Cloud Firestore
    fun getBarangays(context: Context, provID: String, cityID: String): List<String> {
        // A return variable that stores list of barangay data
        val barangays: MutableList<String> = mutableListOf()

        // Access the collection named provinces
        mFSInstance.collection(Constants.PROVINCES)
            // Access the document named by selected province ID
            .document(provID)
            // Access the collection named cities
            .collection(Constants.CITY_COL)
            // Access the document named by selected city/municipality ID
            .document(cityID)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Log the retrieved documents
                Log.i(context.javaClass.simpleName, document.toString())

                // Store the barangay list if the result is not null
                if (document != null && document.exists()) {
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

    // Function to get the document reference of user address for ID generation
    fun getUserAddressReference(): DocumentReference {
        return mFSInstance.collection(Constants.USERS)
            .document(getCurrentUserID())
            .collection(Constants.ADDRESSES)
            .document()
    }  // end of getUserAddressReference method

    // Function to add user address data to Cloud Firestore
    fun addUserAddress(activity: AddressEditActivity, addressInfo: Address) {
        // Access the collection named users.
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Access the collection named addresses. If none, Firestore will create it for you.
            .collection(Constants.ADDRESSES)
            // Access the document named by address id. If none, Firestore will create it for you.
            .document(addressInfo.addressID)
            // Sets the values in the address document and merge with the current object
            .set(addressInfo, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Prompt the user that the address was saved
                activity.addressSavedPrompt()
            }
            // If failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error saving the address data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of addUserAddress method

    // Function to get the query statement for address collection
    fun getAddressQuery(): Query {
        // Access the collection named users
        return mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Access the collection named addresses
            .collection(Constants.ADDRESSES)
            // Order the documents by default, descending (true first)
            .orderBy(Constants.DEFAULT_ADDR, Query.Direction.DESCENDING)
    }  // end of getAddressQuery method

    // Function to search for user address set as default
    fun selectDefaultAddress(activity: CheckoutActivity) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Access the collection named addresses
            .collection(Constants.ADDRESSES)
            // Get the document where the default field is true
            .whereEqualTo(Constants.DEFAULT_ADDR, true)
            .get()
            // If it is successful
            .addOnSuccessListener { documents ->
                /* If a document is found, get the found document and convert
                 * to an object (Address). Otherwise, null.
                 */
                val address: Address? = if (documents != null && !documents.isEmpty)
                    documents.documents[0].toObject(Address::class.java)
                else
                    null

                // Proceed to store the result in the Checkout Activity
                activity.storeSelectedAddress(address)
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving the address data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of selectDefaultAddress method

    // Function to update user address data from Cloud Firestore
    fun updateAddress(
        activity: AddressEditActivity, addressID: String,
        addrHashMap: HashMap<String, Any>
    ) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Access the collection named addresses
            .collection(Constants.ADDRESSES)
            // Access the document named by the selected address id
            .document(addressID)
            // Update the values of the specified field
            .update(addrHashMap)
            // If it is successful
            .addOnSuccessListener {
                // Prompt that the address was saved in the Firestore
                activity.addressSavedPrompt()
            }
            // If failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error updating the address data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of updateAddress method

    // Function to delete user address data from Cloud Firestore
    fun deleteAddress(activity: AddressEditActivity, addressID: String) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Access the collection named addresses
            .collection(Constants.ADDRESSES)
            // Access the document named by the selected address id
            .document(addressID)
            // Delete the entire document
            .delete()
            // If it is successful
            .addOnSuccessListener {
                // Prompt that the address was deleted from Firestore
                activity.addressDeletedPrompt()
            }
            // If failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error deleting the address data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of deleteAddress method

    // Function to get the document reference of product for ID generation
    fun getProductReference(): DocumentReference {
        return mFSInstance.collection(Constants.PRODUCTS).document()
    }  // end of getProductReference method

    // Function to add product data to Cloud Firestore
    fun addProduct(activity: ProductEditorActivity, product: Product) {
        // Access the collection named products. If none, Firestore will create it for you.
        mFSInstance.collection(Constants.PRODUCTS)
            // Access the document named by product ID. If none, Firestore will create it for you.
            .document(product.id)
            // Sets the values in the product document and merge with the current object
            .set(product, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Prompt the user that the product was saved
                activity.productSavedPrompt()
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error saving the product data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of addProduct method

    // Function to get the query statement for products
    fun getProductQuery(fragment: Fragment): Query {
        // Access the collection named products
        val productRef = mFSInstance.collection(Constants.PRODUCTS)

        return with(productRef) {
            // Returns the query, depending on the tab of Product Inventory fragment
            when (fragment) {
                /* To get all products that are in stock, get all the documents
                 * where vendor ID is equal to the current user ID, the status
                 * code is equal to 1 (In Stock), and the stock is greater than
                 * 0.
                 */
                is ProductInStockFragment -> whereEqualTo(
                    Constants.VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                    .whereGreaterThan(Constants.STOCK, 0)

                /* To get all products that are sold out, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * stock is equal to 0.
                 */
                is ProductSoldOutFragment -> whereEqualTo(
                    Constants.VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STOCK, 0)

                /* To get all products that are violated, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * status code is equal to 2 (Violation).
                 */
                is ProductViolationFragment -> whereEqualTo(
                    Constants.VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_VIOLATION)

                /* To get all products that are unlisted, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * status code is equal to 0 (Unlisted).
                 */
                is ProductUnlistedFragment -> whereEqualTo(
                    Constants.VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_UNLISTED)

                /* The default query, get all the documents where the status
                 * code is equal to 1 (In Stock), the stock is greater than 0,
                 * and limit the number of document retrievals to 20.
                 */
                else -> whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                    .whereGreaterThan(Constants.STOCK, 0)
                    .limit(20)
            }  // end of when

        }  // end of with(productRef)

    }  // end of getProductInStockQuery method

    // Function to retrieve a specific product for cart item data
    fun retrieveProductItem(
        context: Context, cartViewHolder: CartAdapter.CartViewHolder,
        position: Int, productID: String
    ) {
        // Access the collection named products
        mFSInstance.collection(Constants.PRODUCTS)
            // Access the document named by the provided product ID
            .document(productID)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                // Convert the retrieved document to object
                val product = document.toObject(Product::class.java)!!

                /* In the RecyclerView Holder of Cart RecyclerView, it
                 * supplies the data for the cart item.
                 */
                cartViewHolder.setProductDetails(product, position)
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    context.javaClass.simpleName,
                    "There was an error retrieving the product data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of retrieveProductData method

    // Function to update product data from Cloud Firestore
    fun updateProduct(
        activity: Activity, productID: String, productHashMap: HashMap<String, Any>,
        fragment: Fragment? = null
    ) {
        // Access the collection named products
        mFSInstance.collection(Constants.PRODUCTS)
            // Access the document named by the selected product id
            .document(productID)
            // Update the values of the specified field
            .update(productHashMap)
            // If it is successful
            .addOnSuccessListener {
                when (activity) {
                    /* In Product Editor Activity, it sends the user to the
                     * Product Inventory Activity
                     */
                    is ProductEditorActivity -> activity.productSavedPrompt()
                }

                // For fragments only
                if (fragment != null) {
                    when (fragment) {
                        // Show the prompt message that the product is unlisted
                        is ProductInStockFragment,
                        is ProductSoldOutFragment -> Toast.makeText(
                            fragment.requireContext(),
                            fragment.resources.getString(R.string.msg_product_unlisted),
                            Toast.LENGTH_SHORT
                        ).show()

                        // Show the prompt message that the product is relisted
                        is ProductUnlistedFragment -> {
                            Toast.makeText(
                                fragment.requireContext(),
                                fragment.resources.getString(R.string.msg_product_relisted),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }  // end of when
                }  // end of if
            }
            // If failed
            .addOnFailureListener { e ->
                // Closes the loading message in the Product Editor Activity
                when (activity) {
                    is ProductEditorActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error updating the product data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of updateProduct method

    // Function to delete product data from Cloud Firestore
    fun deleteProduct(context: Context, productID: String, util: UtilityClass) {
        // Access the collection named products
        mFSInstance.collection(Constants.PRODUCTS)
            // Access the document named by the selected product id
            .document(productID)
            // Delete the entire document
            .delete()
            // If it is successful
            .addOnSuccessListener {
                util.hideProgressDialog()  // Hide the loading message

                // Displays the Toast message
                util.toastMessage(
                    context, context.resources.getString(R.string.msg_product_deleted)
                )
            }
            // If failed
            .addOnFailureListener { e ->
                util.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    context.javaClass.simpleName,
                    "There was an error deleting the product data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of deleteProduct method

    // Function to get the document reference of market for ID generation
    fun getMarketReference(): DocumentReference {
        return mFSInstance.collection(Constants.MARKETS).document()
    }  // end of getMarketReference method

    // Function to add market data to Cloud Firestore
    fun addMarket(activity: MarketEditorActivity, market: Market) {
        // Access the collection named markets. If none, Firestore will create it for you.
        mFSInstance.collection(Constants.MARKETS)
            // Access the document named by market ID. If none, Firestore will create it for you.
            .document(market.id)
            // Sets the values in the market document and merge with the current object
            .set(market, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Delay for 1.5 seconds to give way for Firestore's write operation
                android.os.Handler(Looper.getMainLooper()).postDelayed({
                    // Proceed to update current user's vendor status
                    updateUserProfileData(
                        activity, hashMapOf(Constants.VENDOR to true)
                    )
                }, 1500)
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error saving the market data.",
                    e
                )
            }  // end of mFSInstance

    }  // end of addMarket method

    // Function to get the query statement for market collection
    fun getMarketQuery(): Query {
        // Access the collection named markets
        return mFSInstance.collection(Constants.MARKETS)
            // Limit the number of document retrievals to 10
            .limit(10)
    }  // end of getMarketQuery method

    // Function to retrieve a single market data for Cart Activity
    fun retrieveMarket(activity: Activity, marketID: String) {
        // Access the collection named markets
        mFSInstance.collection(Constants.MARKETS)
            // Access the document named by the provided market ID
            .document(marketID)
            // Get the selected document
            .get()
            // If it is successful
            .addOnSuccessListener { document ->
                /* If the document exists, convert the retrieved document
                 * to object. Otherwise, null.
                 */
                val market = if (document.exists())
                    document.toObject(Market::class.java)!!
                else
                    null

                /* In Cart Activity, it supplies the market name and delivery fee
                 * output data.
                 */
                if (activity is CartActivity) {
                    activity.setMarketData(market)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Hide the loading message and exit the Cart Activity
                if (activity is CartActivity) {
                    activity.hideProgressDialog()
                    activity.finish()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving the market data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of retrieveMarket method

    // Function to update product data from Cloud Firestore
    fun updateMarket(
        activity: Activity, marketID: String, marketHashMap: HashMap<String, Any>
    ) {
        // Access the collection named markets
        mFSInstance.collection(Constants.MARKETS)
            // Access the document named by market ID
            .document(marketID)
            // Update the values of the specified field
            .update(marketHashMap)
            // If it is successful
            .addOnSuccessListener {
                // In Market Editor Activity, it sends the user back to home page
                if (activity is MarketEditorActivity) {
                    activity.marketSavedPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                // Closes the loading message in Market Editor Activity
                if (activity is MarketEditorActivity) {
                    activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error update the market data.",
                    e
                )
            }
    }  // end of updateMarket method

    // Function to update user's cart in Firestore Database
    fun updateCart(
        activity: Activity, items: List<CartItem>, marketID: String = "",
        user: User? = null, toClear: Boolean = false
    ) {
        // Variable to store user's cart data
        val cartItemMap: HashMap<String, Any> = hashMapOf()

        // If the user is not null (for adding items only)
        if (user != null) {
            // Get the current cart items. Default value is empty list if cart is null.
            val currentCartItems: MutableList<CartItem> = if (user.cart != null)
                user.cart.cartItems
            else
                mutableListOf()

            // Get the current market ID. Default value is empty string if cart is null.
            val currentMarketID = if (user.cart != null)
                user.cart.marketID
            else
                ""

            // If the item is not from the same market, change the market ID
            if (currentMarketID != marketID) {
                cartItemMap[Constants.MARKET_ID] = marketID

                // Also, empty the current cart items if it is not empty
                if (currentCartItems.isNotEmpty())
                    currentCartItems.clear()
            }

            // Add the item on the list
            currentCartItems.addAll(items)
            // Store the cart items with the added product
            cartItemMap[Constants.CART_ITEMS] = currentCartItems
        }
        // If it's null, just add the specified fields in the map
        else {
            /* Change the market ID only if the user places order or the
             * cart is emptied (toClear = true)
             */
            if (toClear)
                cartItemMap[Constants.MARKET_ID] = marketID

            // Overwrite the cart items (for both updating and clearing)
            cartItemMap[Constants.CART_ITEMS] = items
        }  // end of if-else

        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Create or overwrite the cart field with the selected product
            .set(
                hashMapOf(Constants.CART to cartItemMap), SetOptions.merge()
            )
            // If it is successful
            .addOnSuccessListener {
                when (activity) {
                    /* In Product Description Activity, it prompts the user
                     * that the product is added to cart.
                     */
                    is ProductDescActivity -> activity.itemAddedPrompt()

                    /* In Checkout Activity, it prompts the user that the
                     * cart data is cleared, and then sends to home page.
                     */
                    is CheckoutActivity -> activity.cartClearedPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                when (activity) {
                    /* Closes the loading message in the Product Description
                     * Activity and Checkout Activity
                     */
                    is ProductDescActivity -> activity.hideProgressDialog()
                    is CheckoutActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error updating the user cart data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of updateCart method

    // Function to add Customer Order Data in Firestore Database
    fun addCustomerOrder(
        activity: CheckoutActivity, orderMap: HashMap<String, Any>
    ) {
        // Access the collection named orders. If none, Firestore will create it for you.
        mFSInstance.collection(Constants.CUSTOMER_ORDERS)
            // Access the document named by order ID. If none, Firestore will create it for you.
            .document(orderMap[Constants.ID].toString())
            // Sets the values in the order document and merge with the current map
            .set(orderMap, SetOptions.merge())
            // If it is successful
            .addOnSuccessListener {
                // Proceed to update the user cart data
                activity.orderPlacedPrompt()
            }
            // If it failed
            .addOnFailureListener { e ->
                activity.hideProgressDialog()  // Hide the loading message

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error saving the customer order data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of addCustomerOrder method

    // Function to get the query statement for orders
    fun getOrderQuery(fragment: Fragment, isRoleVendor: Boolean): Query {
        // Access the collection named orders
        val orderRef = mFSInstance.collection(Constants.CUSTOMER_ORDERS)
        // Use vendor ID if it is from Sales History, customer ID for Order History
        val roleKey = if (isRoleVendor) Constants.VENDOR_ID else Constants.CUSTOMER_ID

        // Set the query, depending on the tab of Order/Sales fragment
        val orderQuery = when (fragment) {
            /* To get all orders that are pending, get all the documents
             * where the status code is equal to 0 (Pending), and the
             * customer ID is equal to the current user ID.
             */
            is OrderPendingFragment -> orderRef.whereEqualTo(
                Constants.STATUS, Constants.ORDER_PENDING_CODE
            ).whereEqualTo(roleKey, getCurrentUserID())

            /* To get all the orders that are on delivery, get all the
             * documents where the status code is either 1 (To Deliver)
             * or 2 (Out for Delivery), and the customer ID is equal to
             * the current user ID.
             */
            is OrderToDeliverFragment -> orderRef.whereGreaterThanOrEqualTo(
                Constants.STATUS, Constants.ORDER_TO_DELIVER_CODE
            ).whereLessThanOrEqualTo(
                Constants.STATUS, Constants.ORDER_OFD_CODE
            ).whereEqualTo(roleKey, getCurrentUserID())
                .orderBy(Constants.STATUS, Query.Direction.DESCENDING)

            /* To get all the orders that are completed, get all the
             * documents where the status code is equal to 3 (Delivered),
             * and the customer ID is equal to the current user ID.
             */
            is OrderDeliveredFragment -> orderRef.whereEqualTo(
                Constants.STATUS, Constants.ORDER_DELIVERED_CODE
            ).whereEqualTo(roleKey, getCurrentUserID())

            /* To get all the orders that are cancelled, get all the
             * documents where the status code is equal to 4 (Cancelled),
             * and the customer ID is equal to the current user ID.
             */
            is OrderCancelledFragment -> orderRef.whereEqualTo(
                Constants.STATUS, Constants.ORDER_CANCELLED_CODE
            ).whereEqualTo(roleKey, getCurrentUserID())

            /* To get all the orders that are returned, get all the
             * documents where the status code is either 5 (Return/Refund
             * Requested), 6 (To Return/Refund), or 7 (Returned);
             * and the customer ID is equal to the current user ID.
             */
            is OrderReturnFragment -> orderRef.whereGreaterThanOrEqualTo(
                Constants.STATUS, Constants.ORDER_RETURN_REQUEST_CODE
            ).whereLessThanOrEqualTo(
                Constants.STATUS, Constants.ORDER_RETURNED_CODE
            ).whereEqualTo(roleKey, getCurrentUserID())
                .orderBy(Constants.STATUS, Query.Direction.DESCENDING)

            /* The default query, get all the documents where the customer
             * ID is equal to the current user ID.
             */
            else -> orderRef.whereEqualTo(
                roleKey, getCurrentUserID()
            )
        }

        // Return the query, ordered by the order date, descending
        return orderQuery.orderBy(
            "${Constants.DATES}.${Constants.ORDER_DATE}",
            Query.Direction.DESCENDING
        )
    }  // end of getOrderQuery method

    // Function to update Customer Order document in Firestore Database
    fun updateOrder(
        fragment: Fragment, orderID: String, orderHashMap: HashMap<String, Any>,
        util: UtilityClass
    ) {
        // Access the collection named orders
        mFSInstance.collection(Constants.CUSTOMER_ORDERS)
            // Access the document named by the current order ID
            .document(orderID)
            // Update the values of the specified field
            .update(orderHashMap)
            // If it is successful
            .addOnSuccessListener {
                /* In Order Details Fragment, either the user will stay at the
                 * said fragment if the status is Out for Delivery and the button
                 * clicked was Mark as Paid, or will go to the previous fragment.
                 */
                if (fragment is OrderDetailsFragment) {
                    fragment.orderUpdatedPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                // Closes the loading message in the Order Details Fragment
                if (fragment is OrderDetailsFragment) {
                    util.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    fragment.javaClass.simpleName,
                    "There was an error updating the order data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of updateOrder method

}  // end of FirestoreClass