package com.lchen.ble.core;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class LocalBluetoothManager { private static final String TAG = "LocalBluetoothManager";

    /** Singleton instance. */
    private static LocalBluetoothManager sInstance;

    private final Context mContext;

    /** If a BT-related activity is in the foreground, this will be it. */
    private Context mForegroundActivity;

    private final LocalBluetoothAdapter mLocalAdapter;

    private final CachedBluetoothDeviceManager mCachedDeviceManager;

    /** The Bluetooth profile manager. */
    private final LocalBluetoothProfileManager mProfileManager;

    /** The broadcast receiver event manager. */
    private final BluetoothEventManager mEventManager;

    public static synchronized LocalBluetoothManager getInstance(Context context,
                                                                 BluetoothManagerCallback onInitCallback) {
        if (sInstance == null) {
            LocalBluetoothAdapter adapter = LocalBluetoothAdapter.getInstance();
            if (adapter == null) {
                return null;
            }
            // This will be around as long as this process is
            Context appContext = context.getApplicationContext();
            sInstance = new LocalBluetoothManager(adapter, appContext);
            if (onInitCallback != null) {
                onInitCallback.onBluetoothManagerInitialized(appContext, sInstance);
            }
        }

        return sInstance;
    }

    private LocalBluetoothManager(LocalBluetoothAdapter adapter, Context context) {
        mContext = context;
        mLocalAdapter = adapter;

        mCachedDeviceManager = new CachedBluetoothDeviceManager(context, this);
        mEventManager = new BluetoothEventManager(mLocalAdapter,
                mCachedDeviceManager, context);
        mProfileManager = new LocalBluetoothProfileManager(context,
                mLocalAdapter, mCachedDeviceManager, mEventManager);
    }

    public LocalBluetoothAdapter getBluetoothAdapter() {
        return mLocalAdapter;
    }

    public Context getContext() {
        return mContext;
    }

    public CachedBluetoothDeviceManager getCachedDeviceManager() {
        return mCachedDeviceManager;
    }

    public BluetoothEventManager getEventManager() {
        return mEventManager;
    }

    public LocalBluetoothProfileManager getProfileManager() {
        return mProfileManager;
    }

    public interface BluetoothManagerCallback {
        void onBluetoothManagerInitialized(Context appContext,
                                           LocalBluetoothManager bluetoothManager);
    }
}
