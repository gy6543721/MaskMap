package com.levilin.maskmap

import android.Manifest.permission.*
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings.*
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.levilin.maskmap.data.MaskInfo
import com.levilin.maskmap.databinding.ActivityMapBinding
import com.levilin.maskmap.utility.OkHttpUtility
import okhttp3.Response


class MapActivity : AppCompatActivity(), OnMapReadyCallback, OnInfoWindowClickListener {
    private lateinit var binding: ActivityMapBinding
    private lateinit var mContext: Context
    private lateinit var mLocationProviderClient: FusedLocationProviderClient
    private var googleMap: GoogleMap? = null
    private var locationPermissionGranted = false
    private var maskInfo: MaskInfo? = null
//    private var mCurrLocationMarker: Marker? = null

    //Default location set to Taipei 101
    private var currentLocation = LatLng(25.0338483, 121.5645283)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set Menu Button
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mContext = this
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getMaskData()
        userCurrentLocationButton()
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Check if permission is granted
            Toast.makeText(this, resources.getString(R.string.location_permission), Toast.LENGTH_SHORT).show()
            locationPermissionGranted = true
            checkGPSState()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(this)
                .setMessage(resources.getString(R.string.location_permission))
                .setPositiveButton(resources.getString(R.string.alert_title_ok)) { _, _ ->
                    ActivityCompat.requestPermissions(this, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
                }
                .setNegativeButton(resources.getString(R.string.alert_title_cancel)) { _, _ ->
                    // Jump to Activity that doesn't need location permission to run
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
    }

    private fun checkGPSState() {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(mContext)
                .setTitle(resources.getString(R.string.gps_permission))
                .setMessage(resources.getString(R.string.gps_permission_denied))
                .setPositiveButton(resources.getString(R.string.alert_title_ok), DialogInterface.OnClickListener { _, _ ->
                    val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, REQUEST_GPS_PERMISSION)
                })
                .setNegativeButton(resources.getString(R.string.alert_title_cancel), null)
                .show()
        } else {
            getDeviceLocation()
            Toast.makeText(this, resources.getString(R.string.gps_permission_accepted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationRequest = LocationRequest()
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                //Setting update frequency
                locationRequest.interval = 1000
                //Setting update time, if not it will continuously update
                //locationRequest.numUpdates = 1
                mLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            //Show Button


                            currentLocation = LatLng(
                                locationResult.lastLocation.latitude,
                                locationResult.lastLocation.longitude
                            )

                            Log.d(
                                "TAG",
                                "$currentLocation"
                            )

//                            googleMap?.moveCamera(
//                                CameraUpdateFactory.newLatLngZoom(
//                                    currentLocation, 15f
//                                )
//                            )
//                            //Clear previous location
//                            mCurrLocationMarker?.remove()
//                            mCurrLocationMarker =googleMap?.addMarker(
//                                MarkerOptions()
//                                    .position(currentLocation).title(resources.getString(R.string.current_location))
//                                    .snippet(currentLocation.toString()).icon(
//                                        getBitmapDescriptor(
//                                            mContext,
//                                            R.drawable.ic_baseline_mask,
//                                            60.px,
//                                            60.px
//                                        )
//                                    )
//                            )
//                            //Set customized window style
//                            googleMap?.setInfoWindowAdapter(MapInfoWindowAdapter(mContext))
//                            //Show title name
//                            mCurrLocationMarker?.showInfoWindow()
//                            //Show location spot
//                            googleMap?.isMyLocationEnabled = true
                        }
                    },
                    null)
            } else {
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getMaskData() {
        binding.progressBar.visibility = View.VISIBLE
        OkHttpUtility.mOkHttpUtility.getAsync(MASK_DATA_URL, object : OkHttpUtility.AsyncCallback {
            override fun onResponse(response: Response) {
                val maskData = response.body?.string()
                maskInfo = Gson().fromJson(maskData, MaskInfo::class.java)
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    addAllMaker()
                }
            }
            override fun onFailure(e: okio.IOException) {
                binding.progressBar.visibility = View.GONE
                Log.d("TAG", "onFailure: $e")
            }
        })
    }

    private fun addAllMaker() {
        maskInfo?.features?.forEach { feature ->
            val pinMarker = googleMap?.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            feature.geometry.coordinates[1],
                            feature.geometry.coordinates[0],
                        )
                    )
                    .title(feature.properties.name)
                    .snippet("${feature.properties.mask_adult}," + "${feature.properties.mask_child}")
//                    .icon(getBitmapDescriptor(
//                        mContext,
//                        R.drawable.ic_baseline_mask,
//                        60.px,
//                        60.px
//                    ))
            )
        }
        getLocationPermission()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty()) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //Location permission granted
                        locationPermissionGranted = true

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                            //Location permission is permanently denied
                            Toast.makeText(this, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()

                            AlertDialog.Builder(this)
                                .setTitle(resources.getString(R.string.location_permission))
                                .setMessage(resources.getString(R.string.location_permission_denied))
                                .setPositiveButton(resources.getString(R.string.alert_title_ok)) { _, _ ->
                                    val intent = Intent(ACTION_LOCATION_SOURCE_SETTINGS)
                                    startActivityForResult(intent, REQUEST_LOCATION_PERMISSION)
                                }
                                .setNegativeButton(resources.getString(R.string.alert_title_cancel)) { _, _ ->
                                    // Jump to Activity that doesn't need location permission to run
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                }
                                .show()
                        } else {
                            //Location permission is temporarily denied
                            Toast.makeText(this, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
                            requestLocationPermission()
                        }
                    }
                }
            }
        }
    }

    private fun userCurrentLocationButton() {
        binding.myLocationButton.setOnClickListener {
            setCurrentLocation(currentLocation)
        }
    }

    private fun setCurrentLocation(currentLocation: LatLng) {
        binding.myLocationButton.setOnClickListener {
            googleMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    currentLocation, 15f
                )
            )
        }
    }

    // Handle user case when user come back from setting
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                getLocationPermission()
            }
            REQUEST_GPS_PERMISSION -> {
                checkGPSState()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                currentLocation, 15f
            )
        )
        googleMap.setInfoWindowAdapter(MapInfoWindowAdapter(mContext))
        googleMap.setOnInfoWindowClickListener(this)
    }

    override fun onInfoWindowClick(p0: Marker?) {
        p0?.title?.let { title ->
            val filterData =
                maskInfo?.features?.filter {
                    it.properties.name == (title)
                }

            if (filterData?.size!! > 0) {
                val intent = Intent(this, CardInfoActivity::class.java)
                intent.putExtra("data", filterData.first())
                startActivity(intent)
            } else {
                Log.d("TAG", "${resources.getString(R.string.alert_title_error)}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        return true
    }
}
