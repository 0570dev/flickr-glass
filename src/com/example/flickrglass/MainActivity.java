package com.example.flickrglass;

import java.io.IOException;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class MainActivity extends Activity implements LocationListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

		try {
			// Register the listener with the Location Manager to receive
			// location updates
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);

			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				onLocationChanged(location);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onNewPhotos(PhotoList photos) throws IOException {
		for (Photo photo : photos) {
			Log.i("Photo", "id: " + photo.getId());
			Log.i("Photo", "url:" + photo.getMedium640Url());
			Log.i("Photo", "title:" + photo.getTitle());
			Log.i("Photo", "description:" + photo.getDescription());
			Log.i("Photo", "owner:" + photo.getOwner().getUsername());
		}
		// try {
		// for (Photo photo : photos) {
		// Context context = getApplicationContext();
		// Card card = new Card(context);
		// card.setImageLayout(Card.ImageLayout.FULL);
		// card.setText(photo.getTitle() + "\n\n" +
		// photo.getDescription());
		// card.setFootnote(photo.getOwner().getRealName());
		// card.addImage(Drawables.drawableFromUrl(photo.getMedium640Url()));
		// }
		// } catch (Throwable e) {
		// Log.e("ERROR", "Kan geen Card aanmaken: " + e.getClass().getName(),
		// e);
		// }
	}

	@Override
	public void onLocationChanged(Location location) {
		try {
			// Called when a new location is found by the network
			// location provider.
			Log.i("Location", "Long: " + location.getLongitude() + "/Lat: "
					+ location.getLatitude());

			FlickrLookup lookup = new FlickrLookup() {
				protected void onPostExecute(PhotoList result) {
					try {
						onNewPhotos(result);
					} catch (IOException e) {
					}
				}
			};
			lookup.execute(location);
		} catch (Exception e) {
			Log.e("Location", "Kon locatie niet bepalen", e);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}
}
