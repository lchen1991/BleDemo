package com.lchen.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Set;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class LocalBluetoothAdapter {
    private static final String TAG = "LocalBluetoothAdapter";

    private final BluetoothAdapter mAdapter;

    private LocalBluetoothProfileManager mProfileManager;

    private static LocalBluetoothAdapter sInstance;

    private int mState = BluetoothAdapter.ERROR;

    private static final int SCAN_EXPIRATION_MS = 5 * 60 * 1000; // 5 mins

    private long mLastScan;

    private LocalBluetoothAdapter(BluetoothAdapter adapter) {
        mAdapter = adapter;
    }

    void setProfileManager(LocalBluetoothProfileManager manager) {
        mProfileManager = manager;
    }

    /**
     * Get the singleton instance of the LocalBluetoothAdapter. If this device
     * doesn't support Bluetooth, then null will be returned. Callers must be
     * prepared to handle a null return value.
     * @return the LocalBluetoothAdapter object, or null if not supported
     */
    static synchronized LocalBluetoothAdapter getInstance() {
        if (sInstance == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                sInstance = new LocalBluetoothAdapter(adapter);
            }
        }

        return sInstance;
    }

    // Pass-through BluetoothAdapter methods that we can intercept if necessary

    public void cancelDiscovery() {
        mAdapter.cancelDiscovery();
    }

    public boolean enable() {
        return mAdapter.enable();
    }

    public boolean disable() {
        return mAdapter.disable();
    }

    void getProfileProxy(Context context,
                         BluetoothProfile.ServiceListener listener, int profile) {
        mAdapter.getProfileProxy(context, listener, profile);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }

    public String getName() {
        return mAdapter.getName();
    }

    public int getScanMode() {
        return mAdapter.getScanMode();
    }

    public BluetoothLeScanner getBluetoothLeScanner() {
        return mAdapter.getBluetoothLeScanner();
    }

    public int getState() {
        return mAdapter.getState();
    }

    public ParcelUuid[] getUuids() {
        return BluetoothAdapterUtils.getUuids(mAdapter);//mAdapter.getUuids();
    }

    public boolean isDiscovering() {
        return mAdapter.isDiscovering();
    }

    public boolean isEnabled() {
        return mAdapter.isEnabled();
    }

    public int getConnectionState() {
        return BluetoothAdapterUtils.getConnectionState(mAdapter);//mAdapter.getConnectionState();
    }

    public void setDiscoverableTimeout(int timeout) {
        BluetoothAdapterUtils.setDiscoverableTimeout(mAdapter, timeout);
    }

    public void setName(String name) {
        mAdapter.setName(name);
    }

    public void setScanMode(int mode) {
        BluetoothAdapterUtils.setScanMode(mAdapter, mode);
    }

    public boolean setScanMode(int mode, int duration) {
        return BluetoothAdapterUtils.setScanMode(mAdapter, mode, duration);
    }

    public void startScanning(boolean force) {
        // Only start if we're not already scanning
        if (!mAdapter.isDiscovering()) {
            if (!force) {
                // Don't scan more than frequently than SCAN_EXPIRATION_MS,
                // unless forced
                if (mLastScan + SCAN_EXPIRATION_MS > System.currentTimeMillis()) {
                    return;
                }

                // If we are playing music, don't scan unless forced.
                A2dpProfile a2dp = mProfileManager.getA2dpProfile();
                if (a2dp != null && a2dp.isA2dpPlaying()) {
                    return;
                }
            }

            if (mAdapter.startDiscovery()) {
                mLastScan = System.currentTimeMillis();
            }
        }
    }

    public void stopScanning() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    public synchronized int getBluetoothState() {
        // Always sync state, in case it changed while paused
        syncBluetoothState();
        return mState;
    }

    synchronized void setBluetoothStateInt(int state) {
        mState = state;

        if (state == BluetoothAdapter.STATE_ON) {
            // if mProfileManager hasn't been constructed yet, it will
            // get the adapter UUIDs in its constructor when it is.
            if (mProfileManager != null) {
                mProfileManager.setBluetoothStateOn();
            }
        }
    }

    // Returns true if the state changed; false otherwise.
    boolean syncBluetoothState() {
        int currentState = mAdapter.getState();
        if (currentState != mState) {
            setBluetoothStateInt(mAdapter.getState());
            return true;
        }
        return false;
    }

    public boolean setBluetoothEnabled(boolean enabled) {
        boolean success = enabled
                ? mAdapter.enable()
                : mAdapter.disable();

        if (success) {
            setBluetoothStateInt(enabled
                    ? BluetoothAdapter.STATE_TURNING_ON
                    : BluetoothAdapter.STATE_TURNING_OFF);
        } else {
            if (Utils.V) {
                Log.v(TAG, "setBluetoothEnabled call, manager didn't return " +
                        "success for enabled: " + enabled);
            }

            syncBluetoothState();
        }
        return success;
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return mAdapter.getRemoteDevice(address);
    }
}
