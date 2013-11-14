package edu.husher.embusy;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

public class PhoneCallStateBroadcastReceiver extends BroadcastReceiver {

	Context context = null;
	private ITelephony telephonyService;

	@Override
	public void onReceive(final Context context, Intent intent) {

		if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			Log.d("PhoneStateReceiver**Call State=", state);

			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
				Intent i = new Intent(context, EmbusyBackgroundService.class);
				i.putExtra(TelephonyManager.EXTRA_STATE,
						TelephonyManager.EXTRA_STATE_IDLE);
				context.startService(i);
			} else if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
				String incomingNumber = intent
						.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				Intent i = new Intent(context, EmbusyBackgroundService.class);
				i.putExtra(TelephonyManager.EXTRA_STATE,
						TelephonyManager.EXTRA_STATE_RINGING);
				i.putExtra(TelephonyManager.EXTRA_INCOMING_NUMBER,
						incomingNumber);
				context.startService(i);

				Log.d("PhoneStateReceiver**Incoming call ", incomingNumber);
				ShowToast(context, " ringing");

			} else {
				Intent i = new Intent(context, EmbusyBackgroundService.class);
				i.putExtra(TelephonyManager.EXTRA_STATE,
						TelephonyManager.EXTRA_STATE_OFFHOOK);
				context.startService(i);
			}
		}
		if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			// Outgoing call
			String outgoingNumber = intent
					.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Log.d("PhoneStateReceiver **Outgoing call ", outgoingNumber);
			ShowToast(context, "Outgoing");
		} else {
			Log.d("PhoneStateReceiver **unexpected intent.action=",
					intent.getAction());
		}
	}

	public void ShowToast(final Context mContext, final String mstr) {
		Toast.makeText(mContext, mstr, Toast.LENGTH_LONG).show();
	}

}
