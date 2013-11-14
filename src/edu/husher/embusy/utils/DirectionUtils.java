package edu.husher.embusy.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DirectionUtils {
	public static int getDurationBetween(float lat1, float lng1, float lat2,
			float lng2) {
		String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
				+ lat1
				+ ","
				+ lng1
				+ "&destination="
				+ lat2
				+ ","
				+ lng2
				+ "&sensor=false&mode=walking";
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response;
		try {
			response = httpclient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(response.getEntity().getContent())));
				StringBuilder wholeText = new StringBuilder();
				String output;
				while ((output = br.readLine()) != null) {
					wholeText.append(output + "\n");
				}

				JSONObject jObject = new JSONObject(wholeText.toString());
				JSONArray routes = jObject.getJSONArray("routes");
				if (routes.length() == 0)
					return -1;
				JSONObject route0 = routes.getJSONObject(0);
				JSONArray legs = route0.getJSONArray("legs");
				JSONObject leg0 = legs.getJSONObject(0);
				JSONObject duration = leg0.getJSONObject("duration");
				int seconds = duration.getInt("value");
				return seconds;

			} else {
				// Closes the connection.
				response.getEntity().getContent().close();
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
		return -1;
	}
}
