/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lchen.ble;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lchen.ble.core.CachedBluetoothDevice;

import java.util.ArrayList;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScanResultAdapter extends RecyclerView.Adapter<ScanResultAdapter.ViewHolder> {

    private List<CachedBluetoothDevice> cachedBluetoothDevices;

    private OnItemCLick onItemCLick;

    public interface OnItemCLick {
        boolean onRecylerViewItemClick(View view, CachedBluetoothDevice mSelectedAccessPoint);
    }

    public ScanResultAdapter(List<CachedBluetoothDevice> cachedBluetoothDevices) {
        if (cachedBluetoothDevices == null) {
            cachedBluetoothDevices = new ArrayList<>();
        }
        this.cachedBluetoothDevices = cachedBluetoothDevices;
    }

    @Override
    public ScanResultAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_scanresult, parent, false);
        return new ViewHolder(view);
    }

    public void setonItemCLick(OnItemCLick onItemCLick) {
        this.onItemCLick = onItemCLick;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(cachedBluetoothDevices.get(position));
    }

    @Override
    public int getItemCount() {
        return cachedBluetoothDevices.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements CachedBluetoothDevice.Callback{

        private Context context;
        TextView tvdevicename;
        TextView tvdeviceaddress;
        TextView tvdeviconnectstate;

        private CachedBluetoothDevice device;

        public ViewHolder(View itemView) {
            super(itemView);
            context = itemView.getContext();
            tvdevicename = itemView.findViewById(R.id.device_name);
            tvdeviceaddress = itemView.findViewById(R.id.device_address);
            tvdeviconnectstate = itemView.findViewById(R.id.device_connect_state);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemCLick != null) {
                        onItemCLick.onRecylerViewItemClick(v, device);
                    }
                }
            });
        }

        public void bind(CachedBluetoothDevice device) {
            this.device = device;
            this.device.registerCallback(this);
            tvdevicename.setText(device.getName());
            tvdeviceaddress.setText(device.getDevice().getAddress());
            if (device.getConnectionSummary() != 0) {
                tvdeviconnectstate.setText("state : " + context.getString(device.getConnectionSummary()));
            } else {
                tvdeviconnectstate.setText("");
            }

        }

        @Override
        public void onDeviceAttributesChanged() {
            if(device != null) {
                tvdevicename.setText(device.getName());
                tvdeviceaddress.setText(device.getDevice().getAddress());
                if (device.getConnectionSummary() != 0) {
                    tvdeviconnectstate.setText("state : " + context.getString(device.getConnectionSummary()));
                } else {
                    tvdeviconnectstate.setText("");
                }
                Log.e("info", "刷新 item。。。" + device.getConnectionSummary());
            }
        }
    }
}
