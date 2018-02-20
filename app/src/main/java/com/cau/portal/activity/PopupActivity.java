package com.cau.portal.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.cau.portal.R;

public class PopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);

        aquireWakeLock();

        Button cancelBtn = (Button) findViewById(R.id.push_popup_btn_cancel);
        Button okBtn = (Button) findViewById(R.id.push_popup_btn_ok);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopupActivity.this, ContentActivity.class);
                intent.putExtra("isPushBoxGo", true);
                startActivity(intent);
                finish();
            }
        });

    }

    private void aquireWakeLock() {

        Window window = getWindow();

        window.setLayout( -1, -1 );
        window.addFlags( WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN );

        PowerManager pm = ( PowerManager ) getSystemService( Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, getPackageName() );

        wakeLock.acquire();
        wakeLock.release();
    }

}
