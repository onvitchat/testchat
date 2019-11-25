package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.chatapp.fragment.AccountFragment;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.mainActivity_bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, new PeopleFragment()).commitAllowingStateLoss();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.action_people:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, new PeopleFragment()).commitAllowingStateLoss();
                        return true;
                    case R.id.action_chat:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, new ChatFragment()).commitAllowingStateLoss();
                        return true;
                    case R.id.action_account:
                        getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, new AccountFragment()).commitAllowingStateLoss();
                        return true;

                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        passPushTokenToServer();
        if(getIntent().getStringExtra("MessageActivity")!=null && getIntent().getStringExtra("MessageActivity").equals("MessageActivity")){
            getSupportFragmentManager().beginTransaction().replace(R.id.mainActivity_fragmentLayout, new ChatFragment()).commitAllowingStateLoss();
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
            }
        });

    }
}
