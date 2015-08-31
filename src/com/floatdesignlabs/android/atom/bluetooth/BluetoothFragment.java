package com.floatdesignlabs.android.atom.bluetooth;

import java.util.Set;

import com.floatdesignlabs.android.atom.MapRouting;
import com.floatdesignlabs.android.atom.R;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothFragment extends Fragment {
	
	private BluetoothAdapter mBluetoothAdapter;
	private ArrayAdapter<String> mBluetoothPairedArrayAdapter;
	private ArrayAdapter<String> mBluetoothAvailableArrayAdapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bluetooth_fragment, parentViewGroup, false);
		Button bluetoothOn = (Button) rootView.findViewById(R.id.btn_bluetooth_on);
		Button bluetoothOff = (Button) rootView.findViewById(R.id.btn_bluetooth_off);
		Button bluetoothSearch = (Button) rootView.findViewById(R.id.btn_bluetooth_search);
		ListView pairedDeviceListView = (ListView) rootView.findViewById(R.id.paired_device_list);
		ListView availableDeviceListView = (ListView) rootView.findViewById(R.id.available_device_list);
		
		mBluetoothPairedArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter == null) {
			Toast.makeText(getActivity(),"Device does not support Bluetooth"
                    ,Toast.LENGTH_LONG).show();
			return rootView;
		}
		
		//TODO: this doesn't work
		displayPaireddevices(pairedDeviceListView);
		
		OnClickListener bluetoothOnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mBluetoothAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);
                    Toast.makeText(getActivity(),"Bluetooth turned on"
                            ,Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getActivity(),"Bluetooth already on",
                            Toast.LENGTH_LONG).show();
                }
			}
		};
		bluetoothOn.setOnClickListener(bluetoothOnListener);
		
		OnClickListener bluetoothOffListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluetoothAdapter.disable();
				 Toast.makeText(getActivity(),"Bluetooth turned off" ,
	                        Toast.LENGTH_LONG).show();
			}
		};
		bluetoothOff.setOnClickListener(bluetoothOffListener);
		
		OnClickListener bluetoothSearchListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		};
		bluetoothSearch.setOnClickListener(bluetoothSearchListener);
		
		return rootView;
		
	}
	
	private void displayPaireddevices(ListView pairedDeviceListView) {
		// TODO Auto-generated method stub
		Set<BluetoothDevice> pairedDevices;
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		 for(BluetoothDevice device : pairedDevices)
			           mBluetoothPairedArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
		 pairedDeviceListView.setAdapter(mBluetoothPairedArrayAdapter);
	}
}
