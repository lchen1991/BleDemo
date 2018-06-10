package com.lchen.ble.core;

/**
 * BluetoothCallback provides a callback interface for the settings
 * UI to receive events from {@link BluetoothEventManager}.
 */
public interface BluetoothCallback {
    void onBluetoothStateChanged(int bluetoothState);

    void onScanningStateChanged(boolean started);

    void onDeviceAdded(CachedBluetoothDevice cachedDevice);

    void onDeviceDeleted(CachedBluetoothDevice cachedDevice);

    void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState);

    void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state);
}