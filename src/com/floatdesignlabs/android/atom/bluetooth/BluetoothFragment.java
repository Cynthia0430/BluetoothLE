package com.floatdesignlabs.android.atom.bluetooth;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import com.floatdesignlabs.android.atom.AtomActivity;
import com.floatdesignlabs.android.atom.MapRouting;
import com.floatdesignlabs.android.atom.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothFragment extends Fragment {

	private ArrayAdapter<String> mBluetoothPairedArrayAdapter;
	private ArrayAdapter<String> mBluetoothAvailableArrayAdapter;
	private AtomActivity mAtomActivity = new AtomActivity();
	ListView mPairedDeviceListView;
	ListView mAvailableDeviceListView;
	ArrayList<BluetoothDevice> mBluetoothAvailableArrayList;
	ArrayList<BluetoothDevice> mBluetoothPairedArrayList;
	String TAG = "FLOAT";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bluetooth_fragment, parentViewGroup, false);
		Button bluetoothOn = (Button) rootView.findViewById(R.id.btn_bluetooth_on);
		Button bluetoothOff = (Button) rootView.findViewById(R.id.btn_bluetooth_off);
		Button bluetoothSearch = (Button) rootView.findViewById(R.id.btn_bluetooth_search);
		mAvailableDeviceListView = (ListView) rootView.findViewById(R.id.available_device_list);
		mPairedDeviceListView = (ListView) rootView.findViewById(R.id.paired_device_list);

		mBluetoothPairedArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayList = new ArrayList<BluetoothDevice>();
		mBluetoothPairedArrayList = new ArrayList<BluetoothDevice>();

		if(mAtomActivity.getBluetoothAdapter() == null) {
			Toast.makeText(getActivity(),"Device does not support Bluetooth"
					,Toast.LENGTH_LONG).show();
			return rootView;
		}

		if(mAtomActivity.getBluetoothAdapter().isEnabled()) {
			displayPairedDevices();
		} else {
			clearPairedDevices();
		}
		OnClickListener bluetoothOnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mAtomActivity.getBluetoothAdapter().isEnabled()) {
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
				mAtomActivity.getBluetoothAdapter().disable();
				Toast.makeText(getActivity(),"Bluetooth turned off" ,
						Toast.LENGTH_LONG).show();
			}
		};
		bluetoothOff.setOnClickListener(bluetoothOffListener);

		OnClickListener bluetoothSearchListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (mAtomActivity.getBluetoothAdapter().isDiscovering()) {
					mAtomActivity.getBluetoothAdapter().cancelDiscovery();
				}
				clearAvailableDevices();
				mAtomActivity.getBluetoothAdapter().startDiscovery();
			}
		};
		bluetoothSearch.setOnClickListener(bluetoothSearchListener);
		mPairedDeviceListView.setAdapter(mBluetoothPairedArrayAdapter);
		mAvailableDeviceListView.setAdapter(mBluetoothAvailableArrayAdapter);

		mAvailableDeviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(),"Click" ,
						Toast.LENGTH_LONG).show();
				final int devicePosition = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						if (mAtomActivity.getBluetoothAdapter().isDiscovering()) {
							mAtomActivity.getBluetoothAdapter().cancelDiscovery();
						}
						BluetoothDevice bluetoothDevice = mBluetoothAvailableArrayList.get(devicePosition);
						Boolean isPaired = doPairing(bluetoothDevice);
						if(isPaired) {
//							mBluetoothPairedArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
							Log.d(TAG, "createBond called successfully");
						}
					}
				});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alert11 = builder.create();
				alert11.show();
			}
		});
		
		mPairedDeviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				// TODO Auto-generated method stub
				final int devicePosition = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						BluetoothDevice bluetoothDevice = mBluetoothPairedArrayList.get(devicePosition);
						Boolean isRemoved = doRemove(bluetoothDevice);
						if(isRemoved) {
							Log.d(TAG, "removeBond called successfully");
						}
					}
				});
				builder.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});

				AlertDialog alert11 = builder.create();
				alert11.show();
			}
			
		});
		
		return rootView;
	}

	private Boolean doPairing(BluetoothDevice bluetoothDevice) {
		// TODO Auto-generated method stub
		try {
			Class<?> class1 = Class.forName("android.bluetooth.BluetoothDevice");
			Method createBondMethod = class1.getMethod("createBond");  
			Boolean returnValue = (Boolean) createBondMethod.invoke(bluetoothDevice);  
			return returnValue.booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private Boolean doRemove(BluetoothDevice bluetoothDevice) {
		try {
			Class<?> class1 = Class.forName("android.bluetooth.BluetoothDevice");
			Method removeBondMethod = class1.getMethod("removeBond");  
			Boolean returnValue = (Boolean) removeBondMethod.invoke(bluetoothDevice);  
			return returnValue.booleanValue();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public void clearPairedDevices() {
		// TODO Auto-generated method stub
		mBluetoothPairedArrayAdapter.clear();
		mBluetoothPairedArrayList.clear();
		mBluetoothPairedArrayAdapter.notifyDataSetChanged();
	}

	public void displayPairedDevices() {
		// TODO Auto-generated method stub
		Set<BluetoothDevice> pairedDevices;
		pairedDevices = mAtomActivity.getBluetoothAdapter().getBondedDevices();
		mBluetoothPairedArrayAdapter.clear();
		mBluetoothPairedArrayList.clear();
		for(BluetoothDevice device : pairedDevices) {
			mBluetoothPairedArrayAdapter.add(device.getName()+ "\n" + device.getAddress());
			mBluetoothPairedArrayList.add(device);
		}
		mBluetoothPairedArrayAdapter.notifyDataSetChanged();
	}

	public void clearAvailableDevices() {
		mBluetoothAvailableArrayAdapter.clear();
		if(mBluetoothAvailableArrayList != null)
			mBluetoothAvailableArrayList.clear();
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}
	
	public void addDeviceAndDisplayAvailableDevices(BluetoothDevice device) {
		mBluetoothAvailableArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.add(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}
	
	public void removeDeviceAndDisplayAvailableDevices(BluetoothDevice device) {
		mBluetoothAvailableArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.remove(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}
}
