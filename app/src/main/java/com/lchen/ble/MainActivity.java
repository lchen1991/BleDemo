package com.lchen.ble;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lchen.ble.core.BluetoothCallback;
import com.lchen.ble.core.BluetoothEnabler;
import com.lchen.ble.core.BluetoothEventManager;
import com.lchen.ble.core.CachedBluetoothDevice;
import com.lchen.ble.core.LocalBluetoothAdapter;
import com.lchen.ble.core.LocalBluetoothManager;
import com.lchen.ble.core.Utils;
import com.lchen.ble.widget.SwitchBar;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity implements BluetoothCallback, ScanResultAdapter.OnItemCLick {

    private boolean mHasPermission;
    //权限请求码
    private static final int PERMISSION_REQUEST_CODE = 0;
    //两个危险权限需要动态申请
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean mInitialScanStarted;

    private boolean mInitiateDiscoverable;

    private BluetoothEnabler mBluetoothEnabler;

    private ProgressBar mProgressHeader;

    private RecyclerView mBleList;

    BluetoothDevice mSelectedDevice;

    LocalBluetoothAdapter mLocalAdapter;
    LocalBluetoothManager mLocalManager;

    private Button button;

    private ScanResultAdapter scanResultAdapter;

    private AlertDialog mDisconnectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.btn_scan);
        mProgressHeader = findViewById(R.id.ble_progress_header);

        mBleList = findViewById(R.id.rv_ble_list);
        mBleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        setProgressBarVisible(false);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });

        mLocalManager = Utils.getLocalBtManager(this);

        scanResultAdapter = new ScanResultAdapter(mLocalManager.getCachedDeviceManager().getCachedDevices());
        scanResultAdapter.setonItemCLick(this);
        mBleList.setAdapter(scanResultAdapter);

        if (mLocalManager == null) {
            Log.e("info", "Bluetooth is not supported on this device");
            return;
        }
        mLocalAdapter = mLocalManager.getBluetoothAdapter();

        SwitchBar switchBar = findViewById(R.id.switch_bar);
        mBluetoothEnabler = new BluetoothEnabler(this, switchBar);
        mBluetoothEnabler.setupSwitchBar();

        mHasPermission = checkPermission();
        if (!mHasPermission) {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        // resume BluetoothEnabler before calling super.onResume() so we don't get
        // any onDeviceAdded() callbacks before setting up view in updateContent()
        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.resume(this);
        }
        super.onResume();

        if (mLocalManager == null) return;

        mLocalManager.getEventManager().registerCallback(this);

        mInitiateDiscoverable = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        removeAllDevices();
        mLocalManager.getEventManager().unregisterCallback(this);

        if (mBluetoothEnabler != null) {
            mBluetoothEnabler.pause();
        }
    }

    private void startScanning() {
        mLocalManager.getCachedDeviceManager().clearNonBondedDevices();
        mInitialScanStarted = true;
        mLocalAdapter.startScanning(true);
    }

    void removeAllDevices() {
        mLocalAdapter.stopScanning();
//        mDevicePreferenceMap.clear();
//        mDeviceListGroup.removeAll();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothEnabler.teardownSwitchBar();
    }

    protected void setProgressBarVisible(boolean visible) {
        if (mProgressHeader != null) {
            mProgressHeader.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onBluetoothStateChanged(int bluetoothState) {
        if (bluetoothState == BluetoothAdapter.STATE_OFF) {
            Log.e("info", "onBluetoothStateChanged:STATE_OFF");
        } else {
            Log.e("info", "onBluetoothStateChanged:STATE_ON");
        }
    }

    @Override
    public void onScanningStateChanged(boolean started) {
        setProgressBarVisible(started);
        Log.e("info", "onScanningStateChanged:" + started);
    }

    @Override
    public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
        if ((cachedDevice.getBtClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                || cachedDevice.getBtClass().getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)) {
            Log.e("info", "----蓝牙耳机----");
        } else {
            Log.e("info", "--other----");
        }
        if (scanResultAdapter != null) {
            scanResultAdapter.notifyDataSetChanged();
        }
        Log.e("info", "onDeviceAdded:" + cachedDevice.getName() + ";DeviceClass:" + cachedDevice.getBtClass().getDeviceClass());
    }

    @Override
    public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
        Log.e("info", "onDeviceDeleted:" + cachedDevice.getName() + ";" + cachedDevice.getConnectionSummary());
    }

    @Override
    public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
        Log.e("info", "onDeviceBondStateChanged:" + cachedDevice.getName() + ";bondState:" + bondState);
    }

    @Override
    public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
        Log.e("info", "onConnectionStateChanged:" + cachedDevice.getName() + ";state:" + state);
        if (scanResultAdapter != null) {
            scanResultAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 检查是否已经授予权限
     *
     * @return
     */
    private boolean checkPermission() {
        for (String permission : NEEDED_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasAllPermission = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    hasAllPermission = false;   //判断用户是否同意获取权限
                    break;
                }
            }
            //如果同意权限
            if (hasAllPermission) {
                mHasPermission = true;
                if (mHasPermission) {  // 已经获取权限
                    Toast.makeText(MainActivity.this, "Permission Ok!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "权限获取失败", Toast.LENGTH_SHORT).show();
                }

            } else {  //用户不同意权限
                mHasPermission = false;
                Toast.makeText(MainActivity.this, "获取权限失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onRecylerViewItemClick(View view, CachedBluetoothDevice mCachedDevice) {
        int bondState = mCachedDevice.getBondState();

        if (mCachedDevice.isConnected()) {
            askDisconnect(mCachedDevice);
        } else if (bondState == BluetoothDevice.BOND_BONDED) {
            mCachedDevice.connect(true);
        } else if (bondState == BluetoothDevice.BOND_NONE) {
            pair(mCachedDevice);
        }
        return true;
    }

    private void askDisconnect(final CachedBluetoothDevice mCachedDevice) {
        String name = mCachedDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = "bluetooth_device";
        }
        String message = getString(R.string.bluetooth_disconnect_all_profiles, name);
        String title = getString(R.string.bluetooth_disconnect_title);

        DialogInterface.OnClickListener disconnectListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCachedDevice.disconnect();
            }
        };

        mDisconnectDialog = Utils.showDisconnectDialog(this
                , mDisconnectDialog, disconnectListener, title, Html.fromHtml(message));
    }

    private void pair(final CachedBluetoothDevice mCachedDevice) {
        if (!mCachedDevice.startPairing()) {
            Utils.showError(this, mCachedDevice.getName(), R.string.bluetooth_pairing_error_message);
        } else {
            Toast.makeText(this, " startPairing...", Toast.LENGTH_SHORT).show();
        }
    }
}
