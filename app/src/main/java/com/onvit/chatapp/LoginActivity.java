package com.onvit.chatapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.onvit.chatapp.certification.CertificateActivity;
import com.onvit.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText id;
    private EditText password;
    private Button login;
    private Button signup;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private ValueEventListener valueEventListener;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        String logOut = getIntent().getStringExtra("logOut");
        if(logOut!=null && logOut.equals("logOut")){
            Log.d("로그아웃", "00");
            firebaseAuth.signOut();
        }



        id = findViewById(R.id.loginactivity_edittext_id);
        password = findViewById(R.id.loginactivity_edittext_password);
        login = findViewById(R.id.loginactivity_button_login);
        signup = findViewById(R.id.loginactivity_button_signup);



        password.setImeOptions(EditorInfo.IME_ACTION_DONE);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() { // 완료눌러도 회원가입기능되게~
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    loginEvent();
                }
                return false;
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginEvent();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        //로그인 인터페이스 리스너
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null){
                    //로그인
                    Log.d("로그아웃", "11");
                    valueEventListener = new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // 가입한 유저들의 정보를 가지고옴.
                            User user = null;
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(snapshot.getValue(User.class).getUid())) {
                                    user = snapshot.getValue(User.class);
                                    PreferenceManager.setString(LoginActivity.this, "name", user.getUserName());
                                    PreferenceManager.setString(LoginActivity.this, "hospital", user.getHospital());
                                    PreferenceManager.setString(LoginActivity.this, "phone", user.getTel());
                                    PreferenceManager.setString(LoginActivity.this, "uid", user.getUid());
                                    PreferenceManager.setString(LoginActivity.this, "grade", user.getGrade());
                                }
                            }
                            if(user != null){
                                Log.d("로그아웃", "22");
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }else{
                                Log.d("로그아웃", "33");
                                Toast.makeText(LoginActivity.this, "회원정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(valueEventListener);
                }else{

                }
            }
        };

//        getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
//        signup.setBackgroundColor(Color.parseColor("#1F50B5"));
//        login.setBackgroundColor(Color.parseColor("#1F50B5"));
    }

    void loginEvent() {
        if(id.getText().toString()==null || id.getText().toString().equals("") || password.getText().toString()==null || password.getText().toString().equals("")){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setView(R.layout.login);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        firebaseAuth.signInWithEmailAndPassword(id.getText().toString(), password.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            //로그인 실패한부분
                            Toast.makeText(LoginActivity.this, "이메일과 비밀번호를 정확하게 입력하세요.",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else{
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener!=null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(valueEventListener!=null){
            FirebaseDatabase.getInstance().getReference().child("Users").removeEventListener(valueEventListener);
        }
    }
}
