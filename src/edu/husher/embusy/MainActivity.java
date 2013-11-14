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
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

  MainActivity ma = this;
  boolean mIsBound;

  private EmbusyBackgroundService mBoundService;
  
  private EditText homeLatitudeTextField;
  private EditText homeLongitudeTextField;
  private EditText workLatitudeTextField;
  private EditText workLongitudeTextField;
  

  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      // This is called when the connection with the service has been
      // established, giving us the service object we can use to
      // interact with the service. Because we have bound to a explicit
      // service that we know is running in our own process, we can
      // cast its IBinder to a concrete class and directly access it.
      mBoundService = ((EmbusyBackgroundService.LocalBinder) service)
          .getService();

      // Tell the user about this for our demo.
      Toast.makeText(ma, "connected", Toast.LENGTH_SHORT).show();
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      // Because it is running in our same process, we should never
      // see this happen.
      mBoundService = null;
      Toast.makeText(ma, "disconnected", Toast.LENGTH_SHORT).show();
    }
  };
private LocationStore locationStore;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ma = this;
    mIsBound = false;

    Button startServiceButton = (Button) findViewById(R.id.start_service_button);
    startServiceButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // your handling code goes here.
        // the v argument is the view that triggered this event, in this case
        // the button.
        // if you want to interact with it as a Button, you'll have to cast it
        // to Button yourself
        Intent i = new Intent(ma, EmbusyBackgroundService.class);
        ma.startService(i);
        // doBindService();
      }
    });

    Button stopServiceButton = (Button) findViewById(R.id.stop_service_button);
    stopServiceButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(ma, EmbusyBackgroundService.class);
        doUnbindService();
        ma.stopService(i);
      }
    });

    Button gpsButton = (Button) findViewById(R.id.update_gps);
    gpsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {

        if (mBoundService == null) {
          Toast.makeText(ma, "mbound is null", Toast.LENGTH_SHORT).show();
        }

        else if (mBoundService.getLocation() == null) {
          Toast.makeText(ma, "location null", Toast.LENGTH_SHORT).show();
        }

        else {
          Toast.makeText(
              ma,
              "" + mBoundService.getLocation().getLatitude() + " "
                  + mBoundService.finalResult, Toast.LENGTH_SHORT).show();
        }

      }
    });

    
    homeLatitudeTextField = (EditText) findViewById(R.id.value_latitude_1);
    homeLongitudeTextField = (EditText) findViewById(R.id.value_longitude_1);
    workLatitudeTextField = (EditText) findViewById(R.id.value_latitude_2);
    workLongitudeTextField = (EditText) findViewById(R.id.value_longitude_2);
    
    locationStore = new LocationStore(ma);
    LatLng homeLocation =  locationStore.getLocation("home");
    LatLng workLocation = locationStore.getLocation("work");
    if(homeLocation!=null){
    	homeLatitudeTextField.setText(Float.toString(homeLocation.lat));
    	homeLongitudeTextField.setText(Float.toString(homeLocation.lng));
    }
    if(workLocation!=null){
    	workLatitudeTextField.setText(Float.toString(workLocation.lat));
    	workLongitudeTextField.setText(Float.toString(workLocation.lng));
    }
    
    
    Button locationSetButton = (Button) findViewById(R.id.location_set_button);
    locationSetButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        
        float home_lat = Float.parseFloat(ma.homeLatitudeTextField.getText().toString());
        float home_lng = Float.parseFloat(ma.homeLongitudeTextField.getText().toString());
        float work_lat = Float.parseFloat(ma.workLatitudeTextField.getText().toString());
        float work_lng = Float.parseFloat(ma.workLongitudeTextField.getText().toString());
       
        locationStore.setLocation("home", home_lat, home_lng);
        locationStore.setLocation("work", work_lat, work_lng);
        
        Toast.makeText(ma, "saved" , Toast.LENGTH_SHORT).show();
      }
    });
    
    


  }
  
  
  

  void doBindService() {
    // Establish a connection with the service. We use an explicit
    // class name because we want a specific service implementation that
    // we know will be running in our own process (and thus won't be
    // supporting component replacement by other applications).
    bindService(new Intent(ma, EmbusyBackgroundService.class), mConnection,
        BIND_AUTO_CREATE);
    mIsBound = true;
  }

  void doUnbindService() {
    if (mIsBound) {
      // Detach our existing connection.
      unbindService(mConnection);
      mIsBound = false;
    }
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
    doUnbindService();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // when activity is resumed:
    // mBound will not be ready if Activity is first created, in this case use
    // onServiceConnected() callback perform service call.
    if (mBoundService == null) // <- or simply check if (mBoundservice != null)
      doBindService();
  }





}
