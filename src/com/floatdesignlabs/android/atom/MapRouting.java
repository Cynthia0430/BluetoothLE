package com.floatdesignlabs.android.atom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.cachemanager.CacheManager;
import org.osmdroid.bonuspack.location.GeocoderNominatim;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MapRouting extends Fragment {

	GeocoderNominatim mGeoCoder;
	String TAG = "FLOAT";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parentViewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.map_fragment, parentViewGroup, false);

		final MapView map = (MapView) rootView.findViewById(R.id.mapview);
		Button down_button = (Button) rootView.findViewById(R.id.btn_download_map);
		map.setTileSource(TileSourceFactory.MAPNIK);
		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		final GeoPoint startPoint = new GeoPoint(15.37, 75.123);
		IMapController mapController = map.getController();
		mapController.setZoom(15);
		mapController.setCenter(startPoint);

		final String searchString = new String("Keshwapur");

		mGeoCoder = new GeocoderNominatim(getActivity());
		if (!GeocoderNominatim.isPresent()){
			Toast.makeText(getActivity(),
					"Sorry! Geocoder service not Present.",
					Toast.LENGTH_LONG).show();
		}

		new Thread(new Runnable()
		{
			public void run() 
			{

				Marker startMarker = new Marker(map);
				startMarker.setPosition(startPoint);
				startMarker.setTitle("Start point");
				startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
				map.getOverlays().add(startMarker);

				//reverse geocoding
				getAddressFromLocationPoint(startPoint);

				//geocoding
				Address endPointAddress = getPointFromPlaceName(searchString);
				if(endPointAddress == null) {
					return;
				}

				RoadManager roadManager = new OSRMRoadManager();
				ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
				waypoints.add(startPoint);
				GeoPoint endPoint = new GeoPoint(endPointAddress.getLatitude(), endPointAddress.getLongitude());
				//				GeoPoint endPoint = new GeoPoint(15.356, 75.146);
				Marker endMarker = new Marker(map);
				endMarker.setIcon(getResources().getDrawable(R.drawable.marker_destination));
				endMarker.setTitle("End point");
				endMarker.setPosition(endPoint);
				map.getOverlays().add(endMarker);

				waypoints.add(endPoint);
				Road road = null;
				try 
				{
					road = roadManager.getRoad(waypoints);
				} 
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if(road != null) {
					Log.d(TAG, "00-->Length: " + road.mLength);
					Log.d("FOAT", "00-->Time: " + road.mDuration);
					if (road.mStatus != Road.STATUS_OK)
					{

						//handle error... warn the user, etc. 
					}
					showRoutes(road);

					Polyline roadOverlay = RoadManager.buildRoadOverlay(road, getActivity());
					map.getOverlays().add(roadOverlay);
				}
			}

			private void showRoutes(Road road) {
				// TODO Auto-generated method stub
				Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
				for (int i = 0; i < road.mNodes.size(); i++){
					RoadNode node = road.mNodes.get(i);
					Marker nodeMarker = new Marker(map);
					nodeMarker.setPosition(node.mLocation);
					nodeMarker.setIcon(nodeIcon);
					nodeMarker.setTitle("Step "+ i);
					nodeMarker.setSnippet(node.mInstructions);
					nodeMarker.setSubDescription(Road.getLengthDurationText(node.mLength, node.mDuration));
					Drawable icon = getManouverTypeIcon(node);
					nodeMarker.setImage(icon);
					map.getOverlays().add(nodeMarker);
				}
			}

			private Drawable getManouverTypeIcon(RoadNode node) {
				Drawable manouverIcon;
				switch(node.mManeuverType) {
				case 0 : manouverIcon = getResources().getDrawable(R.drawable.ic_empty);
				break;

				case 1 : manouverIcon = getResources().getDrawable(R.drawable.ic_continue);
				break;

				case 6 : manouverIcon = getResources().getDrawable(R.drawable.ic_slight_right);
				break;

				case 7 : manouverIcon = getResources().getDrawable(R.drawable.ic_turn_right);
				break;

				case 8 : manouverIcon = getResources().getDrawable(R.drawable.ic_sharp_right);
				break;

				case 12 : manouverIcon = getResources().getDrawable(R.drawable.ic_u_turn);
				break;

				case 5 : manouverIcon = getResources().getDrawable(R.drawable.ic_sharp_left);
				break;

				case 4 : manouverIcon = getResources().getDrawable(R.drawable.ic_turn_left);
				break;

				case 3 : manouverIcon = getResources().getDrawable(R.drawable.ic_slight_left);
				break;

				case 24 : manouverIcon = getResources().getDrawable(R.drawable.ic_arrived);
				break;

				case 27 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 28 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 29 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 30 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 31 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 32 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 33 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				case 34 : manouverIcon = getResources().getDrawable(R.drawable.ic_roundabout);
				break;

				default : manouverIcon = getResources().getDrawable(R.drawable.ic_empty);
				break;
				}
				return manouverIcon;

			}

			private Address getPointFromPlaceName(String searchString) {
				// TODO Auto-generated method stub
				List<Address> result;
				Address address = null;
				try {
					result = mGeoCoder.getFromLocationName(searchString, 5);
					if ((result == null)||(result.isEmpty())){
						Toast.makeText(getActivity(),
								"No matches were found or there is no backend service!",
								Toast.LENGTH_LONG).show();
					} else {
						address = result.get(0);
						double latitude = address.getLatitude();
						double longitude = address.getLongitude(); 
						Log.d(TAG, "Latitude: " + latitude + "Longitude: " + longitude);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return address;
			}

			private void getAddressFromLocationPoint(GeoPoint startPoint) {
				// TODO Auto-generated method stub
				List<Address> result;
				try {
					StringBuilder sb = new StringBuilder();
					result = mGeoCoder.getFromLocation(48.13, -1.63, 5);
					if ((result == null)||(result.isEmpty())){
						Toast.makeText(getActivity(),
								"No matches were found or there is no backend service!",
								Toast.LENGTH_LONG).show();
					}else{
						Address address = result.get(0);
						int n = address.getMaxAddressLineIndex();
						for(int i = 0; i <= n; i++) {
							if(i!=0){
								sb.append(", ");
							}
							sb.append(address.getAddressLine(i));
						}
						String addressString = new String(sb.toString());
						Log.d(TAG, "Address: " + addressString);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start(); 

		OnClickListener downloadMapClickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				CacheManager cacheManager = new CacheManager(map);
				int zoomMin = map.getZoomLevel();
				int zoomMax = map.getZoomLevel() + 4;
				cacheManager.downloadAreaAsync(getActivity(), map.getBoundingBox(), zoomMin, zoomMax);
			}
		};
		down_button.setOnClickListener(downloadMapClickListener);
		map.invalidate();
		return rootView;
	}
}
