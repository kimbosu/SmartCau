package com.cau.portal.activity;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cau.portal.R;
import com.cau.portal.service.ApiService;
import com.cau.portal.service.RegistrationIntentService;
import com.cau.portal.service.WebAppService;
import com.cau.portal.utils.Config;
import com.cau.portal.utils.NetWorkUtils;
import com.cau.portal.utils.Utils;

import java.net.URLEncoder;
import java.util.ArrayList;

public class ContentActivity extends AppCompatActivity {

    public WebView webview;
    private ProgressBar progress;
    private TextView titleTxt;
    private View endPopup;
    private View backImg;
    private ImageView prevBtn;
    private ImageView nextBtn;
    private ImageView refBtn;
    private ImageView homeBtn;

    private ValueCallback<Uri> filePathCallbackNormal;
    private ValueCallback<Uri[]> filePathCallbackLollipop;
    private final static int FILECHOOSER_NORMAL_REQ_CODE = 1;
    private final static int FILECHOOSER_LOLLIPOP_REQ_CODE = 2;

    private Handler mHandler;
    private Runnable mRunnable;

    private View updatePopup;

    private String webURL = Config.SERVICE_MAIN_URL;

    private final Handler handler = new Handler(){

        @Override
        public void handleMessage(Message msg){

            switch (msg.what){
                case 0:
                    hideProgress();
                    break;
                case 1:
                    showProgress();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        updatePopup = (View)findViewById(R.id.updatePopup);

        if(NetWorkUtils.isNetworkConnection(this)){

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) { // 마쉬멜로 버전보다 위면 권한을요청한다.
                moveStart();
            }else{
                checkPermission();
            }

        }else{
            showAlert("알림", "인터넷이 연결되어 있지 않습니다.\n스마트 중앙을 종료합니다.");
        }

    }

    public void naviInit(){
        prevBtn =(ImageView)findViewById(R.id.prevBtn);
        nextBtn =(ImageView)findViewById(R.id.nextBtn);
        refBtn =(ImageView)findViewById(R.id.refBtn);
        homeBtn =(ImageView)findViewById(R.id.homeBtn);

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goPrev();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        refBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goRefresh();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
    }

    /**
     * 컨텐츠초기화
     */
    public void initContent(boolean isPushBoxGo){

        if(isPushBoxGo){
            webURL = Config.PUSH_BOX_URL;
        }

        if(NetWorkUtils.isNetworkConnection(this)){ // 네트워크연결

            progress = (ProgressBar) findViewById(R.id.web_progress);

            webview = (WebView) findViewById(R.id.webView1);
//            webview.clearCache(true);
//            webview.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//            webview.clearHistory();
//            webview.clearView();
            // 세션클리어
            CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.removeSessionCookie();
            cookieSyncManager.sync();
//            ContentActivity.this.deleteDatabase("webview.db");
//            ContentActivity.this.deleteDatabase("webviewCache.db");

            // 프로그래스바처리
            webview.setWebViewClient(new WebViewClient() {

//                @Override
//                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                    handler.proceed(); // SSL 에러가 발생해도 계속 진행!, OZ뷰어 SSL오류 대응코드
//                }

                @Override
                public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                    String message = "신뢰 할수 없는 인증서 사이트 접근입니다.\n계속하시겠습니까?";
                    Log.d("caulog", "onReceivedSslError : "+error.getPrimaryError());
                    switch (error.getPrimaryError())
                    {
                        case SslError.SSL_UNTRUSTED:
                            message = "이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n계속하시겠습니까?"; break;
                        case SslError.SSL_EXPIRED:
                            message = "이 사이트의 보안 인증서가 만료되었습니다.\n계속하시겠습니까?"; break;
                        case SslError.SSL_IDMISMATCH:
                            message = "이 사이트의 보안 인증서가 ID 일치하지 않습니다.\n계속하시겠습니까?"; break;
                        case SslError.SSL_NOTYETVALID:
                            message = "이 사이트의 보안 인증서가 유효하지 않습니다.\n계속하시겠습니까?"; break;
                    }

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ContentActivity.this);
                    builder.setMessage(message);
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.proceed();
                            hideProgress();
                        }
                    });
                    builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            handler.cancel();
                            hideProgress();
                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progress.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    backImg.setVisibility(View.GONE);
                    progress.setVisibility(View.GONE);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    Log.d("caulog", "@ start Url : "+url);

                    Uri uri = Uri.parse(url);
                    // 새창처리
                    if (url.indexOf("_blank") != -1){

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(uri);
                        startActivity(intent);

                        return true;
                    } else if (url.startsWith("tel:")) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                        view.reload();
                        return true;
                    } else if (url.startsWith("mailto:")) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } else if (url.startsWith("intent:")) {
                        try {

                            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String pac = intent.getPackage();
                            intent = getPackageManager().getLaunchIntentForPackage(pac);

                            Intent existPackage = getPackageManager().getLaunchIntentForPackage(pac);

                            if (existPackage != null) {
                                startActivity(intent);
                            } else {
                                Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                                marketIntent.setData(Uri.parse("market://details?id=" + pac));
                                startActivity(marketIntent);
                            }
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else if (url.startsWith("market:")) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            Activity host = (Activity) view.getContext();
                            host.startActivity(intent);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            // Google Play app is not installed, you may want to open the app store link
                            view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery());
                            return true;
                        }
                    } else {
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                }

            });

            // 웹뷰 파일업로드 대응
            webview.setWebChromeClient(new WebChromeClient() {

                // a tag 새창처리
                @Override public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg){
                    WebView newWebView = new WebView(ContentActivity.this);
                    WebView.WebViewTransport transport = (WebView.WebViewTransport)resultMsg.obj;
                    transport.setWebView(newWebView);
                    resultMsg.sendToTarget();

                    newWebView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            try {

                                Log.d("caulog", "@ start22 Url : "+url);

                                Uri uri = Uri.parse(url);

                                String password = Utils.getSharedPreferences(ContentActivity.this, "password");
                                String userId = Utils.getSharedPreferences(ContentActivity.this, "userId");

                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                if(password != null && !password.equals("")){ // sso 로그인처리

                                    String ssoUrl = "https://sso2.cau.ac.kr/SSO/AuthWeb/Logon.aspx?ssosite="+Config.SERVER_DOMAIN+"&credType=BASIC&userID="+userId+"&password="+password+"&retURL="+URLEncoder.encode(uri.toString(), "UTF-8");

                                    Uri req_url = Uri.parse(ssoUrl);
                                    Log.d("caulog", "return url : "+req_url);
                                    intent.setData(req_url);
                                }else{
                                    Log.d("caulog", "is not login!");
                                    intent.setData(uri);
                                }

                                startActivity(intent);

                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            return true;
                        }
                    });

                    return true;
                }

                // For Android < 3.0
                public void openFileChooser( ValueCallback<Uri> uploadMsg) {
                    Log.d("MainActivity", "3.0 <");
                    openFileChooser(uploadMsg, "");
                }
                // For Android 3.0+
                public void openFileChooser( ValueCallback<Uri> uploadMsg, String acceptType) {
                    Log.d("MainActivity", "3.0+");
                    filePathCallbackNormal = uploadMsg;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    // i.setType("video/*");
                    startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_NORMAL_REQ_CODE);
                }
                // For Android 4.1+
                public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                    Log.d("MainActivity", "4.1+");
                    openFileChooser(uploadMsg, acceptType);
                }

                // For Android 5.0+
                public boolean onShowFileChooser(
                        WebView webView, ValueCallback<Uri[]> filePathCallback,
                        WebChromeClient.FileChooserParams fileChooserParams) {
                    Log.d("MainActivity", "5.0+");
                    if (filePathCallbackLollipop != null) {
                        filePathCallbackLollipop.onReceiveValue(null);
                        filePathCallbackLollipop = null;
                    }
                    filePathCallbackLollipop = filePathCallback;
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.addCategory(Intent.CATEGORY_OPENABLE);
                    i.setType("image/*");
                    // i.setType("video/*");
                    startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_LOLLIPOP_REQ_CODE);

                    return true;
                }
            });

            webview.setDownloadListener(new DownloadListener() {

                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                    Log.d("caulog", "down : "+url);
                    try {
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        request.setMimeType(mimeType);
                        request.addRequestHeader("User-Agent", userAgent);
                        request.setDescription("Downloading file");
                        String fileName = contentDisposition.replace("inline; filename=", "");
                        fileName = fileName.replaceAll("\"", "");
                        fileName = fileName.replaceAll(";", "");
                        fileName = fileName.replaceAll("attachment filename=", "");
                        request.setTitle(fileName);
                        request.allowScanningByMediaScanner();
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "파일다운로드 중 입니다.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "파일다운로드 중 오류가 발생하였습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });

            WebSettings set = webview.getSettings();
            set.setUserAgentString(set.getUserAgentString()+""+getString(R.string.user_agent_suffix));
            set.setJavaScriptEnabled(true);
            set.setSupportMultipleWindows(true);
            set.setJavaScriptCanOpenWindowsAutomatically(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 롤리팝, HTTPS에서 외부 이미지 나올수있게
                set.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }

            webview.addJavascriptInterface(new WebAppService(ContentActivity.this, handler), "Android");

            String autoLogin = Utils.getSharedPreferences(ContentActivity.this, "autoLogin");
            String password = Utils.getSharedPreferences(ContentActivity.this, "password");
            String userId = Utils.getSharedPreferences(ContentActivity.this, "userId");

            Log.d("caulog", "Auto Login flag : "+autoLogin);
            Log.d("caulog", "Auto Login password : "+password);

            if(autoLogin != null && autoLogin.equals("Y") && password != null && !password.equals("")){ // 자동로그인

                try {
                    Log.d("caulog", "Auto Login!");

                    String reUrl = Config.SERVICE_MAIN_URL;

                    if(isPushBoxGo){ // 푸쉬메시지 선택에 의한 접근이라면 바로 푸쉬함으로 이동시키도록 파라메타를 전달한다.
                        reUrl = Config.SERVICE_URL+"myPage/pushBox/pushBox.do";
                    }

                    String url = "https://sso2.cau.ac.kr/SSO/AuthWeb/Logon.aspx?ssosite="+Config.SERVER_DOMAIN+"&credType=BASIC&userID="+userId+"&password="+password+"&retURL="+reUrl;
                    webview.loadUrl(url);

                } catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(ContentActivity.this, "자동로그인실패!", Toast.LENGTH_SHORT).show();
                    webview.loadUrl(webURL);
                }

        }else{ // 일반
            webview.loadUrl(webURL);
        }

        }else{ // 네트워크연결안됨
        showAlert("알림", "인터넷이 연결되어 있지 않습니다.\n스마트 중앙을 종료하시겠습니까?");
        }

    }

    public void showProgress(){
        if(progress != null)
            progress.setVisibility(View.VISIBLE);
    }

    public void hideProgress(){
        backImg.setVisibility(View.GONE);
        if(progress != null)
            progress.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
                if (filePathCallbackNormal == null) return ;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;
            } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
                if (filePathCallbackLollipop == null) return ;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 롤리팝
                    filePathCallbackLollipop.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                }
                filePathCallbackLollipop = null;
            }
        } else {
            if (filePathCallbackLollipop != null) {
                filePathCallbackLollipop.onReceiveValue(null);
                filePathCallbackLollipop = null;
            }
        }

    }

    @Override
    public void onBackPressed() {
        //showAlert("알림", "종료하시겠습니까?");
    }

    @Override
    protected void onResume() {
//        if(webview != null)
//            webview.loadUrl(webURL);
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return goPrev();
        }

        return super.onKeyDown(keyCode, event);
    }

    public boolean goPrev(){
        if(webview.canGoBack()){
            webview.goBack();
            return true;
        }else{
            endPopup.setVisibility(View.VISIBLE);
            return false;
        }
    }

    public boolean goNext(){
        if(webview.canGoForward()){
            webview.goForward();
            return true;
        }else{
            return false;
        }
    }

    public void goRefresh(){
        webview.reload();
    }

    public void goHome(){
        webview.loadUrl(Config.SERVICE_MAIN_URL);
    }

    public void moveStart(){

        // PUSH항목 제거
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

        Intent intent = getIntent();
        boolean isPushBoxGo = intent.getBooleanExtra("isPushBoxGo", false);

        Log.d("caulog", "isPushBoxGo : "+isPushBoxGo);
        initContent(isPushBoxGo);

        backImg = (View)findViewById(R.id.backImg);

        // 종료팝업설정
        endPopup = (View)findViewById(R.id.endPopup);

        Button cancelBtn = (Button) findViewById(R.id.popup_btn_cancel);
        Button okBtn = (Button) findViewById(R.id.popup_btn_ok);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPopup.setVisibility(View.GONE);
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ContentActivity.this, "앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        naviInit();

        getInstanceIdToken();

        new ContentActivity.VersionTask().execute(); // 버전체크요청

    }

    public void movePageProc(){

//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                backImg.setVisibility(View.GONE);
//            }
//        }, 1000);

    }

    private class VersionTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {

            final String serverVersion = ApiService.getServerVersion(ContentActivity.this); // 서버버전
            String version = "0";

            try {
                PackageInfo packageInfo = ContentActivity.this.getPackageManager().getPackageInfo(ContentActivity.this.getPackageName(), 0);
                version = packageInfo.versionName; // 현재버전
                Log.d("caulog", "CURRENT VERSION : "+version);

                final String versionText = "현재 : Ver."+Float.parseFloat(version)+"   ,   최신 : Ver."+Float.parseFloat(serverVersion);

                if(Float.parseFloat(serverVersion) > Float.parseFloat(version)){ // 업데이트

                    Handler mHandler = new Handler(Looper.getMainLooper());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            updatePopup.setVisibility(View.VISIBLE);

                            TextView versionTitle = (TextView) findViewById(R.id.version_title);
                            versionTitle.setText(versionText);

                            Button cancelBtn = (Button) findViewById(R.id.up_popup_btn_cancel);
                            Button okBtn = (Button) findViewById(R.id.up_popup_btn_ok);

                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    updatePopup.setVisibility(View.GONE);
                                    movePageProc();
                                }
                            });

                            okBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // TODO 마켓이동
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse("market://details?id=" + ContentActivity.this.getPackageName()));
                                    startActivity(intent);
                                }
                            });

                        }
                    }, 0);

                }else{ // 진행
                    movePageProc();
                }

            } catch(Exception e) {
                e.printStackTrace();
                movePageProc();
            }

            return "";
        }

    }

    public void showAlert(String title, String msg){
        //다이얼로그 지정
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(ContentActivity.this);
        //다이얼로그의 내용을 설정합니다.
        alertdialog.setTitle(title);
        alertdialog.setMessage(msg);

        //확인 버튼
        alertdialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //확인 버튼이 눌렸을 때 토스트를 띄워줍니다.
                Toast.makeText(ContentActivity.this, "앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

//        //취소 버튼
//        alertdialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //취소 버튼이 눌렸을 때 토스트를 띄워줍니다.
//                Toast.makeText(ContentActivity.this, "취소", Toast.LENGTH_SHORT).show();
//            }
//        });

        AlertDialog alert = alertdialog.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        Log.i("cau", "onDstory()");
        if(mHandler != null)
            mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }


    private final static int MY_PERMISSIONS_REQUEST = 0xFF;

    private void checkPermission() { // 권한체크
        ArrayList<String> permissions = new ArrayList<>();

        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        addPermission(permissions, Manifest.permission.READ_EXTERNAL_STORAGE);
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissions.size() > 0) {
            String[] array = new String[permissions.size()];
            permissions.toArray(array);
            ActivityCompat.requestPermissions(this, array, MY_PERMISSIONS_REQUEST);
        }else{
            moveStart();
        }
    }

    private ArrayList<String> addPermission(ArrayList<String> permissionList, String permission){
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // 사용자가 임의로 권한을 취소시킨 경우
                // 권한 재요청
                permissionList.add(permission);
            } else {
                // 권한 요청 (최초 요청)
                permissionList.add(permission);
            }
        }
        return permissionList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    moveStart();
                } else {
                    Toast.makeText(this, "권한사용에 동의해주셔야 이용이 가능합니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    /**
     * Instance ID를 이용하여 디바이스 토큰을 가져오는 RegistrationIntentService를 실행한다.
     */
    public void getInstanceIdToken() {
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);
    }


}
