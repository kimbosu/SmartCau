package com.cau.portal.service;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Build;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.cau.portal.R;
import com.cau.portal.activity.ContentActivity;
import com.cau.portal.utils.Config;
import com.cau.portal.utils.Utils;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.google.android.gms.internal.zzagz.runOnUiThread;

/**
 * Created by user on 2017-11-21.
 */

public class WebAppService {

    private Context context;
    private Handler handler;

    public WebAppService(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    @JavascriptInterface
    public void loadingStart() {
        Log.d("caulog", "loadingStart");
        handler.sendEmptyMessage(1);
    }

    @JavascriptInterface
    public void loadingEnd() {
        Log.d("caulog", "loadingEnd");
        handler.sendEmptyMessage(0);
    }

    @JavascriptInterface
    public void requestLogout() {
        Utils.setSharedPreferences(context, "userId", ""); // 아이디
        Utils.setSharedPreferences(context, "userNo", ""); // 학번
        Utils.setSharedPreferences(context, "encPass", ""); // 비밀번호
        Utils.setSharedPreferences(context, "password", ""); // 비밀번호
        Utils.setSharedPreferences(context, "autoLogin", ""); // 자동로그인값
    }

    @JavascriptInterface
    public void requestLogin(String userId, String password, String autoLogin) {
        Utils.setSharedPreferences(context, "userId", userId); // 아이디
        Utils.setSharedPreferences(context, "password", password); // 비밀번호
        Utils.setSharedPreferences(context, "autoLogin", autoLogin); // 자동로그인값은 항상갱신
        Log.d("caulog", "userId : "+userId);
        Log.d("caulog", "password : "+password);
        Log.d("caulog", "autoLogin : "+autoLogin);
    }

    public static Map<String, String> getQueryMap(String query){
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params)
        {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }
        return map;
    }

