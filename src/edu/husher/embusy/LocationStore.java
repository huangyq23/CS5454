package edu.husher.embusy;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class LocationStore {

	// The SharedPreferences object in which geofences are stored
	private final SharedPreferences mPrefs;

	public static float INVALID_FLOAT = -500.0f;

	// The name of the resulting SharedPreferences
	private static final String SHARED_PREFERENCE_NAME = MainActivity.class
			.getSimpleName();

	// Create the SharedPreferences storage with private access only
	public LocationStore(Context context) {
		mPrefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	public void setLocation(String place, float lat, float lng) {
		Editor editor = mPrefs.edit();
		editor.putFloat("lat_" + place, lat);
		editor.putFloat("lng_" + place, lng);
		editor.commit();

	}

	/**
	 * returns -500 if not exist
	 * 
	 * @param place
	 * @return
	 */
	public LatLng getLocation(String place) {
		float lat  = mPrefs.getFloat("lat_" + place, INVALID_FLOAT);
		float lng = mPrefs.getFloat("lng_" + place, INVALID_FLOAT);
		
		if(lat ==INVALID_FLOAT|| lng==INVALID_FLOAT){
			return null;
		}

		return new LatLng(lat, lng);
	}

	public class LatLng {
		public float lat;
		public float lng;

		public LatLng(float lat, float lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}
}
