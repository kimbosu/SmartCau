package com.cau.portal.utils;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class Utils {

	public static String excutePost(String targetURL, String urlParameters, boolean isJson) {
		Log.d("cau","URL : "+targetURL);
		Log.d("cau","DATA : "+urlParameters);
		
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create connection
			  url = new URL(targetURL);
		      connection = (HttpURLConnection)url.openConnection();
		      connection.setRequestMethod("POST");
		      connection.setRequestProperty("Content-Type", isJson ? "application/json" : "application/x-www-form-urlencoded");
					
		      connection.setRequestProperty("Content-Length", "" + 
		               Integer.toString(urlParameters.getBytes().length));
		      connection.setRequestProperty("Content-Language", "UTF-8");  
		      
		      connection.setUseCaches (false);
		      connection.setDoInput(true);
		      connection.setDoOutput(true);
		      
			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write( urlParameters.getBytes("UTF-8") );
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			
			//System.out.println(connection.getHeaderFields());
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				//response.append('\r');
			}
			rd.close();
			//System.out.println(response.toString());
			return response.toString();

		} catch (Exception e) {

			e.printStackTrace();
			return null;

		} finally {

			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static void setSharedPreferences(Context context, String key, String value){
		SharedPreferences prefs = context.getSharedPreferences("smartcau", MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static String getSharedPreferences(Context context, String key){
		SharedPreferences prefs = context.getSharedPreferences("smartcau", MODE_PRIVATE);
		return prefs.getString(key, "");
	}

	public static boolean isEmpty(String input){
		return input == null || input.equals("");
	}

	public static Map<String, Object> jsonToMap(String json) throws JSONException {

		JSONObject obj = new JSONObject(json);

		Map<String, Object> retMap = new HashMap<String, Object>();

		if(obj != JSONObject.NULL) {
			retMap = toMap(obj);
		}
		return retMap;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while(keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for(int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if(value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if(value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static void alert(final Context context, final String msg){
		Handler mHandler = new Handler(Looper.getMainLooper());
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		}, 0);
	}

	public static String formatMoney(String input){
		double amount = Double.parseDouble(input);
		DecimalFormat formatter = new DecimalFormat("#,###");
		return formatter.format(amount);
	}

	public static String getPhone(Context context){
		TelephonyManager telephonyManager = (TelephonyManager) context.getApplicationContext().getSystemService(context.getApplicationContext().TELEPHONY_SERVICE);
		String userPhone = telephonyManager.getLine1Number();

		userPhone = userPhone.replace("+82", "0");
		//Toast.makeText(getApplicationContext(), userPhone, Toast.LENGTH_LONG).show();

		Log.d("cau", userPhone);
		return userPhone;
	}

	/**
	 * 전화번호 포맷
	 * @param phoneNumber
	 * @return
	 */
	public static String formatPhoneNumber(String phoneNumber) {
		String regEx = "(\\d{3})(\\d{3,4})(\\d{4})";

		if(!Pattern.matches(regEx, phoneNumber)) return null;

		return phoneNumber.replaceAll(regEx, "$1-$2-$3");
	}

	/**
	 * 알림창표시
	 * @param title
	 * @param msg
	 */
	public void showAlert(final Context context, String title, String msg, final boolean isEnd){
		//다이얼로그 지정
		AlertDialog.Builder alertdialog = new AlertDialog.Builder(context);
		//다이얼로그의 내용을 설정합니다.
		alertdialog.setTitle(title);
		alertdialog.setMessage(msg);

		//확인 버튼
		alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//확인 버튼이 눌렸을 때 토스트를 띄워줍니다.
				if(isEnd){
					Toast.makeText(context, "앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
					((Activity)context).finish();
				}
			}
		});

		//취소 버튼
		alertdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//취소 버튼이 눌렸을 때 토스트를 띄워줍니다.
				Toast.makeText(context, "취소를 선택하였습니다.", Toast.LENGTH_SHORT).show();
			}
		});

		AlertDialog alert = alertdialog.create();
		alert.show();
	}



}
