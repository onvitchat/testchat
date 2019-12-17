package com.onvit.chatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.onvit.chatapp.chat.SelectGroupChatActivity;
import com.onvit.chatapp.fragment.ChatFragment;
import com.onvit.chatapp.fragment.NoticeFragment;
import com.onvit.chatapp.fragment.PeopleFragment;
import com.onvit.chatapp.fragment.ShoppingFragment;
import com.onvit.chatapp.model.ADlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final static int PERMISSION_REQUEST_CODE = 1000;
    private FirebaseAuth firebaseAuth;
    private String text = null;
    private Uri uri = null;
    private PeopleFragment peopleFragment;
    private ChatFragment chatFragment;
    private NoticeFragment noticeFragment;
    private ShoppingFragment shoppingFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        //유저없으면 로그인 페이지로
        if (firebaseAuth.getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            firebaseAuth.signOut();
            startActivity(intent);
            finish();
        }
        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        peopleFragment = new PeopleFragment();
        chatFragment = new ChatFragment();
        noticeFragment = new NoticeFragment();
        shoppingFragment = new ShoppingFragment();
        passPushTokenToServer();
        BottomNavigationView bottomNavigationView = findViewById(R.id.mainActivity_bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, noticeFragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_notice:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, noticeFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, peopleFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, chatFragment).commitAllowingStateLoss();
                        return true;
                    case R.id.action_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, shoppingFragment).commitAllowingStateLoss();
                        return true;

                }
                return false;
            }
        });
        if (getIntent().getStringExtra("text") != null || getIntent().getParcelableExtra("shareUri") != null) {
            text = getIntent().getStringExtra("text");
            uri = getIntent().getParcelableExtra("shareUri");
            Intent intent1 = new Intent(MainActivity.this, SelectGroupChatActivity.class);
            intent1.putExtra("text", text);
            intent1.putExtra("shareUri", uri);
            startActivity(intent1);
        }

        requestPermission();


    }




    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            ArrayList<String> arrayPermission = new ArrayList<>();

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                arrayPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                arrayPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (arrayPermission.size() > 0) {
                String strArray[] = new String[arrayPermission.size()];
                strArray = arrayPermission.toArray(strArray);
                ActivityCompat.requestPermissions(this, strArray, PERMISSION_REQUEST_CODE);
            } else {
                // Initialize 코드
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length < 1) {
                    Toast.makeText(this, "권한을 받아오는데 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "권한을 거부하였습니다.", Toast.LENGTH_SHORT).show();

                        return;
                    }
                }

                Toast.makeText(this, "권한을 허용하였습니다.", Toast.LENGTH_SHORT).show();
                // Initialize 코드
            }
            break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getStringExtra("fcm") != null) {
            Log.d("들어옴?", "");
            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, chatFragment).commitAllowingStateLoss();
        }
    }

    void passPushTokenToServer() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                String token = instanceIdResult.getToken();
                Map<String, Object> map = new HashMap<>();
                map.put("pushToken", token);

                FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
                FirebaseDatabase.getInstance().getReference().child("groupChat").child("normalChat").child("userInfo").child(uid).updateChildren(map);
                FirebaseDatabase.getInstance().getReference().child("groupChat").child("officerChat").child("userInfo").child(uid).updateChildren(map);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mail_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.logout) {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Map<String, Object> map = new HashMap<>();
            map.put("pushToken", "");
            FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
            FirebaseDatabase.getInstance().getReference().child("groupChat").child("normalChat").child("userInfo").child(uid).updateChildren(map);
            FirebaseDatabase.getInstance().getReference().child("groupChat").child("officerChat").child("userInfo").child(uid).updateChildren(map);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("logOut", "logOut");
            PreferenceManager.clear(this);
            startActivity(intent);
            finish();
        }

        return true;
    }
}
