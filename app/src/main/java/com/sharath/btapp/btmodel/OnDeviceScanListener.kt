package com.sharath.btapp.btmodel
import com.sharath.btapp.btmodel.BleDeviceData

interface OnDeviceScanListener {

    /**
     * Scan Completed -
     *
     * @param deviceDataList - Send available devices as a list to the init Activity
     * The List Contain, device name and mac address,
     */
     fun onScanCompleted(deviceDataList: BleDeviceData)
}