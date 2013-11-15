package edu.husher.embusy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import edu.husher.embusy.LocationStore.LatLng;
import edu.husher.embusy.utils.DirectionUtils;
import edu.husher.embusy.utils.EventUtils;
import edu.husher.embusy.utils.EventUtils.EBCalendarEvent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.telephony.TelephonyManager;
import android.util.Log;

public class EmbusyBackgroundService extends Service implements
		SensorEventListener {
	private static final String TAG = "EMBUSYGPS";

	private LocationManager mLocationManager = null;
	private static final int LOCATION_INTERVAL = 60 * 1000;
	private static final float LOCATION_DISTANCE = 10f;
	private LocationListener locationListener;

	private EmbusyBackgroundService ms = this;

	private boolean rang = false;
	private PhoneCallStateBroadcastReceiver pcsbr;

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private LocationStore locationStore;

	public ArrayList<String> classifyResults;
	public String finalResult;

	public Location getLocation() {
		Log.e(TAG, "Getting location");
		return locationListener.getCurrentBestLocation();
	}
	
	private static EmbusyBackgroundService instance = null;
	
	public static boolean isInstanceCreated() { 
      return instance != null; 
   }
	
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);

		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

		if (state != null) {
			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)
					|| state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				if (rang) {
					hideWindow();
				}
			} else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				String incomingNumber = intent
						.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				rang = true;
				showWindow(incomingNumber);
			}
		}
		return START_STICKY;
	}

	private void showWindow(String number) {
		PhoneCallCoverWindow.closeAll(this, PhoneCallCoverWindow.class);
		PhoneCallCoverWindow.show(this, PhoneCallCoverWindow.class,
				PhoneCallCoverWindow.DEFAULT_ID);
		ArrayList<EBSuggestion> suggestionlist = new ArrayList<EBSuggestion>();

		EBCalendarEvent event = EventUtils
				.getCurrentEvent(getContentResolver());
		if (event != null) {
			suggestionlist.add(new EBSuggestion(event.title,
					"Can't talk now, call you back in %d mins",
					event.remainingSeconds / 60));
		}
		
		if(finalResult.equals(("walk"))){
			
		}

		Location loc = locationListener.getCurrentBestLocation();
		if (loc != null) {
			final LatLng homeLocation = locationStore.getLocation("home");
			final LatLng workLocation = locationStore.getLocation("work");
			
			
			new Thread(new Runnable() {
	          public void run() {
	        	ArrayList<EBSuggestion>  locationSuggestionlist = new ArrayList<EBSuggestion>();
	  			float lat = (float) locationListener.getCurrentBestLocation()
	  					.getLatitude();
	  			float lng = (float) locationListener.getCurrentBestLocation()
	  					.getLongitude();
	  			

	  			int homeDirectionTime = DirectionUtils.getDurationBetween(lat, lng,
	  					homeLocation.lat, homeLocation.lng);

	  			Log.e(TAG, "Direction Got: " + homeDirectionTime);
	  			if (homeDirectionTime >= 3) {
	  				locationSuggestionlist.add(new EBSuggestion("Walk to Home",
	  						"Heading home, arrive in %d mins",
	  						homeDirectionTime / 60));
	  			}

	  			int workDirectionTime = DirectionUtils.getDurationBetween(lat, lng,
	  					workLocation.lat, workLocation.lng);

	  			if (workDirectionTime>=3) {
	  				locationSuggestionlist.add(new EBSuggestion("Walk to Work",
	  						"On my way to office, call you back in %d mins",
	  						workDirectionTime / 60));
	  			}
	  			
	  			Bundle b = new Bundle();
	  			b.putParcelableArrayList("Suggestions", locationSuggestionlist);
	  			PhoneCallCoverWindow.sendData(EmbusyBackgroundService.this, PhoneCallCoverWindow.class,
	  					PhoneCallCoverWindow.DEFAULT_ID, 3, b, null, 0);
	          }
			}).start();
			
		}

		Bundle b = new Bundle();
		b.putString("Number", number);
		b.putParcelableArrayList("Suggestions", suggestionlist);
		PhoneCallCoverWindow.sendData(this, PhoneCallCoverWindow.class,
				PhoneCallCoverWindow.DEFAULT_ID, 2, b, null, 0);

	}

	private void hideWindow() {
		PhoneCallCoverWindow.closeAll(this, PhoneCallCoverWindow.class);
	}

	private void generateFinalResult() {
		HashMap<String, Integer> strMap = new HashMap<String, Integer>();
		for (String a : this.classifyResults) {
			Integer cur = strMap.get(a);
			if (cur == null) {
				cur = 0;
			}
			strMap.put(a, cur + 1);
		}
		String maxStr = "";
		int maxVal = 0;
		for (String key : strMap.keySet()) {
			if (strMap.get(key) > maxVal) {
				maxStr = key;
				maxVal = strMap.get(key);
			}
		}
		finalResult = maxStr;
	}

	private class UpdateAccelerometerTask extends TimerTask {

		public void run() {
			// calculate the new position of myBall
			classifyResults = new ArrayList<String>();
			mSensorManager.registerListener(ms, mAccelerometer,
					SensorManager.SENSOR_DELAY_GAME);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSensorManager.unregisterListener(ms);
			generateFinalResult();

		}
	}

	ScheduledExecutorService ses;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		Log.e(TAG, "onCreate");
		initializeLocationManager();
		registerPhoneCallStateBroadCastReceiver();

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// get a handle to the accelerometer via the sensor manager
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		locationStore = new LocationStore(this);

		ses = Executors.newScheduledThreadPool(5);

		ses.scheduleAtFixedRate(new UpdateAccelerometerTask(), 0, 20, TimeUnit.SECONDS);

	}

	private void registerPhoneCallStateBroadCastReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.PHONE_STATE");
		filter.setPriority(99999);
		pcsbr = new PhoneCallStateBroadcastReceiver();
		registerReceiver(pcsbr, filter);
	}

	private void unregisterPhoneCallStateBroadCastReceiver() {
		unregisterReceiver(pcsbr);
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
		instance = null;
		if (mLocationManager != null) {
			try {
				mLocationManager.removeUpdates(locationListener);
			} catch (Exception ex) {
				Log.i(TAG, "fail to remove location listeners, ignore", ex);
			}
		}
		unregisterPhoneCallStateBroadCastReceiver();
		ses.shutdownNow();
	}

	private void initializeLocationManager() {
		Log.e(TAG, "initializeLocationManager");
		if (mLocationManager == null) {
			mLocationManager = (LocationManager) getApplicationContext()
					.getSystemService(Context.LOCATION_SERVICE);
			locationListener = new LocationListener();

			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL,
						LOCATION_DISTANCE, locationListener);
			} catch (java.lang.SecurityException ex) {
				Log.i(TAG, "fail to request location update, ignore", ex);
			} catch (IllegalArgumentException ex) {
				Log.d(TAG,
						"network provider does not exist, " + ex.getMessage());
			}
			try {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, LOCATION_INTERVAL,
						LOCATION_DISTANCE, locationListener);
			} catch (java.lang.SecurityException ex) {
				Log.i(TAG, "fail to request location update, ignore", ex);
			} catch (IllegalArgumentException ex) {
				Log.d(TAG, "gps provider does not exist " + ex.getMessage());
			}
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	private class DataInASecond {
		public ArrayList<DataUnit> arr;
		public long startMs;
		public double mean;
		public double var;
		public double fft[];

		DataInASecond() {
			arr = new ArrayList<DataUnit>();
			startMs = 0;
		}

		private void computeFeatures() {
			for (DataUnit du : arr) {
				du.computeEudis();
			}
			mean = 0;
			for (DataUnit du : arr) {
				mean += du.eudis;
			}
			mean /= arr.size();

			var = 0;
			for (DataUnit du : arr) {
				var += (du.eudis - mean) * (du.eudis - mean);
			}
			var /= arr.size();

			ArrayList<Double> tmpd = new ArrayList<Double>();
			for (DataUnit du : arr) {
				tmpd.add(du.eudis);
			}

			fft = new double[5];
			for (int i = 0; i < 5; i++) {
				fft[i] = goertzel(tmpd, i + 1, tmpd.size());
			}
		}

		public String classify() {
			computeFeatures();
			if (var <= 68.285972) {
				if (var <= 1.357996) {
					return "still";
				} else {
					if (fft[1] <= 4371.576341) {
						if (fft[2] <= 10228.377315) {
							return "walk";
						} else {
							return "run";
						}
					} else {
						if (fft[0] <= 1394.584444) {
							return "walk";
						} else {
							if (fft[0] <= 3392.720288) {
								return "walk";
							} else {
								return "other";
							}
						}
					}
				}
			} else {
				if (fft[1] <= 27449.150444) {
					if (fft[0] <= 25096.917232) {
						return "run";
					} else {
						return "other";
					}
				} else {
					return "other";
				}
			}
		}

	}

	private static double goertzel(ArrayList<Double> accData, double freq,
			double sr) {
		double s_prev = 0;
		double s_prev2 = 0;
		double coeff = 2 * Math.cos((2 * Math.PI * freq) / sr);
		double s;
		for (int i = 0; i < accData.size(); i++) {
			double sample = accData.get(i);
			s = sample + coeff * s_prev - s_prev2;
			s_prev2 = s_prev;
			s_prev = s;
		}
		double power = s_prev2 * s_prev2 + s_prev * s_prev - coeff * s_prev2
				* s_prev;
		return power;
	}

	private class DataUnit {
		public double x;
		public double y;
		public double z;
		public double eudis;
		public long ms;

		public void computeEudis() {
			eudis = Math.sqrt(x * x + y * y + z * z);
		}
	}

	DataInASecond lastSecond = new DataInASecond();

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		DataUnit du = new DataUnit();
		du.ms = System.currentTimeMillis();
		du.x = event.values[0];
		du.y = event.values[1];
		du.z = event.values[2];

		if (du.ms < lastSecond.startMs + 1000) {
			// continue to record
			lastSecond.arr.add(du);
		} else {
			String classifyResult = lastSecond.classify();

			classifyResults.add(classifyResult);
			DataInASecond dia = new DataInASecond();
			dia.startMs = du.ms;
			dia.arr.add(du);

			lastSecond = dia;
		}
	}

	private class LocationListener implements android.location.LocationListener {
		private static final int INTERVAL_THRESHOLD = 1000 * 60 * 1;// 1 Minute
		private Location currentBestLocation;

		public Location getCurrentBestLocation() {
			return currentBestLocation;
		}

		@Override
		public void onLocationChanged(Location location) {
			Log.e(TAG, "onLocationChanged: " + location);
			if (isBetterLocation(location, currentBestLocation)) {
				currentBestLocation = location;
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.e(TAG, "onProviderDisabled: " + provider);
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.e(TAG, "onProviderEnabled: " + provider);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.e(TAG, "onStatusChanged: " + provider);
		}

		protected boolean isBetterLocation(Location location,
				Location currentBestLocation) {
			if (currentBestLocation == null) {
				// A new location is always better than no location
				return true;
			}

			// Check whether the new location fix is newer or older
			long timeDelta = location.getTime() - currentBestLocation.getTime();
			boolean isSignificantlyNewer = timeDelta > INTERVAL_THRESHOLD;
			boolean isSignificantlyOlder = timeDelta < -INTERVAL_THRESHOLD;
			boolean isNewer = timeDelta > 0;

			// If it's been more than two minutes since the current location,
			// use the new location
			// because the user has likely moved
			if (isSignificantlyNewer) {
				return true;
				// If the new location is more than two minutes older, it must
				// be worse
			} else if (isSignificantlyOlder) {
				return false;
			}

			// Check whether the new location fix is more or less accurate
			int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
					.getAccuracy());
			boolean isLessAccurate = accuracyDelta > 0;
			boolean isMoreAccurate = accuracyDelta < 0;
			boolean isSignificantlyLessAccurate = accuracyDelta > 200;

			// Check if the old and new location are from the same provider
			boolean isFromSameProvider = isSameProvider(location.getProvider(),
					currentBestLocation.getProvider());

			// Determine location quality using a combination of timeliness and
			// accuracy
			if (isMoreAccurate) {
				return true;
			} else if (isNewer && !isLessAccurate) {
				return true;
			} else if (isNewer && !isSignificantlyLessAccurate
					&& isFromSameProvider) {
				return true;
			}
			return false;
		}

		/** Checks whether two providers are the same */
		private boolean isSameProvider(String provider1, String provider2) {
			if (provider1 == null) {
				return provider2 == null;
			}
			return provider1.equals(provider2);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}