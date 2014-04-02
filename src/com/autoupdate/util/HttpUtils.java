package com.autoupdate.util;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public final class HttpUtils {

	private HttpUtils(){}
	
	
	public static JSONObject getJSONObj(String url){
		String jsonStr = HTTPGET(url);
		JSONObject jsonObj = null;
		try {
			jsonObj = new JSONObject(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	private static String HTTPGET(String url) {
		HttpClient client = new DefaultHttpClient();
		try {
			HttpGet  httpGet = new HttpGet(url);
			HttpResponse response = client.execute(httpGet);
			HttpEntity entity = response.getEntity();
			String jsonStr = EntityUtils.toString(entity, "utf-8");
			return jsonStr;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
