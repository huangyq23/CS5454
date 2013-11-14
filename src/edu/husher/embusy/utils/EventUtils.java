package edu.husher.embusy.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

public class EventUtils {

	public static EBCalendarEvent getCurrentEvent(ContentResolver cr) {
		long now = System.currentTimeMillis();

		String instanceQuery = "begin <= " + now + " AND end >= " + now;

		Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
				.buildUpon();
		ContentUris.appendId(eventsUriBuilder, now);
		ContentUris.appendId(eventsUriBuilder, now);
		Uri eventsUri = eventsUriBuilder.build();
		Cursor cursor = null;
		cursor = cr.query(eventsUri, new String[] {
				CalendarContract.Instances.TITLE,
				CalendarContract.Instances.BEGIN,
				CalendarContract.Instances.END }, instanceQuery, null, null);

		cursor.moveToFirst();
		String CNames[] = new String[cursor.getCount()];
		EBCalendarEvent ebce = null;
		long latestEndMs = 0;
		for (int i = 0; i < CNames.length; i++) {
			EBCalendarEvent hce = new EBCalendarEvent();
			hce.title = cursor.getString(0);
			//long startMs = Long.parseLong(cursor.getString(1));
			long endMs = Long.parseLong(cursor.getString(2));
			if(ebce==null || endMs>latestEndMs){
				hce.remainingSeconds =  (int)((endMs-now)/1000);
				ebce = hce;
				
			}
			cursor.moveToNext();
		}
		cursor.close();
		return ebce;
	}

	public static class EBCalendarEvent {
		public String title;
		public int remainingSeconds;
	}
}
