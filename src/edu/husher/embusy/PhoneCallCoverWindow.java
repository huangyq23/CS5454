package edu.husher.embusy;

import java.lang.reflect.Method;
import java.util.ArrayList;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.TelephonyManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PhoneCallCoverWindow extends StandOutWindow {

	private Button declineButton;
	private TextView smsView;
	private View view;
	private LinearLayout selectionContainer;
	private EBOnClickeListener buttonClicklistener;
	private EBOnSeekBarChangeListener seekbarListener;
	private ArrayList<EBSuggestion> suggestionlist;
	
	//private String suggestionPattern = "Can't talk now, call you back later.";
	private EBSuggestion currentsuggestion;
	private int currentETA = 0;
	
	private ArrayList<Button> buttonList;
	private SeekBar seekBar;
	private String currentNumber;

	@Override
	public String getAppName() {
		return "SimpleWindow";
	}

	@Override
	public int getAppIcon() {
		return android.R.drawable.ic_menu_close_clear_cancel;
	}

	@Override
	public String getPersistentNotificationMessage(int id) {
		return "";
	}

	@Override
	public Intent getPersistentNotificationIntent(int id) {
		return getCloseAllIntent(this, PhoneCallCoverWindow.class);
	}

	@Override
	public void createAndAttachView(int id, FrameLayout frame) {
		// create a new layout from body.xml
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		view = inflater
				.inflate(R.layout.activity_phone_call_cover, frame, true);
		selectionContainer = (LinearLayout) view.findViewById(R.id.seletionContainer);
		declineButton = (Button) view.findViewById(R.id.declineButton);
		
		smsView = (TextView) view.findViewById(R.id.smsView);
		seekBar = (SeekBar) view.findViewById(R.id.seekBar);
		buttonClicklistener = new EBOnClickeListener();
		seekbarListener = new EBOnSeekBarChangeListener();
		
		
		currentsuggestion = new EBSuggestion("", "Can't talk right now, call you back in %d mins.", 0); 
		
		declineButton.setOnClickListener(buttonClicklistener);
		seekBar.setOnSeekBarChangeListener(seekbarListener);
	}
	
	public void renderList(){
		
		
		buttonList = new ArrayList<Button>();
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		
		selectionContainer.removeAllViews();

		for(EBSuggestion ebs: suggestionlist){
			Button b = (Button) inflater.inflate(R.layout.selection_button, selectionContainer, false);
			b.setText(ebs.eta+"mins \u2794 "+ebs.titlePattern);
			b.setOnClickListener(buttonClicklistener);
			selectionContainer.addView(b);
			buttonList.add(b);
		}
		
		
	}
	
	
	

	// the window will be centered
	@Override
	public StandOutLayoutParams getParams(int id, Window window) {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		// int height = size.y;
		return new StandOutLayoutParams(id, width, 736, 0, 0);
	}

	// move the window by dragging the view
	@Override
	public int getFlags(int id) {
		return super.getFlags(id)
				| StandOutFlags.FLAG_WINDOW_FOCUS_INDICATOR_DISABLE;
	}


	public boolean killCall(Context context) {
		try {
			// Get the boring old TelephonyManager
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// Get the getITelephony() method
			Class classTelephony = Class.forName(telephonyManager.getClass()
					.getName());
			Method methodGetITelephony = classTelephony
					.getDeclaredMethod("getITelephony");
			// Ignore that the method is supposed to be private
			methodGetITelephony.setAccessible(true);
			// Invoke getITelephony() to get the ITelephony interface
			Object telephonyInterface = methodGetITelephony
					.invoke(telephonyManager);
			// Get the endCall method from ITelephony
			Class telephonyInterfaceClass = Class.forName(telephonyInterface
					.getClass().getName());
			Method methodEndCall = telephonyInterfaceClass
					.getDeclaredMethod("endCall");
			// Invoke endCall()
			methodEndCall.invoke(telephonyInterface);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void onReceiveData(int id, int requestCode, Bundle data,
			Class<? extends StandOutWindow> fromCls, int fromId) {
		// TODO Auto-generated method stub
		//super.onReceiveData(id, requestCode, data, fromCls, fromId);
		Log.d("Windows Incoming data ", requestCode+"");
		if (requestCode==1){
			close(DEFAULT_ID);
		}else if(requestCode==2){
			currentNumber = data.getString("Number");
			suggestionlist = data.getParcelableArrayList("Suggestions");
			renderList();
		}else if(requestCode==3){
			ArrayList<EBSuggestion> suggestionlist2 = data.getParcelableArrayList("Suggestions");
			suggestionlist.addAll(suggestionlist2);
			renderList();
		}
		
	}
	
	
	
	private void renderSuggestionSMS(){
		String suggestion = String.format(currentsuggestion.pattern, currentETA);
		smsView.setText(suggestion);
	}
	
	private void setCurrentETA(int eta, boolean update){
		currentETA = eta;
		
		int progress = (int) Math.floor(Math.log10(eta)/Math.log10(1.04179));
		renderSuggestionSMS();
		if(update){
			seekBar.setProgress(progress);
		}
	}
	
	private void sendSMS(){
		SmsManager smsm = SmsManager.getDefault();
		smsm.sendTextMessage(currentNumber, null, smsView.getText().toString(), null, null);
	}
	
	
	private class EBOnSeekBarChangeListener implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if(fromUser){
				int eta = (int) Math.ceil(Math.pow(1.04179, progress));
				setCurrentETA(eta, false);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class EBOnClickeListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			if(v==declineButton){
				killCall(getApplicationContext());
				sendSMS();
				closeAll();
			}else if(buttonList.contains(v)){
				int index = buttonList.indexOf(v);
				currentsuggestion = suggestionlist.get(index);
				setCurrentETA(currentsuggestion.eta, true);
				renderSuggestionSMS();
			}
			
		}
		
	}
}
