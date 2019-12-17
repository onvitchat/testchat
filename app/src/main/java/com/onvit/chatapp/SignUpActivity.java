package com.onvit.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onvit.chatapp.model.NoticeList;
import com.onvit.chatapp.model.User;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1000;
    private EditText name;
    private EditText hospital;
    private EditText tel;
    private EditText email;
    private EditText password;
    private Button signup;
    private TextInputLayout tName;
    private TextInputLayout tHospital;
    private TextInputLayout tTel;
    private TextInputLayout tEmail;
    private TextInputLayout tPassword;
    private ImageView profileImageView;
    private Uri imageUri;
    private User user;
    private String filePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        getWindow().setStatusBarColor(Color.parseColor("#050099"));
        imageUri = null;
        profileImageView = findViewById(R.id.signupActivity_imageview_profile);

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
        tel = findViewById(R.id.signupActivity_edittext_tel);
        email = findViewById(R.id.signupActivity_edittext_email);
        password = findViewById(R.id.signupActivity_edittext_password);
        hospital = findViewById(R.id.signupActivity_edittext_hospital);

        tName = findViewById(R.id.signupActivity_textInputLayout_name);
        tHospital = findViewById(R.id.signupActivity_textInputLayout_hospital);
        tTel = findViewById(R.id.signupActivity_textInputLayout_tel);
        tEmail = findViewById(R.id.signupActivity_textInputLayout_email);
        tPassword = findViewById(R.id.signupActivity_textInputLayout_password);
        tName.setErrorEnabled(true);
        tHospital.setErrorEnabled(true);
        tTel.setErrorEnabled(true);
        tEmail.setErrorEnabled(true);
        tPassword.setErrorEnabled(true);
        signup = findViewById(R.id.signupActivity_button_signup);
        signup.setBackgroundColor(Color.parseColor("#050099"));
        password.setImeOptions(EditorInfo.IME_ACTION_DONE);


        if (getIntent().getParcelableExtra("modify") == null) {
//            final User user = getIntent().getParcelableExtra("user");
//            name.setText(user.getUserName());
//            tel.setText(user.getTel());
//            email.setText(user.getUserEmail());
//            hospital.setText(user.getHospital());
//            name.setFocusable(false);
//            name.setClickable(false);
//            tel.setFocusable(false);
//            tel.setClickable(false);
//            email.setFocusable(false);
//            email.setClickable(false);
//            hospital.setFocusable(false);
//            hospital.setClickable(false);
            signup.setText("회원가입");
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
        } else {
            signup.setText("프로필 수정");
            user = getIntent().getParcelableExtra("modify");
            if (user.getUserProfileImageUrl().equals("noImg")) {
                profileImageView.setImageResource(R.drawable.standard_profile);
            } else {
                imageUri = Uri.parse(user.getUserProfileImageUrl());
                Glide.with(SignUpActivity.this).load(imageUri).into(profileImageView);
            }
            name.setText(user.getUserName());
            tel.setText(user.getTel());
            hospital.setText(user.getHospital());
            email.setText(user.getUserEmail());
            email.setFocusable(false);
            email.setClickable(false);
            name.setFocusable(false);
            name.setClickable(false);
            tel.setFocusable(false);
            tel.setClickable(false);
            hospital.setFocusable(false);
            hospital.setClickable(false);
            tPassword.setVisibility(View.GONE);
            password.setText(user.getUserPassword());
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setView(R.layout.update);
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    dialog.show();
                    modify(dialog);

                }
            });
        }

    }

    private void modify(final AlertDialog dialog) {
        if (checkInfo()) {
            return;
        }

        if (imageUri != null && !imageUri.equals(Uri.parse(user.getUserProfileImageUrl()))) {
            Bitmap bitmap = resize(SignUpActivity.this, imageUri, 500);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation  = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Bitmap newBitmap = rotateBitmap(bitmap, orientation);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] bytes = baos.toByteArray();
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(user.getUid());
            UploadTask uploadTask = storageReference.putBytes(bytes); // firebaseStorage에 uid이름으로 프로필 사진 저장
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
                        modifyInfo(imageUri, dialog);

                    }
                }
            });
        } else {
            String imageUri = user.getUserProfileImageUrl();
            modifyInfo(imageUri, dialog);
        }

    }

    private void modifyInfo(String imageUri, final AlertDialog dialog) {
        Map<String, Object> map = new HashMap<>();
        final User modifyUser = new User();
        modifyUser.setHospital(hospital.getText().toString());
        modifyUser.setPushToken(user.getPushToken());
        modifyUser.setTel(tel.getText().toString());
        modifyUser.setUid(user.getUid());
        modifyUser.setUserEmail(user.getUserEmail());
        modifyUser.setUserName(name.getText().toString());
        modifyUser.setUserPassword(user.getUserPassword());
        modifyUser.setUserProfileImageUrl(imageUri);
        map.put(user.getUid(), modifyUser);
        FirebaseDatabase.getInstance().getReference().child("Users").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String, Object> map = new HashMap<>();
                //각각의 그룹채팅방에 유저 정보 / 접속여부를 넣음
                map.put("normalChat/userInfo/" + user.getUid(), modifyUser);
                map.put("officerChat/userInfo/" + user.getUid(), modifyUser);
                FirebaseDatabase.getInstance().getReference().child("groupChat").updateChildren(map);
                PreferenceManager.setString(SignUpActivity.this, "name", modifyUser.getUserName());
                PreferenceManager.setString(SignUpActivity.this, "hospital", modifyUser.getHospital());
                PreferenceManager.setString(SignUpActivity.this, "phone", modifyUser.getTel());
                PreferenceManager.setString(SignUpActivity.this, "uid", modifyUser.getUid());

                FirebaseDatabase.getInstance().getReference().child("Notice").orderByChild("uid").equalTo(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() == 0) {
                            dialog.dismiss();
                            finish();
                        } else {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                NoticeList notice = item.getValue(NoticeList.class);
                                notice.setName(PreferenceManager.getString(SignUpActivity.this, "name") + "(" + PreferenceManager.getString(SignUpActivity.this, "hospital") + ")");
                                Map<String, Object> map = new HashMap<>();
                                map.put(item.getKey(), notice);
                                FirebaseDatabase.getInstance().getReference().child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    private void signUp() {
        if (checkInfo()) {
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
                        if (task.getException() != null) {
                            dialog.dismiss();
                            Toast.makeText(SignUpActivity.this, task.getException().toString(), Toast.LENGTH_LONG).show();
                            return;
                        }
                        final String uid = task.getResult().getUser().getUid();
                        final User user = new User();
                        user.setUserName(name.getText().toString());
                        user.setHospital(hospital.getText().toString());
                        user.setTel(tel.getText().toString());
                        user.setUserEmail(email.getText().toString());
                        user.setUserPassword(password.getText().toString());
                        user.setPushToken("");
                        user.setUid(uid);

                        //회원가입한 후 이름을 저장함.
//                        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name.getText().toString()).build();
//                        task.getResult().getUser().updateProfile(userProfileChangeRequest);

                        if (imageUri != null) {
                            Bitmap bitmap = resize(SignUpActivity.this, imageUri, 500);
                            ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(filePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            int orientation  = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                            Bitmap newBitmap = rotateBitmap(bitmap, orientation);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            final byte[] bytes = baos.toByteArray();
                            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);
                            UploadTask uploadTask = storageReference.putBytes(bytes); // firebaseStorage에 uid이름으로 프로필 사진 저장
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
                                        user.setUserProfileImageUrl(imageUri);
                                        signUpUser(uid, user, dialog);
                                    }
                                }
                            });
                        } else {
                            user.setUserProfileImageUrl("noImg");
                            signUpUser(uid, user, dialog);
                        }
                    }

                    private void signUpUser(final String uid, final User user, final AlertDialog dialog) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Map<String, Object> map = new HashMap<>();
                                //각각의 그룹채팅방에 유저 정보 / 접속여부를 넣음
                                map.put("normalChat/userInfo/" + uid, user);
                                map.put("officerChat/userInfo/" + uid, user);
                                map.put("normalChat/users/" + uid, false);
                                map.put("officerChat/users/" + uid, false);
                                FirebaseDatabase.getInstance().getReference().child("groupChat").updateChildren(map);

                                //lastChat방에 uid와 안읽은 메세지수 0으로 집어넣음.
                                Map<String, Object> map2 = new HashMap<>();
                                map2.put("normalChat/chatName", "일반채팅방");
                                map2.put("officerChat/chatName", "임원채팅방");
                                map2.put("normalChat/users/" + uid, 0);
                                map2.put("officerChat/users/" + uid, 0);
                                FirebaseDatabase.getInstance().getReference().child("lastChat").updateChildren(map2);
                                dialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage("회원가입완료").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(SignUpActivity.this, SplashActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.create().setCanceledOnTouchOutside(false);
                                builder.create().show();

                                PreferenceManager.setString(SignUpActivity.this, "name", user.getUserName());
                                PreferenceManager.setString(SignUpActivity.this, "hospital", user.getHospital());
                                PreferenceManager.setString(SignUpActivity.this, "phone", user.getTel());
                                PreferenceManager.setString(SignUpActivity.this, "uid", user.getUid());

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
                dialog.dismiss();
                Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                return;

            }
        });
    }

    private Boolean checkInfo() {
        if (name.getText().toString().replace(" ", "").equals("") || name.getText().toString() == null) {
            tEmail.setError("");
            tTel.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tName.setError("이름을 입력하세요.");
            return true;
        }
        if (hospital.getText().toString().replace(" ", "").equals("") || hospital.getText().toString() == null) {
            tEmail.setError("");
            tTel.setError("");
            tPassword.setError("");
            tName.setError("");
            tHospital.setError("병원을 입력하세요.");
            return true;
        }
        if (tel.getText().toString().replace(" ", "").equals("") || tel.getText().toString() == null) {
            tEmail.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tName.setError("");
            tTel.setError("전화번호을 입력하세요.");
            return true;
        }
        if (email.getText().toString().replace(" ", "").equals("") || email.getText().toString() == null) {
            tName.setError("");
            tTel.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tEmail.setError("이메일을 입력하세요.");
            return true;
        }
        if (password.getText().toString().replace(" ", "").equals("") || password.getText().toString() == null || password.getText().length() < 6) {
            tEmail.setError("");
            tTel.setError("");
            tName.setError("");
            tHospital.setError("");
            tPassword.setError("비밀번호를 6자리 이상으로 입력하세요.");
            return true;
        }
        return false;
    }

    //사진크기조절
    private Bitmap resize(Context context, Uri uri, int resize) {
        Bitmap resizeBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            Log.d("사진크기", "width=" + width + "/height=" + height);
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }
            Log.d("사진크기", "width=" + width + "/height=" + height + "/samplesize=" + samplesize);

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap = bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    //사진첩에서 사진 받아오는 부분.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            profileImageView.setImageURI(data.getData()); // 회원가입에 있는 이미지뷰를 바꿈
            imageUri = data.getData();//이미지 경로 원본
            filePath = getRealPathFromURI(imageUri);
        }
    }

    private String getRealPathFromURI(Uri fileUri) {
        String result;
        Cursor cursor = getContentResolver().query(fileUri, null, null,null,null);
        if(cursor==null){
            result = fileUri.getPath();
        }else{
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        }
        catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }
}
