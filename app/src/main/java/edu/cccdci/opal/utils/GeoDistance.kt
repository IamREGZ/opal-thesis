package edu.cccdci.opal.utils

import android.app.Activity
import android.os.Handler
import android.os.Looper
import edu.cccdci.opal.R
import edu.cccdci.opal.ui.activities.CheckoutActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

class GeoDistance(private val activity: Activity) {

    private val mExecutor = Executors.newSingleThreadExecutor()
    private val mHandler = Handler(Looper.getMainLooper())
    private val mGeo: GeoResult = activity as GeoResult

    // Function to calculate the travel duration and distance between two locations
    fun calculateDirections(origin: List<Double>, destination: List<Double>) {
        // Variable to store the result of distance and duration calculation
        var result: String? = null
        // Variable where HTTP requests take place
        var connection: HttpURLConnection? = null

        // Perform the following codes
        mExecutor.execute {
            try {
                // Create a URL for requesting a HTTP to Distance Matrix API
                val distURL = URL(
                    Constants.getDistanceMatrixURL(activity, origin, destination)
                )

                // Open the HTTP connection
                connection = distURL.openConnection() as HttpURLConnection
                // Set the request method as GET
                connection!!.requestMethod = Constants.GET_REQUEST_METHOD
                // Start the HTTP connection (requires Internet)
                connection!!.connect()

                // If the connection is success
                if (connection!!.responseCode == HttpURLConnection.HTTP_OK) {
                    // Variable to read streams of input
                    val br = BufferedReader(
                        InputStreamReader(connection!!.inputStream)
                    )
                    // Variable to receive String inputs
                    val sb = StringBuilder()

                    // Begin the String extraction process
                    var line = br.readLine()

                    while (line != null) {
                        sb.append(line)  // Add the String line
                        // Continue extracting until there's nothing left
                        line = br.readLine()
                    }

                    val dist: String  // Distance variable
                    val dur: String  // Duration variable

                    // Get the root object
                    JSONObject(sb.toString())
                        // Get the array named rows
                        .getJSONArray(Constants.ROWS)
                        // Get the first element of the array (JSON object)
                        .getJSONObject(0)
                        // Get the array named elements
                        .getJSONArray(Constants.ELEMENTS)
                        // Get the first element of the array (JSON object)
                        .getJSONObject(0).let {
                            // Get the value of distance
                            dist = it.getJSONObject(Constants.DISTANCE)
                                .getString(Constants.VALUE)
                            // Get the value of duration
                            dur = it.getJSONObject(Constants.DURATION)
                                .getString(Constants.VALUE)
                        }

                    // Store the result ("distance,duration")
                    result = "$dist,$dur"
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()  // Log the exception
            } catch (e: IOException) {
                e.printStackTrace()  // Log the exception
            } catch (e: JSONException) {
                e.printStackTrace()  // Log the exception
            } finally {
                // Disconnect afterwards (if connection is not null)
                if (connection != null)
                    connection!!.disconnect()
            }  // end of try-catch-finally

            // Codes to perform after execution process
            mHandler.post {
                // Pass the result if it is not null
                if (result != null) {
                    mGeo.setDistanceResult(result!!)
                }
                // Display an error SnackBar if the result is null
                else {
                    when (activity) {
                        is CheckoutActivity -> activity.showSnackBar(
                            activity,
                            activity.getString(R.string.err_failed_get_direction),
                            true
                        )
                    }
                }  // end of if-else
            }  // end of post
        }  // end of execute

    }  // end of calculateDirections method

    // Interface to store the results
    internal interface GeoResult {
        // Abstract function to set the distance and duration result in respective views
        fun setDistanceResult(res: String)
    }  // end of GeoResult interface

}  // end of GeoDistance class