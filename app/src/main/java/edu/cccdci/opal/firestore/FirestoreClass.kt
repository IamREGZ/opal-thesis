package edu.cccdci.opal.firestore

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
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

                spEditor.putString(
                    Constants.CURRENT_LOCATION, Gson().toJson(
                        user.locSettings ?: CurrentLocation(
                            -1, Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
                        )
                    )
                ).apply()

                when (activity) {
                    // In Login Activity, it sends the user to the home activity
                    is LoginActivity -> activity.logInSuccessPrompt()

                    // In Register Activity, it sends the user to the home activity also
                    is RegisterActivity -> activity.firstLogInPrompt()

                    /* In Main Activity, it sets the placeholder values in
                     * the sidebar header to the user's information.
                     */
                    is MainActivity -> activity.setNavigationAttributes(user)

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
                    is MainActivity -> activity.userSavedPrompt()

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
                    is MainActivity -> activity.hideProgressDialog()
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
    fun deleteUserData(fragment: DeleteAccountFragment, util: UtilityClass) {
        // Access the collection named users
        mFSInstance.collection(Constants.USERS)
            // Access the document named by the current user id
            .document(getCurrentUserID())
            // Delete the entire document
            .delete()
            // If it is successful
            .addOnSuccessListener {
                // Proceed to delete the Firebase Authentication account
                fragment.deleteUserAccount()
            }
            // If it failed
            .addOnFailureListener { e ->
                util.hideProgressDialog()  // Hide the loading message

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
                            /* In the User Profile Activity, the Image URI will be
                             * stored in the user profile object or hashmap.
                             */
                            is UserProfileActivity -> activity
                                .updateUserInfo(uri.toString())

                            /* In the Product Editor Activity, it will be stored
                             * in mProductImageURL.
                             */
                            is ProductEditorActivity -> activity
                                .addOrUpdateProduct(uri.toString())

                            /* In the Market Editor Activity, the Image URI will be
                             * stored in the market object or hashmap.
                             */
                            is MarketEditorActivity -> activity
                                .addOrUpdateMarket(uri.toString())
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
                    emptyList()

                /* Proceed to store the retrieved list of provinces in either
                 * Address Edit or Market Editor activity
                 */
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
                    emptyList()

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
    fun getBarangays(activity: Activity, provID: String, cityID: String) {
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
                Log.i(activity.javaClass.simpleName, document.toString())

                // Retrieve the result. If the document is null, return an empty list.
                val barangays = if (document != null && document.exists())
                    document.toObject(CityBarangay::class.java)!!.barangays
                else
                    emptyList()

                // Proceed to store the retrieved list of barangays
                when (activity) {
                    is AddressEditActivity -> activity.retrieveBarangays(barangays)
                    is MarketEditorActivity -> activity.retrieveBarangays(barangays)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving barangay data.",
                    e
                )
            }  // end of mFSInstance
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
                activity.addressSavedOrDeleted(true)
            }
            // If it failed
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

    // Function to search for address set as default
    fun findDefaultAddress(activity: Activity) {
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
                /* If a document is found, get the found document and convert into
                 * an object (Address). Otherwise, null.
                 */
                val address: Address? = if (documents != null && !documents.isEmpty)
                    documents.documents[0].toObject(Address::class.java)
                else
                    null

                when (activity) {
                    /* In Address Edit Activity, remove the default address status
                     * of found address or just update the modified address info.
                     */
                    is AddressEditActivity -> {
                        if (address != null && address.default) {
                            // Proceed to change the default status of found address
                            updateAddress(
                                activity, address.addressID,
                                hashMapOf(Constants.DEFAULT_ADDR to false),
                                true
                            )
                        } else {
                            // Just update the modified address information
                            activity.addOrUpdateAddress()
                        }  // end of if-else
                    }

                    /* In Checkout Activity, it stores the result to the delivery
                     * address section.
                     */
                    is CheckoutActivity -> activity.storeSelectedAddress(address)
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                // Closes the loading message in the Address Edit or Checkout Activity
                when (activity) {
                    is AddressEditActivity -> activity.hideProgressDialog()
                    is CheckoutActivity -> activity.hideProgressDialog()
                }

                // Log the error
                Log.e(
                    activity.javaClass.simpleName,
                    "There was an error retrieving the address data.",
                    e
                )
            }  // end of mFSInstance
    }  // end of findDefaultAddress method

    // Function to get nearby locations within the 5 km radius from center
    fun getNearbyLocations(
        activity: Activity, center: GeoLocation, selectAddress: Address? = null
    ) {
        val radius = Constants.MAX_RADIUS_IN_M  // 5000 m or 5 km
        /* Used to create a circular boundary to limit geo queries within the
         * specified radius
         */
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radius)

        // Variable to store all tasks to query nearby locations
        val tasks: MutableList<Task<QuerySnapshot>> = mutableListOf()

        val locationQuery = if (selectAddress != null) {
            // Get the collection of user's address for Checkout
            mFSInstance.collection(Constants.USERS)
                .document(getCurrentUserID())
                .collection(Constants.ADDRESSES)
        } else {
            // Get the collection of markets for Market Navigation
            mFSInstance.collection(Constants.MARKETS)
        }

        // Loop through bounds using startHash and endHash
        for (b in bounds) {
            val boundQuery = locationQuery.orderBy(
                "${Constants.LOCATION}.${Constants.GEO_HASH}"
            ).startAt(b.startHash).endAt(b.endHash)

            tasks.add(boundQuery.get())  // Add the query
        }

        // Execute all tasks created from bound queries
        Tasks.whenAllComplete(tasks)
            .addOnCompleteListener { task ->
                // If it is successful
                if (task.isSuccessful) {
                    // Variable to store all documents within the 5 km radius from center
                    val matchedDocs = mutableListOf<Any>()

                    for (t in tasks) {
                        // For each result from tasks, get the snapshot
                        val snap = t.result

                        // Make sure it is not null, not empty, and has existing documents
                        if (snap != null && !snap.isEmpty &&
                            snap.documents.isNotEmpty()
                        ) {
                            // Get all documents
                            for (doc in snap.documents) {
                                val data = doc.toObject(
                                    if (selectAddress != null)
                                        Address::class.java  // For Checkout
                                    else
                                        Market::class.java  // For Market Navigation
                                )!!

                                // Store the document geo location
                                val docLoc = when (data) {
                                    is Address -> data.location?.let {
                                        GeoLocation(it.latitude, it.longitude)
                                    } ?: GeoLocation(0.0, 0.0)

                                    is Market -> data.location?.let {
                                        GeoLocation(it.latitude, it.longitude)
                                    } ?: GeoLocation(0.0, 0.0)

                                    else -> GeoLocation(0.0, 0.0)
                                }

                                /* Get the straight line (euclidean) distance
                                 * between the center and document's geo location
                                 */
                                val distance = GeoFireUtils.getDistanceBetween(
                                    docLoc, center
                                )

                                // If it is in the radius, add that document
                                if (distance <= radius) matchedDocs.add(data)
                            }  // end of for (doc)
                        }  // end of if

                    }  // end of for (t)

                    if (selectAddress != null) {
                        /* Checkout - check if the selected address is in the
                         * delivery coverage
                         */
                        (activity as CheckoutActivity).checkAddressCoverage(
                            selectAddress in matchedDocs.filterIsInstance<Address>()
                        )
                    } else {
                        // Market Navigation - get all matched documents
                        (activity as MarketNavActivity).getRetrievedMarkets(
                            matchedDocs.filterIsInstance<Market>()
                        )
                    }
                }
                // If it failed
                else {
                    // Log the error
                    Log.e(
                        activity.javaClass.simpleName,
                        task.exception!!.message.toString(),
                        task.exception!!
                    )
                }  // end of if-else
            }  // end of whenAllComplete

    }  // end of getNearbyLocations method

    // Function to update user address data from Cloud Firestore
    fun updateAddress(
        activity: AddressEditActivity, addressID: String,
        addrHashMap: HashMap<String, Any?>, changeStatus: Boolean = false
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
                // If there's a change in one of the default address' status
                if (changeStatus) {
                    // Delay for 1.5 seconds to give way for Firestore's write operation
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        // Update the rest of the address information
                        activity.addOrUpdateAddress()
                    }, 1500)
                }
                // For updating the rest of address information
                else {
                    // Prompt that the address was saved in the Firestore
                    activity.addressSavedOrDeleted(true)
                }  // end of if-else

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
                activity.addressSavedOrDeleted(false)
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
    fun getProductQuery(
        fragment: Fragment?, activity: Activity? = null, marketID: String = ""
    ): Query {
        // Access the collection named products
        val productRef = mFSInstance.collection(Constants.PRODUCTS)

        return with(productRef) {
            // Activities only
            if (activity != null) {
                when (activity) {
                    /* Get all the documents where
                     *
                     */
                    is MarketPageActivity -> whereEqualTo(
                        Constants.MARKET_ID, marketID
                    ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                        .whereGreaterThan(Constants.STOCK, 0)

                    /* The default query, get all the documents where the status
                     * code is equal to 1 (In Stock), the stock is greater than 0,
                     * and limit the number of document retrievals to 20.
                     */
                    else -> whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                        .whereGreaterThan(Constants.STOCK, 0)
                        .limit(20)
                }
            }
            // Fragments only
            else {
                // Returns the query, depending on the tab of Product Inventory fragment
                when (fragment) {
                    /* To get all products that are in stock, get all the documents
                     * where vendor ID is equal to the current user ID, the status
                     * code is equal to 1 (In Stock), and the stock is greater than
                     * 0.
                     */
                    is ProductInStockFragment -> whereEqualTo(
                        Constants.MARKET_ID, marketID
                    ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                        .whereGreaterThan(Constants.STOCK, 0)

                    /* To get all products that are sold out, get all the documents
                     * where vendor ID is equal to the current user ID, and the
                     * stock is equal to 0.
                     */
                    is ProductSoldOutFragment -> whereEqualTo(
                        Constants.MARKET_ID, marketID
                    ).whereEqualTo(Constants.STOCK, 0)

                    /* To get all products that are violated, get all the documents
                     * where vendor ID is equal to the current user ID, and the
                     * status code is equal to 2 (Violation).
                     */
                    is ProductViolationFragment -> whereEqualTo(
                        Constants.MARKET_ID, marketID
                    ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_VIOLATION)

                    /* To get all products that are unlisted, get all the documents
                     * where vendor ID is equal to the current user ID, and the
                     * status code is equal to 0 (Unlisted).
                     */
                    is ProductUnlistedFragment -> whereEqualTo(
                        Constants.MARKET_ID, marketID
                    ).whereEqualTo(Constants.STATUS, Constants.PRODUCT_UNLISTED)

                    /* The default query, get all the documents where the status
                     * code is equal to 1 (In Stock), the stock is greater than 0,
                     * and limit the number of document retrievals to 20.
                     */
                    else -> whereEqualTo(Constants.STATUS, Constants.PRODUCT_IN_STOCK)
                        .whereGreaterThan(Constants.STOCK, 0)
                        .limit(20)
                }
            }  // end of if-else

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
        context: Context, productID: String, productHashMap: HashMap<String, Any>,
        util: UtilityClass? = null, fragment: Fragment? = null
    ) {
        // Access the collection named products
        mFSInstance.collection(Constants.PRODUCTS)
            // Access the document named by the selected product id
            .document(productID)
            // Update the values of the specified field
            .update(productHashMap)
            // If it is successful
            .addOnSuccessListener {
                when (context) {
                    /* In Product Editor Activity, it sends the user to the
                     * Product Inventory Activity
                     */
                    is ProductEditorActivity -> context.productSavedPrompt()
                }

                // For fragments only
                if (fragment != null) {
                    when (fragment) {
                        // Show the prompt message that the product is unlisted
                        is ProductInStockFragment,
                        is ProductSoldOutFragment -> {
                            util?.hideProgressDialog()  // Hide the loading message

                            // Display a Toast message
                            util?.toastMessage(
                                fragment.requireContext(),
                                fragment.getString(R.string.msg_product_unlisted)
                            )
                        }

                        // Show the prompt message that the product is relisted
                        is ProductUnlistedFragment -> {
                            util?.hideProgressDialog()  // Hide the loading message

                            // Display a loading message
                            util?.toastMessage(
                                fragment.requireContext(),
                                fragment.getString(R.string.msg_product_relisted)
                            )
                        }
                    }  // end of when

                }  // end of if

            }
            // If failed
            .addOnFailureListener { e ->
                // Closes the loading message in the Product Editor Activity
                when (context) {
                    is ProductEditorActivity -> context.hideProgressDialog()
                }

                // Closes the loading message in the listed fragments below
                if (fragment != null) {
                    when (fragment) {
                        is ProductInStockFragment,
                        is ProductSoldOutFragment,
                        is ProductViolationFragment -> util?.hideProgressDialog()
                    }
                }

                // Log the error
                Log.e(
                    context.javaClass.simpleName,
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
                    context, context.getString(R.string.msg_product_deleted)
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
                        activity,
                        hashMapOf(
                            Constants.VENDOR to true,
                            Constants.MARKET_ID to market.id
                        )
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
                val market = if (document != null && document.exists())
                    document.toObject(Market::class.java)!!
                else
                    null

                when (activity) {
                    /* In Cart Activity, it supplies the market name and
                     * delivery fee output data.
                     */
                    is CartActivity -> activity.setMarketData(market)

                    /* In My Market Activity, it stores the market object
                     * in the said activity.
                     */
                    is MyMarketActivity -> activity.setUserMarket(market)
                }
            }
            // If failed
            .addOnFailureListener { e ->
                /* Hide the loading message and exit the activity in
                 * Cart Activity or My Market Activity
                 */
                when (activity) {
                    is CartActivity -> {
                        activity.hideProgressDialog()
                        activity.finish()
                    }

                    is MyMarketActivity -> {
                        activity.hideProgressDialog()
                        activity.finish()
                    }
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
        activity: Activity, marketID: String, marketHashMap: HashMap<String, Any?>
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
                if (currentCartItems.isNotEmpty()) currentCartItems.clear()
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
                    is CheckoutActivity -> activity.cartUpdatedPrompt()
                }
            }
            // If it failed
            .addOnFailureListener { e ->
                when (activity) {
                    /* Closes the loading message in the Product Description
                     * Activity and Checkout Activity.
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
        util: UtilityClass, isFirstCTA: Boolean = false
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
                    fragment.orderUpdatedPrompt(isFirstCTA)
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
