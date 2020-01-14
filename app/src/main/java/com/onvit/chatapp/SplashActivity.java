package com.onvit.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onvit.chatapp.model.KCHA;
import com.onvit.chatapp.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jxl.Sheet;
import jxl.Workbook;

public class SplashActivity extends AppCompatActivity {

    private final long VERSION_CODE = 12; // gradle버전이랑 맞춰야됨. firebase remoteconfig도 같이 맞춰야됨.

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseAuth firebaseAuth;
    private ValueEventListener valueEventListener;
    private String text = null;
    private Uri uri = null;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getIntent().addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        firebaseAuth = FirebaseAuth.getInstance();
//        firebaseAuth.signOut();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0) // 한시간에 최대 한번 요청할 수 있음. 한시간의 캐싱타임을 가짐.
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);


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
                        versionCheck();
                    }
                });


    }

    void versionCheck() {
        long versionCode = mFirebaseRemoteConfig.getLong("version_code"); // 이거 true로 보내면 서버점검중이라고 띄울 수 있음.
        String updateMessage = mFirebaseRemoteConfig.getString("update_message");
        if (versionCode != VERSION_CODE) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(updateMessage).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.onvit.chatapp")));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.onvit.chatapp")));
                    } finally {
                        finish();
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();
        } else {
            initSplash();

        }
    }

    private void initSplash() {
        //회원들 정보 넣음.
//                insertExel();
        if (PreferenceManager.getString(SplashActivity.this, "name") == null || PreferenceManager.getString(SplashActivity.this, "name").equals("")
                || FirebaseAuth.getInstance().getCurrentUser() == null) {
            firebaseAuth.signOut();
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            final Date date = new Date();
            long twoM = (24L * 60 * 60 * 1000 * 60);
            long oldDate = date.getTime() - twoM;
            //맨 처음채팅부터 200개씩 가져와서 두달지난거 삭제함.
            String chatName = "normalChat";
            deleteChat(oldDate, chatName);
            chatName = "officerChat";
            deleteChat(oldDate, chatName);

            valueEventListener = new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 가입한 유저들의 정보를 가지고옴.
                    User user = null;
                    String key = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(snapshot.getValue(User.class).getUid())) {
                            user = snapshot.getValue(User.class);
                            key = snapshot.getKey();
                        }
                        if(snapshot.getValue(User.class).getUserName()==null){
                            Map<String, Object> map = new HashMap<>();
                            map.put("Users/"+snapshot.getKey(), null);
                            map.put("groupChat/normalChat/userInfo/"+snapshot.getKey(), null);
                            map.put("groupChat/officerChat/userInfo/"+snapshot.getKey(), null);
                            map.put("groupChat/normalChat/users/"+snapshot.getKey(), null);
                            map.put("groupChat/officerChat/users/"+snapshot.getKey(), null);
                            map.put("lastChat/normalChat/existUsers/"+snapshot.getKey(), null);
                            map.put("lastChat/officerChat/existUsers/"+snapshot.getKey(), null);
                            map.put("lastChat/normalChat/users/"+snapshot.getKey(), null);
                            map.put("lastChat/officerChat/users/"+snapshot.getKey(), null);
                            FirebaseDatabase.getInstance().getReference().updateChildren(map);
                        }

                    }

                    if (user == null) {
                        firebaseAuth.signOut();
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = getIntent();
                        String action = intent.getAction();
                        String type = intent.getType();
                        if (Intent.ACTION_SEND.equals(action) && type != null) {
                            if ("text/plain".equals(type)) {
                                text = intent.getStringExtra(Intent.EXTRA_TEXT);
                                Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                                intent1.putExtra("text", text);
                                intent1.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1);
                                finish();
                            } else if (type.startsWith("image/")) {
                                Intent intent1 = new Intent(SplashActivity.this, MainActivity.class);
                                uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                                final Uri convertUri = getConvertUri(uri);
                                Log.d("이미지 공유", uri+"");
                                Log.d("이미지 공유", convertUri+"");
                                if (convertUri != null) {
                                    intent1.putExtra("shareUri", convertUri);
                                    intent1.putExtra("filePath", filePath);
                                }
                                intent1.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent1);
                                finish();
                            }
                        } else if(getIntent().getStringExtra("tag")!=null){
                            Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
                            intent2.putExtra("tag",getIntent().getStringExtra("tag"));
                            intent2.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent2);
                            finish();

                        }else{
                            Intent intent2 = new Intent(SplashActivity.this, MainActivity.class);
                            intent2.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent2);
                            finish();
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(valueEventListener);
        }
    }

    private void insertExel() {
        try {
            InputStream is = getBaseContext().getResources().getAssets().open("대한지역병원협의회 회원명단.xls");
            Workbook wb = Workbook.getWorkbook(is);

            Map<String, Object> list = new HashMap<>();
            Sheet sheet = wb.getSheet(0);   // 시트 불러오기
            if (sheet != null) {
                int colTotal = sheet.getColumns();    // 전체 컬럼
                int rowIndexStart = 2;                  // row 인덱스 시작
                int rowTotal = sheet.getRows();
                KCHA sb;
                for (int row = rowIndexStart; row < rowTotal; row++) {
                    sb = new KCHA();
                    for (int col = 1; col < colTotal - 3; col++) {
                        String contents = sheet.getCell(col, row).getContents();
                        switch (col) {
                            case 1:
                                sb.setName(contents);
                                break;
                            case 2:
                                sb.setHospital(contents);
                                break;
                            case 3:
                                sb.setPhone(contents);
                                break;
                            case 4:
                                sb.setMajor(contents);
                                break;
                            case 5:
                                sb.setAddress(contents);
                                break;
                            case 6:
                                sb.setEmail(contents);
                                break;
                            case 7:
                                sb.setTel(contents);
                                break;
                            case 8:
                                sb.setFax(contents);
                                break;
                            case 9:
                                sb.setmNo(contents);
                                break;
                            case 10:
                                sb.setGrade(contents);
                                break;
                        }
                    }
                    list.put(sb.getName(), sb);
                }
                FirebaseDatabase.getInstance().getReference().child("KCHA").updateChildren(list);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void deleteChat(final long date, final String chatName) {
        FirebaseDatabase.getInstance().getReference().child("groupChat").child(chatName).child("comments")
                .orderByChild("timestamp")
                .endAt(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Object> map = new HashMap<>();
                Log.d("삭제", dataSnapshot.getChildrenCount() + "");
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    map.put(item.getKey(), null);
                }
                FirebaseDatabase.getInstance().getReference().child("groupChat").child(chatName).child("comments").updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Uri getConvertUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            File file = getCacheDir();
            String fileName = System.currentTimeMillis() + ".jpeg";
            File tempFile = new File(file, fileName);
            OutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            getFilePath(tempFile.getAbsolutePath());
            return FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", tempFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void getFilePath(String absolutePath) {
        filePath = absolutePath;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").removeEventListener(valueEventListener);
        }
    }


}
