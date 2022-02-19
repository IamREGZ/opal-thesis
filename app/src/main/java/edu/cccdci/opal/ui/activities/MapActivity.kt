package edu.cccdci.opal.ui.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import edu.cccdci.opal.R
import edu.cccdci.opal.databinding.ActivityMapBinding
import edu.cccdci.opal.utils.Constants
import edu.cccdci.opal.utils.GeoDirections
import edu.cccdci.opal.utils.GeoDistance
import edu.cccdci.opal.utils.UtilityClass

class MapActivity : UtilityClass(), View.OnClickListener, OnMapReadyCallback,
    GeoDirections.GeoDirectionsResult, GeoDistance.GeoDistanceResult,
    GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraIdleListener {

    private lateinit var binding: ActivityMapBinding
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var mSupportMap: SupportMapFragment
    private var mPolyline: Polyline? = null
    private var mLocationMarkers: List<LatLng>? = null
    private var mMarketName: String? = null
    private var mCurrentMarkerPos: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)

        // Prepare the SupportMapFragment
        mSupportMap = supportFragmentManager
            .findFragmentById(R.id.mpfr_delivery_route_map) as SupportMapFragment
        // Load the map fragment
        mSupportMap.getMapAsync(this@MapActivity)

        // Check if there's an existing parcelable array extra info
        if (intent.hasExtra(Constants.LOCATION_MARKERS_INFO)) {
            // Get data from the parcelable array
            mLocationMarkers = intent.getParcelableArrayExtra(
                Constants.LOCATION_MARKERS_INFO
            )!!.filterIsInstance<LatLng>().toList()
        }

        // Check if there's an existing string info
        if (intent.hasExtra(Constants.MARKET_NAME_DATA)) {
            // Get the string data
            mMarketName = intent.getStringExtra(Constants.MARKET_NAME_DATA)
        }

        // Check if there's an existing parcelable extra info
        if (intent.hasExtra(Constants.CURRENT_MARKER_POS)) {
            // Get the LatLng data
            mCurrentMarkerPos = intent.getParcelableExtra(Constants.CURRENT_MARKER_POS)
        }

        with(binding) {
            setContentView(root)
            // Setups the Action Bar of the current activity
            setupActionBar(tlbMapActivity, false)

            // Change the activity title text
            tvMapTitle.text = if (mLocationMarkers != null)
                getString(R.string.tlb_title_delivery_route)
            else
                getString(R.string.tlb_title_select_location)
        }  // end of with(binding)

    }  // end of onCreate method

    // onClick events are declared here
    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                R.id.btn_select_location -> {
                    // Creates the Shared Preferences
                    val sharedPrefs = getSharedPreferences(
                        Constants.OPAL_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                    // Create the editor for Shared Preferences
                    val spEditor: SharedPreferences.Editor = sharedPrefs.edit()

                    // Shared Preference for Current Marker Position
                    spEditor.putString(
                        Constants.CURRENT_MARKER_POS, Gson().toJson(
                            mCurrentMarkerPos
                        )
                    ).apply()

                    finish()  // Closes the activity
                }
            }  // end of when

        }  // end of if

    }  // end of onClick method

    // Overriding function to set the Map UI of delivery route
    override fun onMapReady(gMap: GoogleMap) {
        mGoogleMap = gMap  // Store the GoogleMap object
        mGoogleMap.clear()  // Clear all the markers set in the map

        // For displaying route between the market and customer's location
        if (mLocationMarkers != null) {
            setMarkersRoute()
        }
        // For location selection only
        else {
            initCurrentLocation()
        }

        mGoogleMap.uiSettings.isMapToolbarEnabled = false
    }  // end of onMapReady method

    // Overriding function to handle events when the map camera is moving
    override fun onCameraMove() {
        mGoogleMap.clear()  // Clear all the markers set in the map

        // Make the centered marker visible, as if it moves along the camera
        binding.ivMapMarkerCentered.visibility = View.VISIBLE
    }  // end of onCameraMove method

    // Overriding function to handle events when the map camera is not moving
    override fun onCameraIdle() {
        // Make the centered marker invisible
        binding.ivMapMarkerCentered.visibility = View.GONE

        mGoogleMap.apply {
            // Update the coordinates of the current marker position
            mCurrentMarkerPos = cameraPosition.target

            // Make the marker visible to the Map UI
            addMarker(MarkerOptions().apply {
                // Set the marker position
                position(mCurrentMarkerPos!!)
                // Disable the draggable functionality of the marker
                draggable(false)
                // Customize the icon image
                icon(
                    BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_map_marker_primary)
                )
            })
        }
    }  // end of onCameraIdle method

    // Overriding function to draw the route between the market and customer's address
    override fun drawRoute(poly: PolylineOptions) {
        // Clear existing polyline to draw a new one
        if (mPolyline != null) mPolyline!!.remove()

        // Draw the route in the Map UI
        mPolyline = mGoogleMap.addPolyline(poly)

        // Variable to set map bounds based on the route size
        val boundsBuilder = LatLngBounds.builder()

        // Determine all the route points
        for (latLngPoint in mPolyline!!.points) boundsBuilder.include(latLngPoint)

        // Focus the Map UI to the entire route between the market and customer's address
        mGoogleMap.moveCamera(
            CameraUpdateFactory
                .newLatLngBounds(boundsBuilder.build(), 75)
        )

        // Get the route's distance and duration
        GeoDistance(this@MapActivity)
            .calculateDistance(
                // Market's location coordinates
                listOf(
                    mLocationMarkers!![0].latitude,
                    mLocationMarkers!![0].longitude
                ),
                // Customer's location coordinates
                listOf(
                    mLocationMarkers!![1].latitude,
                    mLocationMarkers!![1].longitude
                )
            )
    }  // end of drawRoute method

    // Overriding function to get the result of distance and duration calculations
    override fun setDistanceResult(res: String) {
        // Extract the values, separated by commas
        val distRes: List<String> = res.split(',')

        // Set the route distance value
        binding.tvMapRouteDistance.text = getString(
            R.string.delivery_distance_value, distRes[0].toDouble() / 1000
        )
        // Set the route duration value
        binding.tvMapRouteDuration.text = getString(
            R.string.delivery_duration_value, distRes[1].toInt() / 60
        )

        // Make the distance panel visible
        binding.cvMapDistancePanel.visibility = View.VISIBLE
    }  // end of setDistanceResult method

    // Function to set the two markers in the Map UI, also determine the route
    private fun setMarkersRoute() {
        // Set the market marker attributes
        val marketMarker = MarkerOptions().apply {
            // Set the position to the latitude and longitude of market location
            position(mLocationMarkers!![0])
            // Marker title that will show up when it is clicked
            title(mMarketName)
            // Customize the icon image
            icon(
                BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_map_marker_primary)
            )
        }

        // Set the delivery marker attributes
        val deliveryMarker = MarkerOptions().apply {
            // Set the position to the latitude and longitude of delivery address
            position(mLocationMarkers!![1])
            // Marker title that will show up when it is clicked
            title("Delivery Location")
            // Customize the icon image
            icon(
                BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_map_marker_secondary)
            )
        }

        // Make the markers visible to the Map UI
        mGoogleMap.addMarker(marketMarker)
        mGoogleMap.addMarker(deliveryMarker)

        // Get the route directions between the market and delivery address
        GeoDirections(this@MapActivity)
            .getDirections(
                // Market's location coordinates
                listOf(
                    mLocationMarkers!![0].latitude,
                    mLocationMarkers!![0].longitude
                ),
                // Customer's location coordinates
                listOf(
                    mLocationMarkers!![1].latitude,
                    mLocationMarkers!![1].longitude
                )
            )
    }  // end of setMarketDeliveryRoute method

    // Function to initialize current location marker for location selection
    private fun initCurrentLocation() {
        mGoogleMap.apply {
            // Make the marker visible to the Map UI
            addMarker(MarkerOptions().apply {
                // Set the marker position
                position(
                    mCurrentMarkerPos ?:
                    // Jose Rizal's house as the default coordinates
                    LatLng(
                        Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
                    )
                )
                // Disable the draggable functionality of the marker
                draggable(false)
                // Customize the icon image
                icon(
                    BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_map_marker_primary)
                )
            })

            // Focus the Map UI to the position of marker
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    mCurrentMarkerPos ?:
                    // Jose Rizal's house as the default coordinates
                    LatLng(
                        Constants.DEFAULT_LATITUDE, Constants.DEFAULT_LONGITUDE
                    ), 18f
                )
            )

            // Make Select Location Button visible
            binding.btnSelectLocation.visibility = View.VISIBLE
            // Click event for Select Location Button
            binding.btnSelectLocation.setOnClickListener(this@MapActivity)

            // Map Camera Move Event
            setOnCameraMoveListener(this@MapActivity)
            // Map Camera Idle Event
            setOnCameraIdleListener(this@MapActivity)
        }  // end of apply

    }  // end of initCurrentLocation method

}  // end of MapActivity class