    @JavascriptInterface
    public void requestDownload(String url, String param) {
        Log.d("caulog", "requestDownload url : "+url);
        Log.d("caulog", "requestDownload param : "+param);

        try {

//            String testParam = "fileModifyName=청탁금지법 매뉴얼 변경 사항 안내 161017_2016-10-27(13 42 52 4252).hwp&fileOriginName=청탁금지법 매뉴얼 변경 사항 안내 161017_2016-10-27(13 42 52 4252).hwp&fileFolder=/Upload/TIS/GPS/CLE/BRD2/upload/";

//            Map<String, String> paramMap = getQueryMap(param);
//            String fileModifyName = Base64.encodeToString(paramMap.get("fileModifyName").getBytes("UTF-8"), Base64.DEFAULT);
//            String fileOriginName = Base64.encodeToString(paramMap.get("fileOriginName").getBytes("UTF-8"), Base64.DEFAULT);
//            String fileFolder = Base64.encodeToString(paramMap.get("fileFolder").getBytes("UTF-8"), Base64.DEFAULT);
//
//            url = "http://"+Config.SERVER_DOMAIN+url+"?fileModifyName="+fileModifyName+"&fileOriginName="+fileOriginName+"&fileFolder="+fileFolder;

            url = "http://"+Config.SERVER_DOMAIN+url+"?"+param;

//            String password = Utils.getSharedPreferences(context, "password");
//            String userId = Utils.getSharedPreferences(context, "userId");

//            String password = "lee35";
//            String userId = "thflks";

//            url = "http://"+Config.SERVER_DOMAIN+url+"?"+ URLEncoder.encode(param, "UTF-8");

//            url = "http://mportal.cau.ac.kr"+url+"?"+ URLEncoder.encode(testParam, "UTF-8");

//            if(password != null && !password.equals("")) { // sso 로그인처리
//                url = "https://sso2.cau.ac.kr/SSO/AuthWeb/Logon.aspx?ssosite=mportal.cau.ac.kr&credType=BASIC&userID=" + userId + "&password=" + password + "&retURL=" + url;
//            }

            Log.d("caulog", "full url : "+url);

//            final WebView webView = (WebView) ((Activity)context).findViewById(R.id.webView1);
//            webView.post(new Runnable() {
//                @Override
//                public void run() {
//                    String add = "http://"+Config.SERVER_DOMAIN+url;
//                    webView.postUrl(add, testParam.getBytes());
//                }
//            });

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    String add = "http://"+Config.SERVER_DOMAIN+url;
////                    ((ContentActivity)context).webview.postUrl(add, testParam.getBytes());
//
//                    WebView webView = (WebView) ((Activity)context).findViewById(R.id.webView1);
//                    webView.postUrl(add, testParam.getBytes());
//
//                }
//            });

            Uri address = Uri.parse(url);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(address);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @JavascriptInterface
    public void requestRegi(String userNo, String userId, String encPass, String autoLogin) {

        Utils.setSharedPreferences(context, "userNo", userNo); // 학번
        Utils.setSharedPreferences(context, "userId", userId); // 아이디
        Utils.setSharedPreferences(context, "encPass", encPass); // 비밀번호
//        Utils.setSharedPreferences(context, "autoLogin", autoLogin); // 자동로그인값은 항상갱신

        String changePushToken = Utils.getSharedPreferences(context, "changePushToken");
        String isAddUser = Utils.getSharedPreferences(context, "isAddUser");
        String AddUserId = Utils.getSharedPreferences(context, "AddUserId");

        if(!userId.equals(AddUserId)){ // 로그인사용자가 변경될경우 다시 등록
            Log.d("caulog", "Change User !");
            isAddUser = "";
            changePushToken = "Y";
        }

//        Log.d("caulog", "autoLogin : "+autoLogin);
        Log.d("caulog", "userNo : "+userNo);
        Log.d("caulog", "userId : "+userId);
        Log.d("caulog", "encPass : "+encPass);
        Log.d("caulog", "isAddUser : "+isAddUser);
        Log.d("caulog", "AddUserId : "+AddUserId);
        Log.d("caulog", "changePushToken : "+changePushToken);

        if(changePushToken.equals("Y") || isAddUser.equals("")){ // 미등록

            String pushToken = Utils.getSharedPreferences(context, "pushToken");

            Log.d("caulog", "pushToken : "+pushToken);

            if(!pushToken.equals("")){ // 레지됨

                String phone = Utils.getPhone(context);

                //phone = "01020171121";

                Map<String, Object> data = new HashMap<String, Object>();
                data.put( "MemID", phone);
                data.put( "PhoneNo", phone);
                data.put( "Phone_Type", String.format("Android %s", Build.VERSION.RELEASE));
                data.put( "OID", "10020");

                JSONObject json = new JSONObject(data);
                String param = json.toString();
                //Log.d("caulog", "param : "+param);

                // 유니톡 사용자등록
                String result = Utils.excutePost(Config.UNITOCK_ADD_USER_URL, param, true);

                Log.d("caulog", "add user result : "+result);

                if(result != null && result.equals("\"SUCCESS\"")){

                    param = "oid=10020&phoneno="+phone+"&C2DM_AuthKey="+pushToken;
                    //Log.d("caulog", "param : "+param);

                    // 유니톡 레지등록
                    result = Utils.excutePost(Config.UNITOCK_REGI_URL, param, false);
                    Log.d("caulog", "add regi result : "+result);

                    if(result != null){

                        data = new HashMap<String, Object>();
                        data.put( "phone", phone);
                        data.put( "user_id", userNo);

                        json = new JSONObject(data);
                        param = json.toString();
                        //Log.d("caulog", "param : "+param);

                        // 포탈전화번호 등록
                        result = Utils.excutePost(Config.SERVICE_UPDATE_PHONE_URL, param, true);
                        Log.d("caulog", "result : "+result);

                        if(result.indexOf("success") != -1){
                            Utils.setSharedPreferences(context, "isAddUser", "Y");
                            Utils.setSharedPreferences(context, "changePushToken", "N");
                            Utils.setSharedPreferences(context, "AddUserId", userId);
                        }

                    }
                }

            }else{ // 레지안됨

            }

        }else{ // 등록

        }

    }


}
