package com.lchen.ble.core;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;

import java.lang.reflect.Method;

/**
 * Created by chenlei on 2018/6/10.
 */

public class BluetoothProfileUtils {

    public static boolean disconnect(BluetoothHeadset proxy, BluetoothDevice device) {
        if (proxy == null) {
            return false;
        }
        try {
            Method disconnect = proxy.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            if (disconnect != null) {
                disconnect.setAccessible(true);
                return (boolean) disconnect.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean disconnect(BluetoothA2dp proxy, BluetoothDevice device) {
        if (proxy == null) {
            return false;
        }
        try {
            Method disconnect = proxy.getClass().getDeclaredMethod("disconnect", BluetoothDevice.class);
            if (disconnect != null) {
                disconnect.setAccessible(true);
                return (boolean) disconnect.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean connect(BluetoothHeadset proxy, BluetoothDevice device) {
        if (proxy == null) {
            return false;
        }
        try {
            Method connect = proxy.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
            if (connect != null) {
                connect.setAccessible(true);
                return (boolean) connect.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean connect(BluetoothA2dp proxy, BluetoothDevice device) {
        if (proxy == null) {
            return false;
        }
        try {
            Method connect = proxy.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
            if (connect != null) {
                connect.setAccessible(true);
                return (boolean) connect.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getPriority(BluetoothA2dp proxy, BluetoothDevice device) {
        if (proxy == null) {
            return -1;
        }
        try {
            Method getPriority = proxy.getClass().getDeclaredMethod("getPriority", BluetoothDevice.class);
            if (getPriority != null) {
                getPriority.setAccessible(true);
                return (int) getPriority.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getPriority(BluetoothHeadset proxy, BluetoothDevice device) {
        if (proxy == null) {
            return -1;
        }
        try {
            Method getPriority = proxy.getClass().getDeclaredMethod("getPriority", BluetoothDevice.class);
            if (getPriority != null) {
                getPriority.setAccessible(true);
                return (int) getPriority.invoke(proxy, device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean setPriority(BluetoothHeadset proxy, BluetoothDevice device, int priority) {
        if (proxy == null) {
            return false;
        }
        try {
            Method connect = proxy.getClass().getDeclaredMethod("setPriority", BluetoothDevice.class, Integer.class);
            if (connect != null) {
                connect.setAccessible(true);
                return (boolean) connect.invoke(proxy, device, priority);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean setPriority(BluetoothA2dp proxy, BluetoothDevice device, int priority) {
        if (proxy == null) {
            return false;
        }
        try {
            Method connect = proxy.getClass().getDeclaredMethod("setPriority", BluetoothDevice.class, Integer.class);
            if (connect != null) {
                connect.setAccessible(true);
                return (boolean) connect.invoke(proxy, device, priority);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
