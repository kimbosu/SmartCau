package com.cau.portal.utils;

/**
 * Created by user on 2017-11-22.
 */

public class Config {

//    public static String SERVER_DOMAIN = "tportal.cau.ac.kr";
    public static String SERVER_DOMAIN = "mportal.cau.ac.kr";
//    public static String SERVER_DOMAIN = "192.168.0.99:8080";
//    public static String SERVER_DOMAIN = "192.168.0.131:8080";

    /**
     * 서비스 주소
     */
      public static String SERVICE_URL = "http://"+SERVER_DOMAIN+"/";
      /**
      * 서비스 메인주소
     */
    public static String SERVICE_MAIN_URL = SERVICE_URL+"main.do";
    /**
     * 전화번호갱신
     */
    public static String SERVICE_UPDATE_PHONE_URL = SERVICE_URL+"api/updatePhoneNumber.api";
    /**
     * 유니톡 사용자등록
     */
    public static String UNITOCK_ADD_USER_URL = "http://unitokrcv.itisn.net:5225/MessageDistributor/insert_member";
    /**
     * 유니톡 PUSH레지
     */
    public static String UNITOCK_REGI_URL =  "http://unitokrcv.itisn.net/setauth/";
    /**
     * SSO로그인URL
     */
    public static String SSO_LOGIN_URL =  SERVICE_URL+"common/auth/SSOlogin.do";
    /**
     * 푸쉬메시지함
     */
    public static String PUSH_BOX_URL =  SERVICE_URL+"myPage/pushBox/pushBox.do";
    /**
     * 버전확인
     */
    public static String VERSION_URL =  SERVICE_URL+"api/getAndroidVersion.api";
}
