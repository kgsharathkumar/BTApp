package com.sharath.btapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sharath.btapp.btmodel.*
import com.sharath.btapp.btmodel.BLEConnectionManager.readBatteryLevel
import com.sharath.btapp.btmodel.BLEConnectionManager.readEmergencyGatt
import com.sharath.btapp.btmodel.BLEConnectionManager.readMissedConnection
import com.sharath.btapp.btmodel.BLEConnectionManager.writeMissedConnection


class MainActivity : AppCompatActivity(), View.OnClickListener, OnDeviceScanListener {


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

    private var mDeviceAddress: String = ""

    override fun onScanCompleted(deviceDataList: BleDeviceData) {

        //Initiate a dialog Fragment from here and ask the user to select his device
        // If the application already know the Mac address, we can simply call connect device

        mDeviceAddress = deviceDataList.mDeviceAddress
        BLEConnectionManager.connect(deviceDataList.mDeviceAddress)

    }




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

        checkLocationPermission()

    }

    private fun checkLocationPermission() {

        if (isAboveMarshmallow()) {
            when {
                isLocationPermissionEnabled() -> initBLEModule()
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) -> displayRationale()
                else -> requestLocationPermission()
            }
        } else {
            initBLEModule()
        }


    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
            REQUEST_LOCATION_PERMISSION)
    }

    private fun displayRationale() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.location_permission_disabled))
            .setPositiveButton(getString(R.string.ok)
            ) { _, _ -> requestLocationPermission() }
            .setNegativeButton(getString(R.string.cancel)
            ) { _, _ -> }
            .show()
    }

    private fun isLocationPermissionEnabled(): Boolean {

        return ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    }

    private fun isAboveMarshmallow(): Boolean {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     *After receive the Location Permission, the Application need to initialize the
     * BLE Module and BLE Service
     */
    private fun initBLEModule() {
        // BLE initialization
        if (!BLEDeviceManager.init(this)) {
            Toast.makeText(this, "BLE NOT SUPPORTED", Toast.LENGTH_SHORT).show()
            return
        }
        registerServiceReceiver()
        BLEDeviceManager.setListener(this)

        if (!BLEDeviceManager.isEnabled()) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

        BLEConnectionManager.initBLEService(this@MainActivity)
    }

    private fun registerServiceReceiver() {
        this.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())

    }


    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when {
                BLEConstants.ACTION_GATT_CONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_CONNECTED ")
                    BLEConnectionManager.findBLEGattService(this@MainActivity)
                }
                BLEConstants.ACTION_GATT_DISCONNECTED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_DISCONNECTED ")
                }
                BLEConstants.ACTION_GATT_SERVICES_DISCOVERED.equals(action) -> {
                    Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED ")
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    BLEConnectionManager.findBLEGattService(this@MainActivity)
                }
                BLEConstants.ACTION_DATA_AVAILABLE.equals(action) -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    val uuId = intent.getStringExtra(BLEConstants.EXTRA_UUID)
                    Log.i(TAG, "ACTION_DATA_AVAILABLE $data")

                }
                BLEConstants.ACTION_DATA_WRITTEN.equals(action) -> {
                    val data = intent.getStringExtra(BLEConstants.EXTRA_DATA)
                    Log.i(TAG, "ACTION_DATA_WRITTEN ")
                }
            }
        }
    }

    /**
     * Intent filter for Handling BLEService broadcast.
     */
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BLEConstants.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BLEConstants.ACTION_DATA_WRITTEN)

        return intentFilter
    }

    /**
     * Unregister GATT update receiver
     */
    private fun unRegisterServiceReceiver() {
        try {
            this.unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            //May get an exception while user denies the permission and user exists the app
            Log.e(TAG, e.message.toString())
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        BLEConnectionManager.disconnect()
        unRegisterServiceReceiver()
    }



    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.btn_scan -> {
                toastInfo("Scanning BT");
                scanDevice(false);
            }
            R.id.btn_read_connection ->
                readMissedConnection()
            R.id.btn_read_battery ->
                readBatteryLevel()

            R.id.btn_read_emergency ->
                readEmergencyGatt()

            R.id.btn_write_emergency ->
                writeEmergency()

            R.id.btn_write_battery ->
                writeBattery()

            R.id.btn_write_connection ->
                writeMissedConnection()

        }
    }

    private fun writeEmergency() {
        BLEConnectionManager.writeEmergencyGatt("0xfe");
    }

    private fun writeBattery() {
        BLEConnectionManager.writeBatteryLevel("100")
    }

    private fun writeMissedConnection() {
        BLEConnectionManager.writeMissedConnection("0x00")
    }

    private fun readMissedConnection() {
        BLEConnectionManager.readMissedConnection(getString(R.string.char_uuid_missed_calls))
    }

    private fun readBatteryLevel() {
        BLEConnectionManager.readBatteryLevel(getString(R.string.char_uuid_emergency))
    }

    private fun readEmergencyGatt() {
        BLEConnectionManager.readEmergencyGatt(getString(R.string.char_uuid_emergency))
    }

    private fun scanDevice(isContinuesScan: Boolean) {
        if (!mDeviceAddress.isNullOrEmpty()) {
            connectDevice()
        } else {
            BLEDeviceManager.scanBLEDevice(isContinuesScan)
        }
    }

    private fun connectDevice() {
        Handler().postDelayed({
            BLEConnectionManager.initBLEService(this@MainActivity)
            if (BLEConnectionManager.connect(mDeviceAddress)) {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "DEVICE CONNECTION FAILED", Toast.LENGTH_SHORT).show()
            }
        }, 100)
    }

    private fun toastInfo(toastMsg: String) {
        val myToast = Toast.makeText(applicationContext,toastMsg,Toast.LENGTH_SHORT)
        myToast.setGravity(Gravity.LEFT,200,200)
        myToast.show()
    }
}