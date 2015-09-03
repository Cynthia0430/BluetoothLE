package com.floatdesignlabs.android.atom;

import java.util.UUID;

import com.floatdesignlabs.android.atom.bluetooth.BluetoothFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;


public class AtomActivity extends Activity {

	private static Context mContext;
	private static BluetoothAdapter mBluetoothAdapter;
	private static String BLUETOOTH_TAG = "BLUETOOTH";
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.atom);
		mContext = getApplicationContext();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		Button btnLoadMapCurrentLoc = (Button) findViewById(R.id.btn_load_map_current_loc);
		Button btnMapRouting = (Button) findViewById(R.id.btn_load_map_routing);
		Button btnBluetoothStart = (Button) findViewById(R.id.btn_load_bluetooth);

		OnClickListener currentLocListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				MapFragment mapFragment = new MapFragment();
				fragmentTransaction.add(R.id.map_fragment_container, mapFragment, "MAPS");
				fragmentTransaction.commit();
			}
		};
		btnLoadMapCurrentLoc.setOnClickListener(currentLocListener);

		OnClickListener routingListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				MapRouting mapRouting = new MapRouting();
				fragmentTransaction.add(R.id.map_fragment_container, mapRouting, "MAPSROUTING");
				fragmentTransaction.commit();
			}

		};
		btnMapRouting.setOnClickListener(routingListener);

		OnClickListener bluetoothListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
				mainLinearLayout.setVisibility(View.INVISIBLE);
				FragmentManager fragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				BluetoothFragment bluetoothFragment = new BluetoothFragment();
				fragmentTransaction.add(R.id.bluetooth_fragment_container, bluetoothFragment, BLUETOOTH_TAG);
				fragmentTransaction.addToBackStack(BLUETOOTH_TAG);
				fragmentTransaction.commit();
			}
		};
		btnBluetoothStart.setOnClickListener(bluetoothListener);
	}

	private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateBluetoothFragment(intent);
		}
	};

	private void updateBluetoothFragment(Intent intent) {
		// TODO Auto-generated method stub
		FragmentManager fragmentManager = getFragmentManager();
		BluetoothFragment bluetoothFragment = (BluetoothFragment) fragmentManager.findFragmentByTag(BLUETOOTH_TAG);
		final String action = intent.getAction();
		BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		switch(action) {
		case BluetoothAdapter.ACTION_STATE_CHANGED : 
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
					BluetoothAdapter.ERROR);
			if(state == BluetoothAdapter.STATE_ON) {
				bluetoothFragment.displayPairedDevices();
			} else if (state == BluetoothAdapter.STATE_OFF) {
				bluetoothFragment.clearPairedDevices();
				bluetoothFragment.clearAvailableDevices();
			}
			break;
		case BluetoothDevice.ACTION_FOUND : 
			if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
				bluetoothFragment.addDeviceAndDisplayAvailableDevices(device);
			}
			break;
		case BluetoothAdapter.ACTION_DISCOVERY_STARTED : 
			// Discovery has started, display progress spinner
			setProgressBarIndeterminateVisibility(true);
			break;
		case BluetoothAdapter.ACTION_DISCOVERY_FINISHED : 
			// Discovery has ended, hide progress spinner
			setProgressBarIndeterminateVisibility(false);
			break;
		case BluetoothDevice.ACTION_BOND_STATE_CHANGED :
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);
			switch(bondState) {

			case BluetoothDevice.BOND_BONDED :
				bluetoothFragment.displayPairedDevices();
				bluetoothFragment.removeDeviceAndDisplayAvailableDevices(device);
				Toast.makeText(this,"Paired" ,
						Toast.LENGTH_LONG).show();
			case BluetoothDevice.BOND_NONE : 
				bluetoothFragment.clearPairedDevices();
				bluetoothFragment.displayPairedDevices();
				Toast.makeText(this,"Unpaired" ,
						Toast.LENGTH_LONG).show();
				if(mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}
				mBluetoothAdapter.startDiscovery();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.atom, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//		if(mReceiver != null) {
		mBluetoothAdapter.cancelDiscovery();
		mContext.unregisterReceiver(mBluetoothReceiver);
		//			mReceiver = null;
		//		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(mBluetoothAdapter.isEnabled()) {
			FragmentManager fragmentManager = getFragmentManager();
			BluetoothFragment bluetoothFragment = (BluetoothFragment) fragmentManager.findFragmentByTag(BLUETOOTH_TAG);
			if(bluetoothFragment != null && bluetoothFragment.isVisible()) {
				bluetoothFragment.displayPairedDevices();
			}
		}
		IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
		bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		bluetoothFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		mContext.registerReceiver(mBluetoothReceiver, bluetoothFilter);
	}

	@Override
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() > 0 ){
			getFragmentManager().popBackStack();
			LinearLayout mainLinearLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
			mainLinearLayout.setVisibility(View.VISIBLE);
		} else {
			super.onBackPressed();
		}
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}
	
	public UUID getMyUUID() {
		return MY_UUID;
	}
}
