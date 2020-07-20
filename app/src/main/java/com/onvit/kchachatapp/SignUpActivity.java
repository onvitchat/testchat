package com.onvit.kchachatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.onvit.kchachatapp.model.ChatModel;
import com.onvit.kchachatapp.model.LastChat;
import com.onvit.kchachatapp.model.Notice;
import com.onvit.kchachatapp.model.User;
import com.onvit.kchachatapp.util.PreferenceManager;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1000;
    private EditText name,hospital,tel,email,grade,password;
    private TextInputLayout tName,tHospital,tTel,tEmail,tPassword;
    private ImageView profileImageView, camera, invalid;
    private Uri imageUri;
    private User user;
    private String filePath;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        imageUri = null;
        profileImageView = findViewById(R.id.signupActivity_imageview_profile);
        constraintLayout = findViewById(R.id.update_camera);
        camera = findViewById(R.id.camera);
        invalid = findViewById(R.id.img_text);
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
        grade = findViewById(R.id.signupActivity_edittext_grade);
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
        Button signUp = findViewById(R.id.signupActivity_button_signup);
        password.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //회원가입
        if (getIntent().getParcelableExtra("modify") == null) {
            final User joinUser = getIntent().getParcelableExtra("user");
            grade.setText(joinUser.getGrade());
            name.setText(joinUser.getUserName());
            tel.setText(joinUser.getTel());
            email.setText(joinUser.getUserEmail());
            hospital.setText(joinUser.getHospital());
            grade.setFocusable(false);
            grade.setClickable(false);
            name.setFocusable(false);
            name.setClickable(false);
            signUp.setText("회원가입");
            camera.setVisibility(View.VISIBLE);
            invalid.setVisibility(View.VISIBLE);
            password.setOnEditorActionListener(new TextView.OnEditorActionListener() { // 완료눌러도 회원가입기능되게~
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        signUp();
                    }
                    return false;
                }
            });

            signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signUp();

                }
            });
        } else {
            constraintLayout.setVisibility(View.VISIBLE);
            signUp.setText("저장");
            user = getIntent().getParcelableExtra("modify");
            if (user.getUserProfileImageUrl().equals("noImg")) {
                profileImageView.setImageResource(R.drawable.standard_profile);
            } else {
                imageUri = Uri.parse(user.getUserProfileImageUrl());
                Glide.with(SignUpActivity.this).load(imageUri).into(profileImageView);
            }
            grade.setText(user.getGrade());
            name.setText(user.getUserName());
            tel.setText(user.getTel());
            hospital.setText(user.getHospital());
            email.setText(user.getUserEmail());
            email.setFocusable(false);
            email.setClickable(false);
            name.setFocusable(false);
            name.setClickable(false);
            grade.setFocusable(false);
            grade.setClickable(false);
            tPassword.setVisibility(View.GONE);
            signUp.setOnClickListener(new View.OnClickListener() {
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
        if (imageUri != null && !imageUri.equals(Uri.parse(user.getUserProfileImageUrl()))) {
            Bitmap bitmap = resize(SignUpActivity.this, imageUri);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (exif != null) {
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Bitmap newBitmap = rotateBitmap(bitmap, orientation);
                if (newBitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] bytes = baos.toByteArray();
                    final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(user.getUid());
                    UploadTask uploadTask = storageReference.putBytes(bytes); // firebaseStorage에 uid이름으로 프로필 사진 저장
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        //storageReference 에 저장한 이미지 uri를 불러옴
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri taskResult = task.getResult();
                                String imageUri = String.valueOf(taskResult);
                                modifyInfo(imageUri, dialog);

                            }
                        }
                    });
                } else {
                    String imageUri = user.getUserProfileImageUrl();
                    modifyInfo(imageUri, dialog);
                }
            }
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
        modifyUser.setUserEmail(email.getText().toString());
        modifyUser.setUserName(name.getText().toString());
        modifyUser.setUserProfileImageUrl(imageUri);
        modifyUser.setGrade(grade.getText().toString());
        map.put(user.getUid(), modifyUser);
        FirebaseDatabase.getInstance().getReference().child("Users").updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
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
                                Notice notice = item.getValue(Notice.class);
                                String key = item.getKey();
                                if (notice != null && key != null) {
                                    notice.setName(PreferenceManager.getString(SignUpActivity.this, "name") + "(" + PreferenceManager.getString(SignUpActivity.this, "hospital") + ")");
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(key, notice);
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
        builder.setView(R.layout.loading);
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
                            Utiles.customToast(SignUpActivity.this, task.getException().toString()).show();
                            return;
                        }
                        if(task.getResult()!=null && task.getResult().getUser()!=null){
                            final String uid = task.getResult().getUser().getUid();
                            final User signUser = new User();
                            signUser.setUserName(name.getText().toString());
                            signUser.setHospital(hospital.getText().toString());
                            signUser.setGrade(grade.getText().toString());
                            signUser.setTel(tel.getText().toString());
                            signUser.setUserEmail(email.getText().toString());
                            signUser.setPushToken("");
                            signUser.setUid(uid);
                            if (imageUri != null) {
                                Bitmap bitmap = resize(SignUpActivity.this, imageUri);
                                ExifInterface exif;
                                Log.d("이미지파일", filePath);
                                try {
                                    exif = new ExifInterface(filePath);
                                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                                    Bitmap newBitmap = rotateBitmap(bitmap, orientation);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    if(newBitmap!=null){
                                        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                        final byte[] bytes = baos.toByteArray();
                                        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("userImages").child(uid);
                                        UploadTask uploadTask = storageReference.putBytes(bytes); // firebaseStorage에 uid이름으로 프로필 사진 저장
                                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                            @Override
                                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                if (!task.isSuccessful()) {
                                                    throw Objects.requireNonNull(task.getException());
                                                }
                                                return storageReference.getDownloadUrl();
                                            }
                                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                            //storageReference 에 저장한 이미지 uri를 불러옴
                                            @Override
                                            public void onComplete(@NonNull Task<Uri> task) {
                                                if (task.isSuccessful()) {
                                                    Uri taskResult = task.getResult();
                                                    String imageUri = String.valueOf(taskResult);
                                                    signUser.setUserProfileImageUrl(imageUri);
                                                    signUpUser(uid, signUser, dialog);
                                                }
                                            }
                                        });
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                signUser.setUserProfileImageUrl("noImg");
                                signUpUser(uid, signUser, dialog);
                            }
                        }
                    }

                    private void signUpUser(final String uid, final User user, final AlertDialog dialog) {
                        FirebaseDatabase.getInstance().getReference().child("Users").child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                PreferenceManager.setString(SignUpActivity.this, "name", user.getUserName());
                                PreferenceManager.setString(SignUpActivity.this, "hospital", user.getHospital());
                                PreferenceManager.setString(SignUpActivity.this, "phone", user.getTel());
                                PreferenceManager.setString(SignUpActivity.this, "uid", user.getUid());
//                                PreferenceManager.setString(SignUpActivity.this, "grade", user.getGrade());
                                Date date = new Date();
                                long time = date.getTime();
                                ChatModel.Comment normalComment = new ChatModel.Comment();
                                normalComment.uid = uid;
                                normalComment.message = String.format("%s(%s)님이 채팅방에 참여하였습니다.", user.getUserName(), user.getHospital());
                                normalComment.timestamp = time;
                                normalComment.type = "io";
                                normalComment.unReadCount = 0;
                                ChatModel.Comment officerComment = new ChatModel.Comment();
                                officerComment.uid = uid;
                                officerComment.message = String.format("%s(%s)님이 채팅방에 참여하였습니다.", user.getUserName(), user.getHospital());
                                officerComment.timestamp = time;
                                officerComment.type = "io";
                                officerComment.unReadCount = 0;

                                Map<String, Object> map = new HashMap<>();
                                Map<String, Object> map2 = new HashMap<>();
                                LastChat.timeInfo timeInfo = new LastChat.timeInfo();
                                timeInfo.setExitTime(time);
                                timeInfo.setInitTime(time - 1);
                                timeInfo.setUnReadCount(0);
                                FirebaseDatabase.getInstance().getReference().child("groupChat").child("회원채팅방").child("comments").push().setValue(normalComment);
                                //각각의 그룹채팅방에 유저 정보 / 접속여부를 넣음
                                if (user.getGrade().equals("임원")) {
                                    map.put("회원채팅방/users/" + uid, false);
                                    map.put("임원채팅방/users/" + uid, false);
                                    map.put("회원채팅방/id/", 1);
                                    map.put("임원채팅방/id/", 2);
                                    map2.put("회원채팅방/chatName", "회원채팅방");
                                    map2.put("회원채팅방/existUsers/" + uid, timeInfo);
                                    map2.put("임원채팅방/chatName", "임원채팅방");
                                    map2.put("임원채팅방/existUsers/" + uid, timeInfo);
                                    FirebaseDatabase.getInstance().getReference().child("groupChat").child("임원채팅방").child("comments").push().setValue(officerComment);
                                } else {
                                    map.put("회원채팅방/users/" + uid, false);
                                    map.put("회원채팅방/id/", 1);
                                    map2.put("회원채팅방/chatName", "회원채팅방");
                                    map2.put("회원채팅방/existUsers/" + uid, timeInfo);
                                }
                                FirebaseDatabase.getInstance().getReference().child("groupChat").updateChildren(map);
                                //lastChat방에 uid와 안읽은 메세지수 0으로 집어넣음.
                                FirebaseDatabase.getInstance().getReference().child("lastChat").updateChildren(map2);
                                dialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                builder.setMessage("회원가입완료").setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                        UserMap.clearApp();
                                        UserMap.setUid(uid);
                                        UserMap.getUserMap();
                                        intent.putExtra("user", user);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder.setCancelable(false);
                                builder.create().setCanceledOnTouchOutside(false);
                                builder.create().show();

                            }
                        });
                    }
                }).addOnCanceledListener(SignUpActivity.this, new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Utiles.customToast(SignUpActivity.this, "가입취소").show();
            }
        }).addOnFailureListener(SignUpActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Utiles.customToast(SignUpActivity.this, e.toString()).show();
            }
        });
    }

    private Boolean checkInfo() {
        if (name.getText().toString().replace(" ", "").equals("")) {
            tEmail.setError("");
            tTel.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tName.setError("이름을 입력하세요.");
            Utiles.customToast(SignUpActivity.this, "이름을 입력하세요.").show();
            return true;
        }
        if (hospital.getText().toString().replace(" ", "").equals("")) {
            tEmail.setError("");
            tTel.setError("");
            tPassword.setError("");
            tName.setError("");
            tHospital.setError("병원을 입력하세요.");
            Utiles.customToast(SignUpActivity.this, "병원을 입력하세요.").show();
            return true;
        }
        if (tel.getText().toString().replace(" ", "").equals("")) {
            tEmail.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tName.setError("");
            tTel.setError("전화번호을 입력하세요.");
            Utiles.customToast(SignUpActivity.this, "전화번호을 입력하세요.").show();
            return true;
        }
        if (email.getText().toString().replace(" ", "").equals("") || checkEmail(email.getText().toString())) {
            tName.setError("");
            tTel.setError("");
            tPassword.setError("");
            tHospital.setError("");
            tEmail.setError("이메일을 형식에 맞게 입력하세요.");
            Utiles.customToast(SignUpActivity.this, "이메일을 형식에 맞게 입력하세요.").show();
            return true;
        }
        if (password.getText().toString().replace(" ", "").equals("") || password.getText().length() < 6) {
            tEmail.setError("");
            tTel.setError("");
            tName.setError("");
            tHospital.setError("");
            tPassword.setError("비밀번호를 6자리 이상으로 입력하세요.");
            Utiles.customToast(SignUpActivity.this, "비밀번호를 6자리 이상으로 입력하세요.").show();
            return true;
        }
        return false;
    }

    //사진크기조절
    private Bitmap resize(Context context, Uri uri) {
        Bitmap resizeBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            Log.d("사진크기", "width=" + width + "/height=" + height);
            int samplesize = 1;


            while (width / 2 >= Utiles.RESIZE || height / 2 >= Utiles.RESIZE) {//2번
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }
            Log.d("사진크기", "width=" + width + "/height=" + height + "/samplesize=" + samplesize);

            options.inSampleSize = samplesize;
            resizeBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    //사진첩에서 사진 받아오는 부분.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();//이미지 경로 원본
            filePath = getRealPathFromURI(imageUri);
            ExifInterface exif;
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                exif = new ExifInterface(filePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Bitmap newBitmap = rotateBitmap(b, orientation);
                Glide.with(this).load(newBitmap).into(profileImageView);
                camera.setVisibility(View.GONE);
                invalid.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
                Utiles.customToast(SignUpActivity.this, "지원하지 않는 이미지 형식입니다.").show();
            }
            if (filePath == null) {
                Utiles.customToast(SignUpActivity.this, "지원하지 않는 이미지 형식입니다.").show();
            }
        }
    }

    private String getRealPathFromURI(Uri fileUri) {
        String result;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(fileUri, proj, null, null, null);
        if (cursor == null) {
            result = fileUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
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
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkEmail(String email) {
        boolean result = true;
        String regex = "^[_a-zA-Z0-9-.]+@[.a-zA-Z0-9-]+\\.[a-zA-Z]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        if (m.matches()) {
            result = false;
        }
        return result;
    }
}
