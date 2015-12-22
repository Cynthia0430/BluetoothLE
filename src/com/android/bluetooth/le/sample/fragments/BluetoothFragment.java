package com.android.bluetooth.le.sample.fragments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.android.bluetooth.le.sample.BluetoothLEActivity;
import com.android.bluetooth.le.sample.R;
import com.android.bluetooth.le.sample.utils.BleNamesResolver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothFragment extends Fragment {

	private BluetoothGatt mBluetoothGatt = null;
	private ArrayAdapter<String> mBluetoothPairedArrayAdapter;
	private ArrayAdapter<String> mBluetoothAvailableArrayAdapter;
	private BluetoothLEActivity mBLEActivity = new BluetoothLEActivity();
	ListView mPairedDeviceListView;
	ListView mAvailableDeviceListView;
	ArrayList<BluetoothDevice> mBluetoothAvailableArrayList;
	ArrayList<BluetoothDevice> mBluetoothPairedArrayList;
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	private Handler mHandler = new Handler();
	private static final long SCAN_PERIOD = 12000;
	String TAG = "BLE";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.bluetooth_fragment, parentViewGroup, false);
		Button bluetoothOn = (Button) rootView.findViewById(com.android.bluetooth.le.sample.R.id.btn_bluetooth_on);
		Button bluetoothOff = (Button) rootView.findViewById(R.id.btn_bluetooth_off);
		Button bluetoothSearch = (Button) rootView.findViewById(R.id.btn_bluetooth_search);
		mAvailableDeviceListView = (ListView) rootView.findViewById(R.id.available_device_list);
		mPairedDeviceListView = (ListView) rootView.findViewById(R.id.paired_device_list);

		mBluetoothPairedArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
		mBluetoothAvailableArrayList = new ArrayList<BluetoothDevice>();
		mBluetoothPairedArrayList = new ArrayList<BluetoothDevice>();

		if(mBLEActivity.getBluetoothAdapter() == null) {
			Toast.makeText(getActivity(),"Device does not support Bluetooth"
					,Toast.LENGTH_LONG).show();
			return rootView;
		}

		if(mBLEActivity.getBluetoothAdapter().isEnabled()) {
			clearAvailableDevices();
		} else {

		}
		OnClickListener bluetoothOnListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!mBLEActivity.getBluetoothAdapter().isEnabled()) {
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
				mBLEActivity.getBluetoothAdapter().disable();
				Toast.makeText(getActivity(),"Bluetooth turned off" ,
						Toast.LENGTH_LONG).show();
				clearAvailableDevices();
			}
		};
		bluetoothOff.setOnClickListener(bluetoothOffListener);

		OnClickListener bluetoothSearchListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				clearAvailableDevices();
				if(mBLEActivity.getBluetoothAdapter().isEnabled()) {
					startBluetoothLEScan();
				} else {
					Toast.makeText(mBLEActivity, "Turn on Bluetooth", Toast.LENGTH_SHORT).show();
				}
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
				final int devicePosition = position;
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Are you sure");
				builder.setCancelable(true);
				builder.setPositiveButton("Ok",
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						BluetoothDevice bluetoothDevice = mBluetoothAvailableArrayList.get(devicePosition);
						if(bluetoothDevice != null) {
							connect(bluetoothDevice);
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

	public void startBluetoothLEScan() {
		//		int apiVersion = android.os.Build.VERSION.SDK_INT;
		//		if (apiVersion > android.os.Build.VERSION_CODES.KITKAT){
		//			final BluetoothLeScanner scanner = mBLEActivity.getBluetoothAdapter().getBluetoothLeScanner();
		//			mHandler.postDelayed(new Runnable() {
		//				@Override
		//				public void run() {
		//					scanner.stopScan(mLeScannerCallback); 
		//				}
		//			}, SCAN_PERIOD);
		//			scanner.startScan(mLeScannerCallback);
		//		} else {
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mBLEActivity.getBluetoothAdapter().stopLeScan(mLeScanCallback);
			}
		}, SCAN_PERIOD);
		mBLEActivity.getBluetoothAdapter().startLeScan(mLeScanCallback);
	}
	//	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback =
			new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					clearAvailableDevices();
					addDeviceAndDisplayAvailableDevices(device);
				}
			});
		}
	};

	@SuppressLint("NewApi") 
	private ScanCallback mLeScannerCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callback, ScanResult result) {
			final BluetoothDevice device = result.getDevice();
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					clearAvailableDevices();
					addDeviceAndDisplayAvailableDevices(device);
				}
			});
		}
	};

	public boolean connect(final BluetoothDevice device) {
		if (device == null) {
			return false;
		}
		if (mBLEActivity.getBluetoothAdapter() == null || device.getAddress() == null) return false;

		// check if we need to connect from scratch or just reconnect to previous device
		/*if(mBluetoothGatt != null && mBluetoothGatt.getDevice().getAddress().equals(device.getAddress())) {
			// just reconnect
			return mBluetoothGatt.connect();
		}
		else {*/
		// connect from scratch
		// get BluetoothDevice object for specified address

		// connect with remote device
		if(mBluetoothGatt != null) mBluetoothGatt.disconnect();
		mBluetoothGatt = device.connectGatt(getActivity(), false, mBleGattConnectCallback);
		//		}
		return true;
	}

	private final BluetoothGattCallback mBleGattConnectCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), "BLE Connected", Toast.LENGTH_SHORT).show();
					}
				});
				startServicesDiscovery();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), "BLE Disonnected", Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			List<BluetoothGattService> bluetoothGattServices = null;
			List<BluetoothGattCharacteristic> bluetoothGattChars = null;
			if(status == BluetoothGatt.GATT_SUCCESS) {
				if(mBluetoothGatt != null) 
					bluetoothGattServices = mBluetoothGatt.getServices();
				for(BluetoothGattService service : bluetoothGattServices) {
					String serviceName = BleNamesResolver.resolveUuid(service.getUuid().toString());
					bluetoothGattChars = service.getCharacteristics();
					for(BluetoothGattCharacteristic characteristic : bluetoothGattChars) {
						String characteristicName = BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString());
						Log.d(TAG, "BluetoothGatt BluetoothGattService: " + serviceName + "BluetoothGattCharacteristic: " + characteristicName + "UUID: " + characteristic.getUuid().toString());
						//To read requested characteristic. Triggers onCharacteristicRead.
						mBluetoothGatt.readCharacteristic(characteristic);
						//To get notified for change in characteristic. onCharacteristicChanged is triggered.
						boolean success = mBluetoothGatt.setCharacteristicNotification
								(characteristic, true);
						//The below method writes the characteristic to the remote device 
						writeCharacteristic(characteristic);
					}

					Log.d(TAG, "BluetoothGatt BluetoothGattService: " + serviceName + "UUID: " + service.getUuid().toString());
				}
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic,
				int status)
		{

		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic)
		{

		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

		};
	};

	private boolean writeCharacteristic(
			BluetoothGattCharacteristic characteristic) {
		// TODO Auto-generated method stub
		if(mBluetoothGatt == null) {
			Log.e(TAG, "connection lost");
			return false;
		}

		byte[] value = new byte[1];
		byte data = 'c';//The value to be written
		value[0] = (byte) (data);
		characteristic.setValue(value);
		//To write characteristic. Triggers onCharacteristicWrite.
		boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
		return status;
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
		pairedDevices = mBLEActivity.getBluetoothAdapter().getBondedDevices();
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
		Log.d(TAG, "display available devices, device: " + device);
		mBluetoothAvailableArrayAdapter.add(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.add(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}

	public void removeDeviceAndDisplayAvailableDevices(BluetoothDevice device) {
		mBluetoothAvailableArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
		mBluetoothAvailableArrayList.remove(device);
		mBluetoothAvailableArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mBluetoothGatt != null) mBluetoothGatt.disconnect();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(mBluetoothGatt != null) mBluetoothGatt.disconnect();
	}

	public void startServicesDiscovery() {
		if(mBluetoothGatt != null) mBluetoothGatt.discoverServices();
	}

}
