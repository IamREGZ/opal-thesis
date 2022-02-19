package edu.cccdci.opal.utils

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.MapActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class GeoDirections(private val activity: Activity) {

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())
    private val mGeoDirections = activity as GeoDirectionsResult

    fun getDirections(origin: List<Double>, destination: List<Double>) {
        // Variable to store the result of directions route
        var result: List<List<LatLng>>? = null
        // Variable where HTTP requests take place
        var connection: HttpURLConnection? = null
        // Variable to read data from URL
        var inputStream: InputStream? = null
        // Variable to read streams of input
        var br: BufferedReader? = null

        // Perform the following codes
        mExecutor.execute {
            try {
                // Create a URL for requesting a HTTP to Directions API
                val directionURL = URL(
                    Constants.getMapsURL(
                        activity, origin, destination, Constants.DIRECTIONS
                    )
                )

                // Open the HTTP connection
                connection = directionURL.openConnection() as HttpURLConnection
                // Set the request method as GET
                connection!!.requestMethod = Constants.GET_REQUEST_METHOD
                // Start the HTTP connection (requires Internet)
                connection!!.connect()

                // If the connection is success
                if (connection!!.responseCode == HttpURLConnection.HTTP_OK) {
                    // Set the input stream
                    inputStream = connection!!.inputStream
                    // Set the BufferedReader
                    br = BufferedReader(InputStreamReader(inputStream))

                    // Variable to receive String inputs
                    val sb = StringBuilder()

                    // Begin the String extraction process
                    var line = br!!.readLine()
                    while (line != null) {
                        sb.append(line)  // Add the String line
                        // Continue extracting until there's nothing left
                        line = br!!.readLine()
                    }

                    // Store the result (list of polyline paths)
                    result = listOf(getPaths(sb.toString()))
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()  // Log the exception
            } catch (e: IOException) {
                e.printStackTrace()  // Log the exception
            } catch (e: JSONException) {
                e.printStackTrace()  // Log the exception
            } finally {
                // Stops the input stream (if it is not null)
                if (inputStream != null) inputStream!!.close()

                // Closes the BufferedReader (if it is not null)
                if (br != null) br!!.close()

                // Disconnect the HTTP connection (if it is not null)
                if (connection != null) connection!!.disconnect()
            }  // end of try-catch-finally

            // Codes to perform after execution process
            mHandler.post {
                // Create and pass the polyline value if it is not null or empty
                if (!result!!.isNullOrEmpty()) {
                    val polyline = PolylineOptions()

                    // Loop through the list of decoded coordinates
                    for (i in result!!.indices) {
                        // Get the decoded coordinates
                        polyline.addAll(result!![i])
                        // Set the line width
                        polyline.width(12f)
                        // Set the line color
                        polyline.color(Color.parseColor(Constants.APP_GREEN))
                        // Disable geodesic measurements
                        polyline.geodesic(false)
                    }

                    mGeoDirections.drawRoute(polyline)
                }
                // Display an error SnackBar if the result is null
                else {
                    when (activity) {
                        is MapActivity -> activity.showSnackBar(
                            activity,
                            activity.getString(R.string.err_failed_draw_route),
                            true
                        )
                    }
                }  // end of if-else
            }  // end of post

        }  // end of execute

    }  // end of getDirections method

    // Function to return the List of paths by route coordinates
    private fun getPaths(rootObj: String): List<LatLng> {
        // Variable to return the list of decoded polyline routes
        val path: MutableList<LatLng> = mutableListOf()

        // Get the root object
        JSONObject(rootObj)
            // Get the array named routes
            .getJSONArray(Constants.ROUTES)
            // Get the first element of the array (JSON object)
            .getJSONObject(0)
            // Get the array named legs
            .getJSONArray(Constants.LEGS)
            // Get the first element of the array (JSON object)
            .getJSONObject(0)
            // Get the array named steps
            .getJSONArray(Constants.STEPS).let {
                // Loop through the elements of steps JSON Array
                for (i in 0 until it.length()) {
                    // Get the decoded polyline coordinates for accurate routes
                    path.addAll(
                        decodePoly(
                            // Get the current element of the array (JSON object)
                            it.getJSONObject(i)
                                // Get the object named polyline
                                .getJSONObject(Constants.POLYLINE)
                                // Get the string value of points
                                .getString(Constants.POINTS)
                        )
                    )
                }  // end of for
            }

        return path
    }  // end of getPaths method

    // Function to decode the encoded polyline points into defined coordinates
    private fun decodePoly(encoded: String): List<LatLng> {
        // Variable to return the list of all decoded polyline points
        val poly: MutableList<LatLng> = mutableListOf()
        // Initial values
        var index = 0
        var lat = 0
        var lng = 0

        // Loop through the encoded String
        while (index < encoded.length) {
            var b: Int  // Binary value
            var shift = 0  // How many bits to shift
            var result = 0  // Binary result

            // Decoding latitude bits
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            // Set the decoded latitude
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            // Reset to initial values for longitude
            shift = 0
            result = 0

            // Decoding longitude bits
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)

            // Set the decoded longitude
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            // Store the polyline coordinates
            val polyLatLng = LatLng(
                lat.toDouble() / 1E5,
                lng.toDouble() / 1E5
            )
            poly.add(polyLatLng)
        }

        return poly
    }  // end of decodePoly method

    /**
     * Interface to draw the resulting route between two locations
     */
    internal interface GeoDirectionsResult {
        // Abstract function to draw the route between two locations
        fun drawRoute(poly: PolylineOptions)
    }  // end of GeoDirectionsResult interface

}  // end of GeoDirections class