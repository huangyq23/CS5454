package edu.husher.embusy;

import edu.husher.embusy.LocationStore.LatLng;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends Activity {

	private EditText homeLatitudeTextField;
	private EditText homeLongitudeTextField;
	private EditText workLatitudeTextField;
	private EditText workLongitudeTextField;

	private LocationStore locationStore;
	private Switch serviceSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serviceSwitch = (Switch) findViewById(R.id.serviceSwitch);

		serviceSwitch.setChecked(EmbusyBackgroundService.isInstanceCreated());

		serviceSwitch
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						Intent i = new Intent(MainActivity.this, EmbusyBackgroundService.class);
						if (isChecked) {
							startService(i);
						} else {
							stopService(i);
						}
					}
				});

		homeLatitudeTextField = (EditText) findViewById(R.id.value_latitude_1);
		homeLongitudeTextField = (EditText) findViewById(R.id.value_longitude_1);
		workLatitudeTextField = (EditText) findViewById(R.id.value_latitude_2);
		workLongitudeTextField = (EditText) findViewById(R.id.value_longitude_2);

		locationStore = new LocationStore(MainActivity.this);
		LatLng homeLocation = locationStore.getLocation("home");
		LatLng workLocation = locationStore.getLocation("work");
		if (homeLocation != null) {
			homeLatitudeTextField.setText(Float.toString(homeLocation.lat));
			homeLongitudeTextField.setText(Float.toString(homeLocation.lng));
		}
		if (workLocation != null) {
			workLatitudeTextField.setText(Float.toString(workLocation.lat));
			workLongitudeTextField.setText(Float.toString(workLocation.lng));
		}

		Button locationSetButton = (Button) findViewById(R.id.location_set_button);
		locationSetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				float home_lat = Float.parseFloat(homeLatitudeTextField
						.getText().toString());
				float home_lng = Float.parseFloat(homeLongitudeTextField
						.getText().toString());
				float work_lat = Float.parseFloat(workLatitudeTextField
						.getText().toString());
				float work_lng = Float.parseFloat(workLongitudeTextField
						.getText().toString());

				locationStore.setLocation("home", home_lat, home_lng);
				locationStore.setLocation("work", work_lat, work_lng);

				Toast.makeText(MainActivity.this, "saved", Toast.LENGTH_SHORT).show();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
