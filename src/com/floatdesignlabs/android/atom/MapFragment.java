package com.floatdesignlabs.android.atom;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MapFragment extends Fragment {

	private MapView mMapView;
	private IMapController mMapController;
	private LocationManager mLocationManager;
	private Location mCurrentLocation;
	private LocationListener mLocationListener;

	String DEBUG_TAG = "ATOM_DEBUG";

	private int INITIAL_ZOOM_LEVEL = 15;
	private int LOCATION_MIN_TIME = 1000;//milliseconds
	private int LOCATION_MIN_DIST = 10;//meters

	private ItemizedOverlay<OverlayItem> mMyLocationOverlay; 
	private ResourceProxy mResourceProxy;
//	Drawable mCurrentLocationDrawable;
	ArrayList<OverlayItem> items;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.map_fragment, parentViewGroup, false);
		mMapView = (MapView) rootView.findViewById(R.id.mapview);
		Button down_button = (Button) rootView.findViewById(R.id.btn_download_map);
		mMapController = (MapController) mMapView.getController();
		mLocationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
		Location lastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		mCurrentLocationDrawable = this.getResources().getDrawable(R.drawable.blue_dot);//TODO Put correct drawable
		setCurrentUserLocation(lastLocation);
		GeoPoint currentLocGeoPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
		mMapController.setCenter(currentLocGeoPoint);
		displayCurrentLocationDrawable(currentLocGeoPoint);//TODO somehow this does not look perfect. Please check this
		mLocationListener = new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				mCurrentLocation = new Location(location);
				setCurrentUserLocation(mCurrentLocation);
			}
		};

		mMapView.setTileSource(TileSourceFactory.MAPNIK);
		mMapView.setBuiltInZoomControls(true);
		mMapController.setZoom(INITIAL_ZOOM_LEVEL);

		
		mMapView.setMultiTouchControls(true);
		
		PackageManager m = getActivity().getPackageManager();
		String s = getActivity().getPackageName();
		try {
		    PackageInfo p = m.getPackageInfo(s, 0);
		    s = p.applicationInfo.dataDir;
		} catch (PackageManager.NameNotFoundException e) {
		    Log.w("FLOAT", "Error Package name not found ", e);
		}
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		
		OnClickListener downloadMapClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CacheManager cacheManager = new CacheManager(mMapView);
				int zoomMin = mMapView.getZoomLevel();
				int zoomMax = mMapView.getZoomLevel() + 4;
				cacheManager.downloadAreaAsync(getActivity(), mMapView.getBoundingBox(), zoomMin, zoomMax);
			}
		};
		down_button.setOnClickListener(downloadMapClickListener);
		
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private void setCurrentUserLocation(Location lastLocation) {
		// TODO Auto-generated method stub
		if(lastLocation == null) {
			Toast.makeText(getActivity().getApplicationContext(), "Unable to reach location services", Toast.LENGTH_SHORT).show();
		} else {
			GeoPoint locGeoPoint = new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude());
			//mMapController.setCenter(locGeoPoint); 
			mMapView.setUseDataConnection(true);
			displayCurrentLocationDrawable(locGeoPoint);
		}
	}

	public void displayCurrentLocationDrawable(GeoPoint loc)
	{
		mResourceProxy = new DefaultResourceProxyImpl(getActivity());
		items = new ArrayList<OverlayItem>(); 
		// Put overlay icon a little way from map center
		OverlayItem overlayForCurrentLocationItem = new OverlayItem("Here you are", "we will keep track of you", loc);
//		overlayForCurrentLocationItem.setMarker(mCurrentLocationDrawable);
		items.add(overlayForCurrentLocationItem);
		/* OnTapListener for the Markers, shows a simple Toast. */ 
		this.mMyLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items, 
				new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() { 
			@Override 
			public boolean onItemSingleTapUp(final int index, 
					final OverlayItem item) { 
				Toast.makeText( 
						getActivity(), 
						"single tap" + item.getTitle(), Toast.LENGTH_LONG).show(); 
				return true; // We 'handled' this event. 
			} 
			@Override 
			public boolean onItemLongPress(final int index, 
					final OverlayItem item) { 
				Toast.makeText( 
						getActivity(),  
						"longPress '" + item.getTitle() ,Toast.LENGTH_LONG).show(); 
				return false; 
			} 
		}, mResourceProxy); 
		mMapView.getOverlays().clear();
		this.mMapView.getOverlays().add(this.mMyLocationOverlay); 
		mMapView.invalidate();
	}

	@Override
	public void onResume() {
		super.onResume();
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocationManager.removeUpdates(mLocationListener);
	}
}
