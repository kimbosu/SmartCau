package com.cau.portal.service;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.cau.portal.utils.Config;
import com.cau.portal.utils.Utils;

import java.util.Map;

/**
 * Created by BSKim on 2017-10-07.
 */

public class ApiService {

    public static String getServerVersion(Context context){

        String serverVersion = "0";

        try {
            String result = Utils.excutePost(Config.VERSION_URL, "", false);

            Log.d("caulog", "api result : "+result);

            Map<String, Object> resultMap = Utils.jsonToMap(result);

            serverVersion = (String)resultMap.get("result");

            Log.d("caulog", "SERVER VERSION : "+serverVersion);

        }catch (Exception e){
            e.printStackTrace();
            Utils.alert(context, "버전확인 중 시스템 오류가 발생하였습니다.");
        }

        return serverVersion;
    }


}
