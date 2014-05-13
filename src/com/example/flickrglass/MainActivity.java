package com.example.flickrglass;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;

public class MainActivity extends CardScrollActivity implements
		LocationListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try {
			// LocationManager locationManager = (LocationManager)
			// getSystemService(LOCATION_SERVICE);
			// Register the listener with the Location Manager to receive
			// location updates
			// locationManager.requestLocationUpdates(
			// LocationManager.GPS_PROVIDER, 0, 0, this);

			Location location = null;
			// locationManager
			// .getLastKnownLocation(LocationManager.GPS_PROVIDER);

			location = new Location("GPS");
			location.setLatitude(52.255992);
			location.setLongitude(6.160466);

			onLocationChanged(location);
		} catch (Exception e) {
			Log.e("ERROR", e.getClass().getName(), e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onNewPhotos(PhotoList photos) throws IOException {
		try {
			for (Photo photo : photos) {
				Card card = new Card(this);
				card.setImageLayout(Card.ImageLayout.FULL);
				card.setText(photo.getTitle() + "\n\n" + photo.getDescription());
				card.setFootnote(photo.getOwner().getRealName());

				Bitmap bmp = BitmapFactory.decodeByteArray(photo.getBytes(), 0,
						photo.getBytes().length);
				card.addImage(bmp);
				addCard(card);
			}
			updateView();
		} catch (Throwable e) {
			Log.e("ERROR", "Kan geen Card aanmaken: " + e.getClass().getName(),
					e);
		}
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
