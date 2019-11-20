package com.example.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        linearLayout = findViewById(R.id.splashactivity_linearlayout);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG) // 디버그모드.
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.default_config);

        mFirebaseRemoteConfig.fetch(0) // 재요청까지 걸리는 시간? 디버그 모드라 0초로 해도됨. 디버그 모드가 아니면 시간에 따른 횟수제한이 있음. 나중에 찾아보기.
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                        }
                        displayMessage();
                    }
                });
    }

    void displayMessage() {
        String loginBacground = mFirebaseRemoteConfig.getString("login_background");
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps");
        String splashMessage = mFirebaseRemoteConfig.getString("splash_message");
        linearLayout.setBackgroundColor(Color.parseColor(loginBacground));
        if (caps) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(splashMessage).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            builder.create().show();
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
    }

}
