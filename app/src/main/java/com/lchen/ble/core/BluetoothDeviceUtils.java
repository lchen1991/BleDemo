package com.lchen.ble.core;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/9.
 */

public class BluetoothDeviceUtils {

    public static String getAliasName(BluetoothDevice device) {
        if (device == null) {
            return null;
        }
        try {
            Method aliasName = device.getClass().getDeclaredMethod("getAliasName");
            if (aliasName != null) {
                aliasName.setAccessible(true);
                return (String) aliasName.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setAliasName(BluetoothDevice device, String name) {
        if (device == null) {
            return;
        }
        try {
            Method aliasName = device.getClass().getDeclaredMethod("setAlias", String.class);
            if (aliasName != null) {
                aliasName.setAccessible(true);
                aliasName.invoke(device, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelBondProcess(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        try {
            Method cancelBondProcess = device.getClass().getDeclaredMethod("cancelBondProcess");
            if (cancelBondProcess != null) {
                cancelBondProcess.setAccessible(true);
                cancelBondProcess.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean removeBond(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            Method cancelBondProcess = device.getClass().getDeclaredMethod("removeBond");
            if (cancelBondProcess != null) {
                cancelBondProcess.setAccessible(true);
                return (boolean) cancelBondProcess.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isBluetoothDock(BluetoothDevice device) {
        if (device == null) {
            return false;
        }
        try {
            Method bluetoothDock = device.getClass().getDeclaredMethod("isBluetoothDock");
            if (bluetoothDock != null) {
                bluetoothDock.setAccessible(true);
                return (boolean) bluetoothDock.invoke(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
