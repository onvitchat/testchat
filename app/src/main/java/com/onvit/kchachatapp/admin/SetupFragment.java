package com.onvit.kchachatapp.admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;
import com.onvit.kchachatapp.LoginActivity;
import com.onvit.kchachatapp.MainActivity;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.util.PreferenceManager;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;

import java.util.HashMap;
import java.util.Map;

public class SetupFragment extends Fragment implements View.OnClickListener {
    private AppCompatActivity activity;

    public SetupFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup, container, false);
        Toolbar chatToolbar = view.findViewById(R.id.chat_toolbar);
        activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(chatToolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("설정");
            }
        }
        TextView logout = view.findViewById(R.id.logout);
        TextView admin = view.findViewById(R.id.admin);
        final Switch notify = view.findViewById(R.id.notify);

        logout.setOnClickListener(this);
        admin.setOnClickListener(this);

        notify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).getInt("vibrate", 0) == 0) {
                    activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).edit().putInt("vibrate", 1).apply();
                    notify.setChecked(false);
                    Utiles.customToast(activity, "앱의 알림이 해제되었습니다.").show();
                } else {
                    activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).edit().putInt("vibrate", 0).apply();
                    notify.setChecked(true);
                    Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(700);
                    Utiles.customToast(activity, "앱의 알림이 설정되었습니다.").show();
                }

            }
        });

        if (activity.getSharedPreferences(activity.getPackageName(), Context.MODE_PRIVATE).getInt("vibrate", 0) == 0) {
            notify.setChecked(true);
        } else {
            notify.setChecked(false);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        //중복클릭방지
        if(Utiles.blockDoubleClick()){
            return;
        }
        if (v.getId() == R.id.logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("대한지역병원협의회");
            builder.setMessage("로그아웃을 하시겠습니까?");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String uid = UserMap.getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("pushToken", "");
                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid).updateChildren(map);
                    NotificationManagerCompat.from(activity).cancelAll();
                    UserMap.clearApp();
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra("logOut", "logOut");
                    PreferenceManager.clear(activity);
                    startActivity(intent);
                    activity.finish();
                }
            }).setNegativeButton("취소", null);
            AlertDialog a = builder.create();
            a.show();
        }
    }
}
