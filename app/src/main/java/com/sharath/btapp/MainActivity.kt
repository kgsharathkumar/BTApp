package com.sharath.btapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var mScanButton: Button
    private lateinit var mBtnReadConnectionChar: Button
    private lateinit var mBtnReadBatteryLevel: Button
    private lateinit var mBtnReadEmergency: Button
    private lateinit var mBtnWriteEmergency: Button
    private lateinit var mBtnWriteConnection: Button
    private lateinit var mBtnWriteBatteryLevel: Button
    private lateinit var mTvResult: TextView

    private val REQUEST_LOCATION_PERMISSION = 2018
    private val TAG = "MainActivity"
    private val REQUEST_ENABLE_BT = 1000




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mScanButton = findViewById<Button>(R.id.btn_scan)

        mBtnReadConnectionChar = findViewById<Button>(R.id.btn_read_connection)
        mBtnReadEmergency = findViewById(R.id.btn_read_emergency)
        mBtnReadBatteryLevel = findViewById(R.id.btn_read_battery)
        mBtnWriteEmergency = findViewById(R.id.btn_write_emergency)
        mBtnWriteConnection = findViewById<Button>(R.id.btn_write_connection)
        mBtnWriteBatteryLevel = findViewById(R.id.btn_write_battery)
        mTvResult = findViewById(R.id.tv_result)

        mScanButton.setOnClickListener(this)
        mBtnReadBatteryLevel.setOnClickListener(this)
        mBtnReadConnectionChar.setOnClickListener(this)
        mBtnWriteEmergency.setOnClickListener(this)
        mBtnWriteConnection.setOnClickListener(this)
        mBtnWriteBatteryLevel.setOnClickListener(this)
        mBtnReadEmergency.setOnClickListener(this)

        // checkLocationPermission()

    }

    private fun checkLocationPermission() {
        TODO("Not yet implemented")
//
//        if (isAboveMarshmallow()) {
//            when {
//                isLocationPermissionEnabled() -> initBLEModule()
//                ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
//                else -> requestLocationPermission()
//            }
//        } else {
//            initBLEModule()
//        }


    }

    private fun initBLEModule() {
        TODO("Not yet implemented")
    }

    private fun requestLocationPermission() {
        TODO("Not yet implemented")
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
    }

    private fun displayRationale() {
        TODO("Not yet implemented")

        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_disabled))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

    private fun isLocationPermissionEnabled(): Boolean {
        TODO("Not yet implemented")
        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    private fun isAboveMarshmallow(): Boolean {
        TODO("Not yet implemented")
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }


    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_scan -> toastInfo("Scanning BT")

        }
    }

    private fun toastInfo(toastMsg: String) {
        val myToast = Toast.makeText(applicationContext,toastMsg,Toast.LENGTH_SHORT)
        myToast.setGravity(Gravity.LEFT,200,200)
        myToast.show()
    }
}