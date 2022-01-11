package edu.cccdci.opal.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
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
        val template: String = when (activity) {
            is UserProfileActivity -> Constants.USER_PROFILE_IMAGE_TEMP
            is ProductEditorActivity -> Constants.PRODUCT_IMAGE_TEMP
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
                             * in mProductImageURL
                             */
                            is ProductEditorActivity -> activity
                                .imageUploadSuccess(uri.toString())
                        }
                    }
            }
            // If failed
            .addOnFailureListener { exception ->
                /* Closes the loading message in the User Profile Activity
                 * or Product Editor Activity
                 */
                when (activity) {
                    is UserProfileActivity -> activity.hideProgressDialog()
                    is ProductEditorActivity -> activity.hideProgressDialog()
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
    fun getProvinces(activity: AddressEditActivity) {
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
                val provinces = if (document != null)
                    document.toObject(Province::class.java)!!.provinceList
                else
                    listOf()

                // Proceed to store the retrieved list of provinces
                activity.retrieveProvinces(provinces)
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
    fun getCities(activity: AddressEditActivity, provID: String) {
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
                val cities = if (document != null)
                    document.toObject(City::class.java)!!.cityList
                else
                    listOf()

                // Proceed to store the retrieved list of cities/municipalities
                activity.retrieveCities(cities)
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
            // First, order the documents by default first, descending (true first)
            .orderBy(Constants.DEFAULT_ADDR, Query.Direction.DESCENDING)
            // Next, order the documents by pickup field, descending (true first)
            .orderBy(Constants.PICKUP_ADDR, Query.Direction.DESCENDING)
    }  // end of getAddressQuery method

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
                // Prompt the user that the product was savec
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

    // Function to get the query statement for Products
    fun getProductQuery(fragment: Fragment): Query {
        // Access the collection named Products
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
                    Constants.PRODUCT_VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                    .whereGreaterThan(Constants.STOCK, 0)

                /* To get all products that are sold out, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * stock is equal to 0.
                 */
                is ProductSoldOutFragment -> whereEqualTo(
                    Constants.PRODUCT_VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STOCK, 0)

                /* To get all products that are violated, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * status code is equal to 2 (Violation).
                 */
                is ProductViolationFragment -> whereEqualTo(
                    Constants.PRODUCT_VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_VIOLATION)

                /* To get all products that are violated, get all the documents
                 * where vendor ID is equal to the current user ID, and the
                 * status code is equal to 0 (Unlisted).
                 */
                is ProductUnlistedFragment -> whereEqualTo(
                    Constants.PRODUCT_VENDOR_ID, getCurrentUserID()
                ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_UNLISTED)

                /* The default query, get all the documents where the status
                 * code is equal to 1 (In Stock) and limit the number of
                 * document retrievals to 20.
                 */
                else -> whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
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
                // Convert the retrieved document to object
                val market = document.toObject(Market::class.java)!!

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

    // Function to update user's cart in Firestore Database
    fun updateCart(
        activity: Activity, user: User, marketID: String,
        items: List<CartItem>, toAdd: Boolean = false
    ) {
        // Variable to store user's cart data
        val cartItemMap: HashMap<String, Any> = hashMapOf()

        // If the cart field exists
        if (user.cart != null) {
            // Get the current cart items
            val currentCartItems = user.cart.cartItems

            // If the item is not from the same market, change the market ID
            if (user.cart.marketID != marketID)
                cartItemMap[Constants.MARKET_ID] = marketID

            /* Add the item on the list if the user adds to cart.
             * In the cart menu, when the user changes items, it
             * will be overwritten instead.
             */
            cartItemMap[Constants.CART_ITEMS] = if (toAdd) {
                currentCartItems.addAll(items)
                currentCartItems
            } else {
                items
            }
            // If it doesn't, just add the specified fields in the map
        } else {
            cartItemMap[Constants.MARKET_ID] = marketID
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
                }
            }
            // If failed
            .addOnFailureListener { e ->
                when (activity) {
                    // Closes the loading message in the Product Description Activity
                    is ProductDescActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error updating the user cart data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of updateCart method

}  // end of FirestoreClass