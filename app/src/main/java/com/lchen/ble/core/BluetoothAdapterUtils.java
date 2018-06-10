package com.lchen.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.os.ParcelUuid;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/9.
 */

public class BluetoothAdapterUtils {

    public static ParcelUuid[] getUuids(BluetoothAdapter bluetoothAdapter) {
        ParcelUuid[] parcelUuids = null;
        if (bluetoothAdapter == null) {
            return parcelUuids;
        }
        try {
            Method connect = bluetoothAdapter.getClass().getDeclaredMethod("getUuids");
            if (connect != null) {
                connect.setAccessible(true);
                parcelUuids = (ParcelUuid[]) connect.invoke(bluetoothAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parcelUuids;
    }

    public static int getConnectionState(BluetoothAdapter bluetoothAdapter) {
        int state = -1;
        if (bluetoothAdapter == null) {
            return state;
        }
        try {
            Method connect = bluetoothAdapter.getClass().getDeclaredMethod("getConnectionState");
            if (connect != null) {
                connect.setAccessible(true);
                state = (int) connect.invoke(bluetoothAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return state;
    }

    public static void setDiscoverableTimeout(BluetoothAdapter bluetoothAdapter, int timeout) {
        if (bluetoothAdapter == null) {
            return;
        }
        try {
            Method connect = bluetoothAdapter.getClass().getDeclaredMethod("setDiscoverableTimeout", Integer.class);
            if (connect != null) {
                connect.setAccessible(true);
                connect.invoke(bluetoothAdapter, timeout);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setScanMode(BluetoothAdapter bluetoothAdapter, int mode) {
        if (bluetoothAdapter == null) {
            return;
        }
        try {
            Method setScanMode = bluetoothAdapter.getClass().getDeclaredMethod("setScanMode", Integer.class);
            if (setScanMode != null) {
                setScanMode.setAccessible(true);
                setScanMode.invoke(bluetoothAdapter, mode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean setScanMode(BluetoothAdapter bluetoothAdapter, int mode, int duration) {
        if (bluetoothAdapter == null) {
            return false;
        }
        try {
            Method scanMode = bluetoothAdapter.getClass().getDeclaredMethod("setScanMode", Integer.class, Integer.class);
            if (scanMode != null) {
                scanMode.setAccessible(true);
                 return (boolean) scanMode.invoke(bluetoothAdapter, mode, duration);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
