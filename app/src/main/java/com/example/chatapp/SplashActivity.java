package com.example.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatapp.chat.SelectGroupChatActivity;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout linearLayout;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener valueEventListener;
    private List<User> userList;
    private List<User> logOutList;
    private String text = null;
    private Uri uri = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setStatusBarColor(Color.parseColor("#050099"));
        linearLayout = findViewById(R.id.splashactivity_linearlayout);
        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.signOut();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        mFirebaseRemoteConfig.setDefaultsAsync(R.xml.default_config);


//        mFirebaseRemoteConfig.fetch(0) // 재요청까지 걸리는 시간? 디버그 모드라 0초로 해도됨. 디버그 모드가 아니면 시간에 따른 횟수제한이 있음. 나중에 찾아보기.
//                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            // Once the config is successfully fetched it must be activated before newly fetched
//                            // values are returned.
//                            mFirebaseRemoteConfig.activateFetched();
//                        } else {
//                        }
//                        displayMessage();
//                    }
//                });
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            boolean updated = task.getResult();
                            Log.d("원격", "Config params updated: " + updated);


                        } else {
                            Toast.makeText(SplashActivity.this, "Fetch failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayMessage();
                    }
                });
        if(firebaseAuth.getCurrentUser()==null){
            firebaseAuth.signOut();
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            userList = new ArrayList<>();
            valueEventListener = new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 가입한 유저들의 정보를 가지고옴.

                    userList.clear();
                    User user = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(snapshot.getValue(User.class).getUid())) {
                            user = snapshot.getValue(User.class);
                            continue;
                        }

                    }

                    if(user==null){
                        firebaseAuth.signOut();
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    Intent intent = getIntent();
                    String action = intent.getAction();
                    String type = intent.getType();
                    if (Intent.ACTION_SEND.equals(action) && type != null) {
                        if ("text/plain".equals(type)) {
                            text = intent.getStringExtra(Intent.EXTRA_TEXT);
                            Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                            intent1.putExtra("text", text);
//                            intent1.putParcelableArrayListExtra("userList", (ArrayList<? extends Parcelable>) userList);
                            intent1.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                            finish();

                            Log.d("공유", text);
                        } else if (type.startsWith("image/")) {
                            Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                            uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                            final Uri convertUri = getConvertUri(uri);
                            if (convertUri != null) {
                                intent1.putExtra("shareUri", convertUri);
                            }
//                            intent1.putParcelableArrayListExtra("userList", (ArrayList<? extends Parcelable>) userList);
                            intent1.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent1);
                            finish();
                            Log.d("공유", uri.toString());
                        }
                    }else{
                        Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
//                        intent2.putParcelableArrayListExtra("userList", (ArrayList<? extends Parcelable>) userList);
                        intent2.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent2);
                        finish();
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(valueEventListener);
        }
    }
    void displayMessage() {
        boolean caps = mFirebaseRemoteConfig.getBoolean("splash_message_caps"); // 이거 true로 보내면 서버점검중이라고 띄울 수 있음.
        String splashMessage = mFirebaseRemoteConfig.getString("splash_message");
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
//            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();

        }
    }
    private Uri getConvertUri(Uri uri) {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/KCHA", System.currentTimeMillis() + ".jpeg");
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                OutputStream outputStream = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                inputStream.close();
            }
            if (file.length() > 0) {
                return new Uri.Builder().scheme("file").path(file.getAbsolutePath()).build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(valueEventListener!=null){
            FirebaseDatabase.getInstance().getReference().child("Users").removeEventListener(valueEventListener);
        }
    }



}
