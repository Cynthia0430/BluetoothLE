package com.android.bluetooth.le.sample;

import java.util.UUID;

import com.android.bluetooth.le.sample.fragments.BluetoothFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class BluetoothLEActivity extends Activity {

	private static Context mContext;
	private static BluetoothAdapter mBluetoothAdapter;
	private BluetoothGatt mBluetoothGatt = null;
	private static String BLUETOOTH_TAG = "BLUETOOTH";
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static boolean isBluetoothBLE = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mContext = getApplicationContext();
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			isBluetoothBLE = true;
		} else {
			return;
		}
		initBluetooth();

		// TODO Auto-generated method stub
		addBLEFragment();

	}

	private void addBLEFragment() {
		// TODO Auto-generated method stub
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		BluetoothFragment bluetoothFragment = new BluetoothFragment();
		fragmentTransaction.add(R.id.bluetooth_fragment_container, bluetoothFragment, BLUETOOTH_TAG);
		fragmentTransaction.commit();
	}

	private void initBluetooth() {
		// TODO Auto-generated method stub
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(isBluetoothBLE) {
			final BluetoothManager bluetoothManager =
					(BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			mBluetoothAdapter = bluetoothManager.getAdapter();
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		initBluetooth();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	public UUID getMyUUID() {
		return MY_UUID;
	}

	public boolean isBluetoothBLE() {
		return isBluetoothBLE;
	}
}
