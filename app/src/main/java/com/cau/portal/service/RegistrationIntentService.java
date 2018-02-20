package com.cau.portal.service;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.cau.portal.utils.Utils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by saltfactory on 6/8/15.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "cau";
    public static final String GCM_SENDERID = "161003222946"; // ASIS소스참조

    public RegistrationIntentService() {
        super(TAG);
    }

    /**
     * GCM을 위한 Instance ID의 토큰을 생성하여 가져온다.
     * @param intent
     */
    @SuppressLint("LongLogTag")
    @Override
    protected void onHandleIntent(Intent intent) {

        // GCM을 위한 Instance ID를 가져온다.
        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            synchronized (TAG) {
                // GCM 앱을 등록하고 획득한 설정파일인 google-services.json을 기반으로 SenderID를 자동으로 가져온다.
                //String default_senderId = getString(R.string.gcm_defaultSenderId);

                // GCM 기본 scope는 "GCM"이다.
                String scope = GoogleCloudMessaging.INSTANCE_ID_SCOPE;
                //String default_senderId = getString(R.string.gcm_defaultSenderId);
                // Instance ID에 해당하는 토큰을 생성하여 가져온다.
                token = instanceID.getToken(GCM_SENDERID, scope, null);

                Log.i(TAG, "GCM Registration Token: " + token);
                if(!Utils.getSharedPreferences(RegistrationIntentService.this,"pushToken").equals(token)){
                    Utils.setSharedPreferences(RegistrationIntentService.this,"pushToken", token);
                    Utils.setSharedPreferences(RegistrationIntentService.this,"changePushToken", "Y");
                }else{
                    Utils.setSharedPreferences(RegistrationIntentService.this,"changePushToken", "N");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}