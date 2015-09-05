package com.floatdesignlabs.android.atom.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import com.floatdesignlabs.android.atom.AtomActivity;
import com.floatdesignlabs.android.atom.R;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

	private ArrayAdapter<String> mBluetoothPairedArrayAdapter;
	private ArrayAdapter<String> mBluetoothAvailableArrayAdapter;
	private AtomActivity mAtomActivity = new AtomActivity();
	ListView mPairedDeviceListView;
	ListView mAvailableDeviceListView;
	ArrayList<BluetoothDevice> mBluetoothAvailableArrayList;
	ArrayList<BluetoothDevice> mBluetoothPairedArrayList;
	protected static final int SUCCESS_CONNECT = 0;
	protected static final int MESSAGE_READ = 1;
	ConnectThread mConnectThread;
	ConnectedThread mConnectedThread;
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
							//mBluetoothPairedArrayAdapter.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());
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
						/*Boolean isRemoved = doRemove(bluetoothDevice);
						if(isRemoved) {
							Log.d(TAG, "removeBond called successfully");
						}*/
						doConnectDevice(bluetoothDevice);
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
	
	private void doConnectDevice(BluetoothDevice bluetoothDevice) {
		if(mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}
		if(mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
		mConnectThread = new ConnectThread(bluetoothDevice, mHandler);
		mConnectThread.start();
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

	/*private class ConnectThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server code
				tmp = device.createRfcommSocketToServiceRecord(mAtomActivity.getMyUUID());
			} catch (IOException e) { 

			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mAtomActivity.getBluetoothAdapter().cancelDiscovery();
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {	
				// Unable to connect; close the socket and get out
				try {
					mmSocket.close();
				} catch (IOException closeException) { }
				return;
			}

			// Do work to manage the connection (in a separate thread)

			mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
		}
		*//** Will cancel an in-progress connection, and close the socket *//*
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}*/

	/*private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) { }

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			byte[] buffer;  // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					buffer = new byte[1024];
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
					.sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		 Call this from the main activity to send data to the remote device 
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) { }
		}

		 Call this from the main activity to shutdown the connection 
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) { }
		}
	}*/

	private class AcceptThread extends Thread {
		private AtomActivity mAtomActivity = new AtomActivity();
		private final BluetoothServerSocket mmServerSocket;

		public AcceptThread() {
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client code
				tmp = mAtomActivity.getBluetoothAdapter().listenUsingRfcommWithServiceRecord("Atom", mAtomActivity.getMyUUID());
			} catch (IOException e) { }
			mmServerSocket = tmp;
		}

		public void run() {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					//	                manageConnectedSocket(socket);
					try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) { }
		}
	}

	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch(msg.what){
			case SUCCESS_CONNECT:
				if(mConnectedThread != null) {
					mConnectedThread.cancel();
					mConnectedThread = null;
				}
				mConnectedThread = new ConnectedThread((BluetoothSocket)msg.obj, mHandler);
				Toast.makeText(getActivity(), "CONNECT", Toast.LENGTH_SHORT).show();
				String s = "successfully connected ";
				mConnectedThread.start();
				for(int i = 0; i < 10; i++) {
					mConnectedThread.write(s.getBytes());
				}
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[])msg.obj;
				String string = new String(readBuf);
				if(!mAtomActivity.getBluetoothAdapter().isEnabled()) {
					if(mConnectedThread != null) {
						mConnectedThread.cancel();
						mConnectedThread = null;
					}
					Log.d(TAG, "00-> Bluetooth returned");
					return;
				}
				Toast.makeText(getActivity(), string, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Bluetooth Received: " + string);
				break;
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		mHandler.removeCallbacksAndMessages(null);
	}
}
