package com.example.flickrglass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.glass.app.Card;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class MainActivity extends Activity {
	private ArrayList<Card> photosCards = new ArrayList<Card>();

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	private OAuthToken oauthToken;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i("Pietje", "Puk");
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		List<String> allProviders = locationManager.getAllProviders();
		for (String provider : allProviders) {
			Log.i("PROVIDERS", "Provider: " + provider);
		}
		try {
			LocationListener locationListener = new LocationListener() {
				public void onLocationChanged(Location location) {
					try {
						// Called when a new location is found by the network
						// location provider.
						Log.i("Pietje2", "Long: " + location.getLongitude());
						Log.i("Pietje2", "Lat:  " + location.getLatitude());

						FlickrLookup lookup = new FlickrLookup() {
							protected void onPostExecute(PhotoList result) {
								try {
									onNewPhotos(result);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
						lookup.execute(location);
					} catch (Exception e) {
						Log.e("Pietje2", "Kon locatie niet bepalen", e);
					}
				}

				public void onStatusChanged(String provider, int status,
						Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
				}
			};

			// Register the listener with the Location Manager to receive
			// location updates
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, locationListener);

			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				Log.i("Pietje", "Long: " + location.getLongitude());
				Log.i("Pietje", "Lat:  " + location.getLatitude());
			}
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onResume() {
		super.onResume();

	}

	public void onNewPhotos(PhotoList photos) throws IOException {
		Log.i("Pietje2", "PostExecute: " + photos.size());
		for (Photo photo : photos) {
			Log.i("Photo", "id: " + photo.getId());
			Log.i("Photo", "url:" + photo.getMedium640Url());
			Log.i("Photo", "title:" + photo.getTitle());
			Log.i("Photo", "description:" + photo.getDescription());
			Log.i("Photo", "owner:" + photo.getOwner().getUsername());
		}
		try {
			for (Photo photo : photos) {
				Context context = getApplicationContext();
				Card card = new Card(context);
				card.setImageLayout(Card.ImageLayout.FULL);
				card.setText(photo.getTitle() + "\n\n" + photo.getDescription());
				card.setFootnote(photo.getOwner().getRealName());
				card.addImage(Drawables.drawableFromUrl(photo.getMedium640Url()));
			}
		} catch (Throwable e) {
			Log.e("ERROR", "Kan geen Card aanmaken: " + e.getClass().getName(),
					e);
		}
	}
}
