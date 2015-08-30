package com.floatdesignlabs.android.atom;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class AtomActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.atom);
		Button btnLoadMapCurrentLoc = (Button) findViewById(R.id.btn_load_map_current_loc);
		Button btnMapRouting = (Button) findViewById(R.id.btn_load_map_routing);

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
				fragmentTransaction.add(R.id.map_fragment_container, mapRouting, "MAPSROUTIG");
				fragmentTransaction.commit();
			}
			
		};
		btnMapRouting.setOnClickListener(routingListener);
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
}
