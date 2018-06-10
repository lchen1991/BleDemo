package com.lchen.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.lchen.ble.R;

import java.util.ArrayList;
import java.util.List;

/**
 * HeadsetProfile handles Bluetooth HFP and Headset profiles.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class HeadsetProfile implements LocalBluetoothProfile {
    private static final String TAG = "HeadsetProfile";
    private static boolean V = true;

    public static final int PRIORITY_ON = 100;

    public static final int PRIORITY_OFF = 0;

    private BluetoothHeadset mService;
    private boolean mIsProfileReady;

    private final LocalBluetoothAdapter mLocalAdapter;
    private final CachedBluetoothDeviceManager mDeviceManager;
    private final LocalBluetoothProfileManager mProfileManager;

    static final ParcelUuid[] UUIDS = {
            BluetoothUuid.HSP,
            BluetoothUuid.Handsfree,
    };

    static final String NAME = "HEADSET";

    // Order of this profile in device profiles list
    private static final int ORDINAL = 0;

    // These callbacks run on the main thread.
    private final class HeadsetServiceListener
            implements BluetoothProfile.ServiceListener {

        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (V) Log.d(TAG, "Bluetooth service connected");
            mService = (BluetoothHeadset) proxy;
            // We just bound to the service, so refresh the UI for any connected HFP devices.
            List<BluetoothDevice> deviceList = mService.getConnectedDevices();
            while (!deviceList.isEmpty()) {
                BluetoothDevice nextDevice = deviceList.remove(0);
                CachedBluetoothDevice device = mDeviceManager.findDevice(nextDevice);
                // we may add a new device here, but generally this should not happen
                if (device == null) {
                    Log.w(TAG, "HeadsetProfile found new device: " + nextDevice);
                    device = mDeviceManager.addDevice(mLocalAdapter, mProfileManager, nextDevice);
                }
                device.onProfileStateChanged(HeadsetProfile.this,
                        BluetoothProfile.STATE_CONNECTED);
                device.refresh();
            }

            mProfileManager.callServiceConnectedListeners();
            mIsProfileReady = true;
        }

        public void onServiceDisconnected(int profile) {
            if (V) Log.d(TAG, "Bluetooth service disconnected");
            mProfileManager.callServiceDisconnectedListeners();
            mIsProfileReady = false;
        }
    }

    public boolean isProfileReady() {
        return mIsProfileReady;
    }

    HeadsetProfile(Context context, LocalBluetoothAdapter adapter,
                   CachedBluetoothDeviceManager deviceManager,
                   LocalBluetoothProfileManager profileManager) {
        mLocalAdapter = adapter;
        mDeviceManager = deviceManager;
        mProfileManager = profileManager;
        mLocalAdapter.getProfileProxy(context, new HeadsetServiceListener(),
                BluetoothProfile.HEADSET);
    }

    public boolean isConnectable() {
        return true;
    }

    public boolean isAutoConnectable() {
        return true;
    }

    public boolean connect(BluetoothDevice device) {
        if (mService == null) return false;
        List<BluetoothDevice> sinks = mService.getConnectedDevices();
        if (sinks != null) {
            for (BluetoothDevice sink : sinks) {
                Log.d(TAG, "Not disconnecting device = " + sink);
            }
        }
        return BluetoothProfileUtils.connect(mService, device);
    }

    public boolean disconnect(BluetoothDevice device) {
        if (mService == null) return false;
        List<BluetoothDevice> deviceList = mService.getConnectedDevices();
        if (!deviceList.isEmpty()) {
            for (BluetoothDevice dev : deviceList) {
                if (dev.equals(device)) {
                    if (V) Log.d(TAG, "Downgrade priority as user" +
                            "is disconnecting the headset");
                    // Downgrade priority as user is disconnecting the headset.
                    if (BluetoothProfileUtils.getPriority(mService, device) > PRIORITY_ON) {
                        BluetoothProfileUtils.setPriority(mService, device, PRIORITY_ON);
                    }
                    return BluetoothProfileUtils.disconnect(mService, device);
                }
            }
        }
        return false;
    }

    public int getConnectionStatus(BluetoothDevice device) {
        if (mService == null) return BluetoothProfile.STATE_DISCONNECTED;
        List<BluetoothDevice> deviceList = mService.getConnectedDevices();
        if (!deviceList.isEmpty()) {
            for (BluetoothDevice dev : deviceList) {
                if (dev.equals(device)) {
                    return mService.getConnectionState(device);
                }
            }
        }
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    public boolean isPreferred(BluetoothDevice device) {
        if (mService == null) return false;
        return BluetoothProfileUtils.getPriority(mService, device) > PRIORITY_OFF;
    }

    public int getPreferred(BluetoothDevice device) {
        if (mService == null) return PRIORITY_OFF;
        return BluetoothProfileUtils.getPriority(mService, device);
    }

    public void setPreferred(BluetoothDevice device, boolean preferred) {
        if (mService == null) return;
        if (preferred) {
            if (BluetoothProfileUtils.getPriority(mService, device) < PRIORITY_ON) {
                BluetoothProfileUtils.setPriority(mService, device, PRIORITY_ON);
            }
        } else {
            BluetoothProfileUtils.setPriority(mService, device, PRIORITY_OFF);
        }
    }

    public List<BluetoothDevice> getConnectedDevices() {
        if (mService == null) return new ArrayList<BluetoothDevice>(0);
        return mService.getDevicesMatchingConnectionStates(
                new int[]{BluetoothProfile.STATE_CONNECTED,
                        BluetoothProfile.STATE_CONNECTING,
                        BluetoothProfile.STATE_DISCONNECTING});
    }

    public String toString() {
        return NAME;
    }

    public int getOrdinal() {
        return ORDINAL;
    }

    public int getNameResource(BluetoothDevice device) {
        return R.string.bluetooth_profile_headset;
    }

    public int getSummaryResourceForDevice(BluetoothDevice device) {
        int state = getConnectionStatus(device);
        switch (state) {
            case BluetoothProfile.STATE_DISCONNECTED:
                return R.string.bluetooth_headset_profile_summary_use_for;

            case BluetoothProfile.STATE_CONNECTED:
                return R.string.bluetooth_headset_profile_summary_connected;

            default:
                return Utils.getConnectionStateSummary(state);
        }
    }

    public int getDrawableResource(BluetoothClass btClass) {
        return R.drawable.ic_launcher;
    }

    protected void finalize() {
        if (V) Log.d(TAG, "finalize()");
        if (mService != null) {
            try {
                BluetoothAdapter.getDefaultAdapter().closeProfileProxy(BluetoothProfile.HEADSET,
                        mService);
                mService = null;
            } catch (Throwable t) {
                Log.w(TAG, "Error cleaning up HID proxy", t);
            }
        }
    }
}