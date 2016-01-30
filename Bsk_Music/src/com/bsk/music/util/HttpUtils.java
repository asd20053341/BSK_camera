package com.bsk.music.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;


public class HttpUtils {
	
	private static URL url;
	private static HttpURLConnection conn;
	
	public HttpUtils() {
		
	}
	
	public static String doGet(String url_path) {
		Log.e("", "url=" + url_path);
		try {
			url = new URL(url_path);
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(8000);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			int code = conn.getResponseCode();
			if(code == 200) {
				return convertStreamToString(conn.getInputStream());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
		return null;
	}
	
	
	

	public static InputStream getInputStream(String url) {
		InputStream is = null;
		DefaultHttpClient httpclient = null;
		HttpGet httpget = null;
		HttpResponse response = null;
		HttpEntity entity = null;
		
		try {
			httpclient = new DefaultHttpClient();
	        httpget = new HttpGet(url);
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			is = entity.getContent();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}

	private static String convertStreamToString(InputStream is) {
		String jsonString = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = 0;
		byte[] data = new byte[1024];
		try {
			while((len = is.read(data)) != -1) {
				baos.write(data, 0, len);
			}
			jsonString = new String(baos.toByteArray());
		}catch(IOException e) {
			
		}
//		Log.i("", "jsonString= " + jsonString);
		return jsonString;
	}
}
