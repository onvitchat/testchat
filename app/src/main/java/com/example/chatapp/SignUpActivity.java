package com.example.chatapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignUpActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1000;
    private EditText name;
    private EditText email;
    private EditText password;
    private Button signup;
    private TextInputLayout tName;
    private TextInputLayout tEmail;
    private TextInputLayout tPassword;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String signUpButton;
    private ImageView profileImageView;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        signUpButton = mFirebaseRemoteConfig.getString("login_button_color");
        getWindow().setStatusBarColor(Color.parseColor(signUpButton));

        imageUri = Uri.parse("android.resource://com.example.chatapp/" + R.drawable.baseline_face_black_48); // 회원가입 기본 이미지.
        profileImageView = findViewById(R.id.signupActivity_imageview_profile);
        profileImageView.setImageURI(imageUri);

        // 내 사진첩 열어서 사진 가지고 오는 부분.
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        name = findViewById(R.id.signupActivity_edittext_name);
        email = findViewById(R.id.signupActivity_edittext_email);
        password = findViewById(R.id.signupActivity_edittext_password);
        tName = findViewById(R.id.signupActivity_textInputLayout_name);
        tEmail = findViewById(R.id.signupActivity_textInputLayout_email);
        tPassword = findViewById(R.id.signupActivity_textInputLayout_password);
        tName.setErrorEnabled(true);
        tEmail.setErrorEnabled(true);
        tPassword.setErrorEnabled(true);

        signup = findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor(signUpButton));

        password.setImeOptions(EditorInfo.IME_ACTION_DONE);
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() { // 완료눌러도 회원가입기능되게~
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    signUp();
                }
                return false;
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();

            }
        });
    }

    private void signUp() {
        if (name.getText().toString().replace(" ", "").equals("") || name.getText().toString() == null) {
            tEmail.setError("");
            tPassword.setError("");
            tName.setError("이름을 입력하세요.");
            return;
        }
        if (email.getText().toString().replace(" ", "").equals("") || email.getText().toString() == null) {
            tName.setError("");
            tPassword.setError("");
            tEmail.setError("이메일을 입력하세요.");
            return;
        }
        if (password.getText().toString().replace(" ", "").equals("") || password.getText().toString() == null) {
            tEmail.setError("");
            tName.setError("");
            tPassword.setError("비밀번호를 입력하세요.");
            return;
        }
        if (password.getText().toString().length() < 6) {
            tEmail.setError("");
            tName.setError("");
            tPassword.setError("비밀번호는 6자리 이상이어야 합니다.");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
        builder.setView(R.layout.loding);
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()) // 비밀번호 6자리 이상으로 해야함. 안그러면 firebase에러뜸.
                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {

                    //fireabseAuth 생성이 성공하면~
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        final String uid = task.getResult().getUser().getUid();

                        //회원가입할때 이름이 여기에 담긴다고함.?뭔소리여
                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
                        task.getResult().getUser().updateProfile(userProfileChangeRequest);


                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);
                        UploadTask uploadTask = storageReference.putFile(imageUri); // firebaseStorage에 uid이름으로 프로필 사진 저장

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return storageReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            //storageReference 에 저장한 이미지 uri를 불러옴
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri taskResult = task.getResult();
                                    String imageUri = taskResult.toString();
                                    User user = new User();
                                    user.setUserName(name.getText().toString());
                                    user.setUserEmail(email.getText().toString());
                                    user.setUserPassword(password.getText().toString());
                                    user.setUserProfileImageUrl(imageUri);
                                    user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    FirebaseDatabase.getInstance().getReference().child("Users").child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialog.dismiss();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                            builder.setMessage("회원가입완료\n로그인 페이지로 이동합니다.").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(intent);
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.create().setCanceledOnTouchOutside(false);
                                            builder.create().show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                }).addOnCanceledListener(SignUpActivity.this, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Toast.makeText(SignUpActivity.this, "가입취소", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(SignUpActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    //사진첩에서 사진 받아오는 부분.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            profileImageView.setImageURI(data.getData()); // 회원가입에 있는 이미지뷰를 바꿈
            imageUri = data.getData();//이미지 경로 원본
        }
    }
}
