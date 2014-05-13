package com.example.flickrglass;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.MediaStore.Files;
import android.util.Log;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.Parameter;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.oauth.OAuthUtils;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.googlecode.flickrjandroid.util.IOUtilities;
import com.googlecode.flickrjandroid.util.UrlUtilities;

public class FlickrLookup extends AsyncTask<Location, Void, PhotoList> {
	@Override
	protected PhotoList doInBackground(Location... locations) {
		PhotoList list = new PhotoList();
		try {
			for (Location location : locations) {
				Flickr f = new Flickr("4a67c256e5b0e4db6aa274513108928c",
						new MyREST());

				SearchParameters pars = new SearchParameters();
				pars.setLatitude(Double.toString(location.getLatitude()));
				pars.setLongitude(Double.toString(location.getLongitude()));
				HashSet<String> extras = new HashSet<String>();
				extras.add("owner_name");
				extras.add("description");
				pars.setExtras(extras);
				list.addAll(f.getPhotosInterface().search(pars, 10, 0));
			}
			for (Photo photo : list) {
				HttpURLConnection connection = (HttpURLConnection) new URL(
						photo.getMedium640Url()).openConnection();
				connection.connect();
				InputStream input = connection.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				byte[] buffer = new byte[32768];
				int len = input.read(buffer);
				while (len != -1) {
					baos.write(buffer, 0, len);
					len = input.read(buffer);
				}
				photo.setBytes(baos.toByteArray());

				Log.i("Photo",
						"id: " + photo.getId() + ", url:"
								+ photo.getMedium640Url() + ", title:"
								+ photo.getTitle() + ", description:"
								+ photo.getDescription() + ", owner:"
								+ photo.getOwner().getUsername() + ", size: "
								+ photo.getBytes().length);
				connection.disconnect();
			}
		} catch (Exception e) {
			Log.e("FlickrLookup", e.getClass() + ":" + e.getMessage(), e);
		}
		return list;
	}

	private class MyREST extends REST {
		private MyREST() throws ParserConfigurationException {
			super(Flickr.DEFAULT_HOST);
		}

		/**
		 * Send a GET request to the provided URL with the given parameters,
		 * then return the response as a String.
		 * 
		 * @param path
		 * @param parameters
		 * @return the data in String
		 * @throws IOException
		 */
		public String getLine(String path, List<Parameter> parameters)
				throws IOException {
			InputStream in = null;
			BufferedReader rd = null;
			try {
				in = getInputStream(path, parameters);
				rd = new BufferedReader(new InputStreamReader(in,
						OAuthUtils.ENC));
				final StringBuffer buf = new StringBuffer();
				String line;
				while ((line = rd.readLine()) != null) {
					buf.append(line);
				}

				return buf.toString();
			} finally {
				IOUtilities.close(in);
				IOUtilities.close(rd);
			}
		}

		private InputStream getInputStream(String path,
				List<Parameter> parameters) throws IOException {
			URL url = buildUrl(getHost(), 443, path, parameters);
			Log.i("REST", "url=" + url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("Cache-Control", "no-cache,max-age=0");
			conn.addRequestProperty("Pragma", "no-cache");
			conn.setRequestMethod("GET");
			if (isProxyAuth()) {
				conn.setRequestProperty("Proxy-Authorization", "Basic "
						+ getProxyCredentials());
			}
			conn.setDoOutput(false);
			conn.setInstanceFollowRedirects(false);
			conn.connect();
			int status = conn.getResponseCode();
			if (status == 200) {
				return conn.getInputStream();
			} else {
				return conn.getErrorStream();
			}
		}

		private URL buildUrl(String host, int port, String path,
				List<Parameter> parameters) throws MalformedURLException {
			// see: AuthUtilities.getSignature()
			// AuthUtilities.addAuthToken(parameters);

			StringBuffer buffer = new StringBuffer();
			buffer.append("https://");
			buffer.append(host);
			if (port > 0 & port != 443) {
				buffer.append(":");
				buffer.append(port);
			}
			if (path == null) {
				path = "/";
			}
			buffer.append(path);

			Iterator<Parameter> iter = parameters.iterator();
			if (iter.hasNext()) {
				buffer.append("?");
			}
			while (iter.hasNext()) {
				Parameter p = (Parameter) iter.next();
				buffer.append(p.getName());
				buffer.append("=");
				Object value = p.getValue();
				if (value != null) {
					String string = UrlUtilities.encode(value.toString());
					buffer.append(string);
				}
				if (iter.hasNext())
					buffer.append("&");
			}

			/*
			 * RequestContext requestContext =
			 * RequestContext.getRequestContext(); Auth auth =
			 * requestContext.getAuth(); if (auth != null &&
			 * !ignoreMethod(getMethod(parameters))) {
			 * buffer.append("&api_sig=");
			 * buffer.append(AuthUtilities.getSignature(sharedSecret,
			 * parameters)); }
			 */

			return new URL(buffer.toString());
		}
	}
}
