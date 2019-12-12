package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.chatapp.chat.SelectGroupChatActivity;
import com.example.chatapp.fragment.NoticeFragment;
import com.example.chatapp.fragment.ShoppingFragment;
import com.example.chatapp.fragment.ChatFragment;
import com.example.chatapp.fragment.PeopleFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
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
        if(firebaseAuth.getCurrentUser()==null){
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
                switch (menuItem.getItemId()){
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

        if(getIntent().getStringExtra("text")!=null || getIntent().getParcelableExtra("shareUri")!=null){
            text = getIntent().getStringExtra("text");
            uri = getIntent().getParcelableExtra("shareUri");
            Intent intent1 = new Intent(MainActivity.this, SelectGroupChatActivity.class);
            intent1.putExtra("text", text);
            intent1.putExtra("shareUri", uri);
            startActivity(intent1);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(getIntent().getStringExtra("fcm")!=null){
            Log.d("들어옴?", "");
            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, chatFragment).commitAllowingStateLoss();
        }
    }

    void passPushTokenToServer(){
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
