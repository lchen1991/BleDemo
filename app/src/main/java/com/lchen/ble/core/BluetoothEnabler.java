package com.lchen.ble.core;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.widget.Switch;

import com.lchen.ble.widget.SwitchBar;
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public final class BluetoothEnabler implements SwitchBar.OnSwitchChangeListener {

    private Context mContext;
    private Switch mSwitch;
    private SwitchBar mSwitchBar;
    private boolean mValidListener;
    private final LocalBluetoothAdapter mLocalAdapter;
    private final IntentFilter mIntentFilter;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            handleStateChanged(state);
        }
    };

    public BluetoothEnabler(Context context, SwitchBar switchBar) {
        mContext = context;
        mSwitchBar = switchBar;
        mSwitch = switchBar.getSwitch();
        mValidListener = false;

        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            // Bluetooth is not supported
            mLocalAdapter = null;
            mSwitch.setEnabled(false);
        } else {
            mLocalAdapter = manager.getBluetoothAdapter();
        }
        mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public void setupSwitchBar() {
        mSwitchBar.show();
    }

    public void teardownSwitchBar() {
        mSwitchBar.hide();
    }

    public void resume(Context context) {
        if (mLocalAdapter == null) {
            mSwitch.setEnabled(false);
            return;
        }

        if (mContext != context) {
            mContext = context;
        }

        // Bluetooth state is not sticky, so set it manually
        handleStateChanged(mLocalAdapter.getBluetoothState());

        mSwitchBar.addOnSwitchChangeListener(this);
        mContext.registerReceiver(mReceiver, mIntentFilter);
        mValidListener = true;
    }

    public void pause() {
        if (mLocalAdapter == null) {
            return;
        }

        mSwitchBar.removeOnSwitchChangeListener(this);
        mContext.unregisterReceiver(mReceiver);
        mValidListener = false;
    }

    void handleStateChanged(int state) {
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_ON:
                setChecked(true);
                mSwitch.setEnabled(true);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                mSwitch.setEnabled(false);
                break;
            case BluetoothAdapter.STATE_OFF:
                setChecked(false);
                mSwitch.setEnabled(true);
                break;
            default:
                setChecked(false);
                mSwitch.setEnabled(true);
        }
    }

    private void setChecked(boolean isChecked) {
        if (isChecked != mSwitch.isChecked()) {
            // set listener to null, so onCheckedChanged won't be called
            // if the checked status on Switch isn't changed by user click
            if (mValidListener) {
                mSwitchBar.removeOnSwitchChangeListener(this);
            }
            mSwitch.setChecked(isChecked);
            if (mValidListener) {
                mSwitchBar.addOnSwitchChangeListener(this);
            }
        }
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        // Show toast message if Bluetooth is not allowed in airplane mode

        if (mLocalAdapter != null) {
            boolean status = mLocalAdapter.setBluetoothEnabled(isChecked);
            // If we cannot toggle it ON then reset the UI assets:
            // a) The switch should be OFF but it should still be togglable (enabled = True)
            // b) The switch bar should have OFF text.
            if (isChecked && !status) {
                switchView.setChecked(false);
                mSwitch.setEnabled(true);
                mSwitchBar.setTextViewLabel(false);
                return;
            }
        }
        mSwitch.setEnabled(false);
    }
}
