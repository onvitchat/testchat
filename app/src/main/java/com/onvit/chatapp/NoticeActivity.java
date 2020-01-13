package com.onvit.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.onvit.chatapp.chat.BigPictureActivity;
import com.onvit.chatapp.chat.GroupMessageActivity;
import com.onvit.chatapp.model.ChatModel;
import com.onvit.chatapp.model.Notice;
import com.onvit.chatapp.model.NotificationModel;
import com.onvit.chatapp.model.User;
import com.squareup.picasso.Picasso;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoticeActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar chatToolbar;
    String noticeName;
    Button insertNotice, insertImg;
    LinearLayout layoutTime, layoutBtn;
    EditText title, content;
    TextView time, name;
    ImageView img;
    String code;
    Uri imgUri;
    private DatabaseReference firebaseDatabase;
    private String uid;
    private ArrayList<String> registration_ids = new ArrayList<>();
    RecyclerView recyclerView;
    NoticeActivityRecyclerAdapter noticeActivityRecyclerAdapter;
    ArrayList<String> imgPath = new ArrayList<>();
    ArrayList<Image> imagesList = new ArrayList<>();
    ArrayList<String> deleteKey = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        recyclerView = findViewById(R.id.notice_recyclerView);
        insertNotice = findViewById(R.id.insert_notice);
        insertImg = findViewById(R.id.insert_img);
        layoutTime = findViewById(R.id.layout_time);
        layoutBtn = findViewById(R.id.layout_button);
        title = findViewById(R.id.edit_title);
        content = findViewById(R.id.edit_content);
        time = findViewById(R.id.text_time);
        name = findViewById(R.id.writer);
        img = findViewById(R.id.info_img);
        layoutTime.setVisibility(View.GONE);
        layoutBtn.setVisibility(View.VISIBLE);
        imgUri = null;
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        registration_ids = getIntent().getStringArrayListExtra("userList");
        if(getIntent().getStringExtra("modify")!=null){
            noticeName = "공지사항 수정";
            insertNotice.setText("공지사항수정");
            title.setText(getIntent().getStringExtra("title"));
            content.setText(getIntent().getStringExtra("content"));
            code = getIntent().getStringExtra("code");
            ArrayList<String> list = getIntent().getStringArrayListExtra("img");
            imagesList = getIntent().getParcelableArrayListExtra("imgList");
            deleteKey = getIntent().getStringArrayListExtra("deleteKey");
            if (list.size()>0){
                for(String s : list){
                    if(!s.equals("noImg")){
                        imgPath.add(s);
                        recyclerView.setVisibility(View.VISIBLE);
                    }else{
                        imagesList.clear();
                        break;
                    }
                }
            }

        }else if(getIntent().getStringExtra("view")!=null){
            noticeName = "공지사항";
            title.setText(getIntent().getStringExtra("title"));
            content.setText(getIntent().getStringExtra("content"));
            name.setText("작성자 : "+ getIntent().getStringExtra("name"));
            time.setText(getIntent().getStringExtra("time"));
            ArrayList<String> list = getIntent().getStringArrayListExtra("img");
            if (list.size()>0){
                for(String s : list){
                    if(!s.equals("noImg")){
                        imgPath.add(s);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }
            title.setFocusable(false);
            content.setFocusable(false);
            title.setClickable(false);
            content.setClickable(false);
            layoutTime.setVisibility(View.VISIBLE);
            layoutBtn.setVisibility(View.GONE);
        }else if(getIntent().getStringExtra("insert")!=null){
            noticeName = "공지사항 등록";
        }

        chatToolbar = findViewById(R.id.notice_toolbar);
        chatToolbar.setBackgroundResource(R.color.notice);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(noticeName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        insertNotice.setOnClickListener(this);
        insertImg.setOnClickListener(this);

        noticeActivityRecyclerAdapter = new NoticeActivityRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(NoticeActivity.this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(noticeActivityRecyclerAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);//화면 사라지는 방향
    }
    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String text = button.getText().toString();

        switch (text){
            case "공지사항등록" :
                NoticeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(NoticeActivity.this);
                View noticeView = getLayoutInflater().from(this).inflate(R.layout.notice,null);
                final TextView tx = noticeView.findViewById(R.id.progress_notice);
                builder.setView(noticeView);
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                tx.setText("공지사항을 등록하는 중입니다.");
                insertNotices(title, content,dialog, tx);
                break;
            case "공지사항수정" :
                NoticeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(NoticeActivity.this);
                View noticeView2 = getLayoutInflater().from(this).inflate(R.layout.notice,null);
                final TextView tx2 = noticeView2.findViewById(R.id.progress_notice);
                builder2.setView(noticeView2);
                final AlertDialog dialog2 = builder2.create();
                dialog2.setCanceledOnTouchOutside(false);
                dialog2.setCancelable(false);
                dialog2.show();
                tx2.setText("공지사항을 수정하는 중입니다.");
                modifyNotice(title, content,dialog2, tx2);
                break;
            case "이미지첨부" :
                Intent intent = new Intent(this, GalleryActivity.class);
                Params params = new Params();
                params.setCaptureLimit(1);
                params.setPickerLimit(10);
                params.setToolbarColor(R.id.dark);
                params.setActionButtonColor(R.id.dark);
                params.setButtonTextColor(R.id.dark);
                intent.putExtra(Constants.KEY_PARAMS, params);
                startActivityForResult(intent, Constants.TYPE_MULTI_PICKER);
                break;
        }
    }

    private void modifyNotice(EditText editTextTitle, EditText editTextContent, final AlertDialog dialog, final TextView tx) {
        final String title = editTextTitle.getText().toString();
        final String content = editTextContent.getText().toString();
        if (title.equals("") || content.equals("")) {
            NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            dialog.dismiss();
            Toast.makeText(NoticeActivity.this, "제목과 내용을 입력하시기 바랍니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Map<String, String> uriList = new HashMap<>();
        if(imagesList != null && imagesList.size()>0 ){
            final int[] flag = {0};
            final DatabaseReference userMessageKeyRef = firebaseDatabase.child("Notice").push();
            for(Image i : imagesList) {
                imgUri = i.uri;
                final String id = i._id + "";
                String uri = imgUri.toString();
                Bitmap bitmap = resize(this, imgUri, 500);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Notice Img").child(userMessageKeyRef.getKey()).child(id);
                UploadTask uploadTask;
                if (!uri.startsWith("https://firebasestorage")) {//사진수정했을때
                    String filePath = getRealPathFromURI(imgUri);
                    ExifInterface exif = null;
                    try {
                        exif = new ExifInterface(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    Bitmap newBitmap = rotateBitmap(bitmap, orientation);

                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytes = baos.toByteArray();
                    uploadTask = storageReference.putBytes(bytes);
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                Toast.makeText(NoticeActivity.this, "오류.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                                throw task.getException();
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        //storageReference 에 저장한 이미지 uri를 불러옴
                        @Override
                        public void onComplete(@NonNull final Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri taskResult = task.getResult();
                                String imageUri = taskResult.toString();
                                uriList.put(id, imageUri);
                                flag[0]++;
                                tx.setText("이미지를 업로드 중입니다(" + flag[0] + "/" + imagesList.size() + ")");
                                if (flag[0] == imagesList.size()) {
                                    final Notice notice = new Notice();
                                    notice.setTitle(title);
                                    notice.setContent(content);
                                    notice.setUid(uid);
                                    SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일");
                                    Date date = new Date();
                                    String newDate = sd.format(date);
                                    notice.setTime(newDate);
                                    notice.setTimestamp(date.getTime());
                                    notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
                                    notice.setImg(uriList);
                                    Map<String, Object> map = new HashMap<>();
                                    map.put(code, null);
                                    firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            firebaseDatabase.child("Notice").child(userMessageKeyRef.getKey()).setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    dialog.dismiss();
                                                    NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    Toast.makeText(NoticeActivity.this, "수정하였습니다.", Toast.LENGTH_SHORT).show();
                                                    if (deleteKey.size() > 0) {
                                                        for (String key : deleteKey) {
                                                            FirebaseStorage.getInstance().getReference().child("Notice Img").child(code).child(key).delete();
                                                        }
                                                    }
                                                    sendFcm(registration_ids);
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }

                            }
                        }
                    });
                } else {//사진 수정안했을때
                    deleteKey.remove(id);
                    uriList.put(id,imgPath.get(flag[0]));
                    flag[0]++;
                    if(flag[0] == imagesList.size()){
                        final Notice notice = new Notice();
                        notice.setTitle(title);
                        notice.setContent(content);
                        notice.setUid(uid);
                        SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일");
                        Date date = new Date();
                        String newDate = sd.format(date);
                        notice.setTime(newDate);
                        notice.setTimestamp(date.getTime());
                        notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
                        notice.setImg(uriList);
                        Map<String, Object> map = new HashMap<>();
                        map.put(code, null);
                        firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                firebaseDatabase.child("Notice").child(code).setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        Toast.makeText(NoticeActivity.this, "수정하였습니다.", Toast.LENGTH_SHORT).show();
                                        if(deleteKey.size()>0){
                                            for (String key : deleteKey) {
                                                FirebaseStorage.getInstance().getReference().child("Notice Img").child(code).child(key).delete();
                                            }
                                        }
                                        sendFcm(registration_ids);
                                        finish();
                                    }
                                });
                            }
                        });
                    }
                }
            }
        }else{
            uriList.put("noImg","noImg");
            final Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setUid(uid);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일");
            Date date = new Date();
            String newDate = sd.format(date);
            notice.setTime(newDate);
            notice.setTimestamp(date.getTime());
            notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
            notice.setImg(uriList);
            Map<String, Object> map = new HashMap<>();
            map.put(code, null);
            firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    firebaseDatabase.child("Notice").child(code).setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Toast.makeText(NoticeActivity.this, "수정하였습니다.", Toast.LENGTH_SHORT).show();
                            if(deleteKey.size()>0){
                                for (String key : deleteKey) {
                                    FirebaseStorage.getInstance().getReference().child("Notice Img").child(code).child(key).delete();
                                }
                            }
                            sendFcm(registration_ids);
                            finish();
                        }
                    });
                }
            });
        }
    }

    private void insertNotices(EditText editTextTitle, EditText editTextContent, final AlertDialog dialog, final TextView tx) {
        final String title = editTextTitle.getText().toString();
        final String content = editTextContent.getText().toString();
        final int[] flag = {0};
        if (title.equals("") || content.equals("")) {
            NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            dialog.dismiss();
            Toast.makeText(NoticeActivity.this, "제목과 내용을 입력하시기 바랍니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        final Map<String,String> imgUriList = new HashMap<>();
        if(imagesList!=null && imagesList.size()>0 ){
            final DatabaseReference userMessageKeyRef = firebaseDatabase.child("Notice").push();
            for(Image i : imagesList){
                imgUri = i.uri;
                final String id = i._id+"";
                Bitmap bitmap = resize(this, imgUri, 500);
                String filePath = getRealPathFromURI(imgUri);
                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                Bitmap newBitmap = rotateBitmap(bitmap, orientation);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();

                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Notice Img").child(userMessageKeyRef.getKey()).child(id);
                UploadTask uploadTask = storageReference.putBytes(bytes);

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
                    public void onComplete(@NonNull final Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri taskResult = task.getResult();
                            String imageUri = taskResult.toString();
                            imgUriList.put(id, imageUri);
                            flag[0]++;
                            tx.setText("이미지를 업로드 중입니다("+flag[0]+"/"+imagesList.size()+")");
                            Log.d("이미지 업로드", flag[0]+"");
                            if(flag[0]==imagesList.size()){
                                Log.d("이미지 업로드", "시작");
                                Notice notice = new Notice();
                                notice.setTitle(title);
                                notice.setContent(content);
                                notice.setUid(uid);
                                SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일");
                                Date date = new Date();
                                String newDate = sd.format(date);
                                notice.setTime(newDate);
                                notice.setTimestamp(date.getTime());
                                notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
                                notice.setImg(imgUriList);
                                firebaseDatabase.child("Notice").child(userMessageKeyRef.getKey()).setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        dialog.dismiss();
                                        NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        Toast.makeText(NoticeActivity.this, "공지사항을 등록하였습니다.", Toast.LENGTH_SHORT).show();
                                        sendFcm(registration_ids);
                                        finish();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }else{
            imgUriList.put("noImg", "noImg");
            Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setUid(uid);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일");
            Date date = new Date();
            String newDate = sd.format(date);
            notice.setTime(newDate);
            notice.setTimestamp(date.getTime());
            notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
            notice.setImg(imgUriList);
            Log.d("등록", notice.toString());
            firebaseDatabase.child("Notice").push().setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    dialog.dismiss();
                    NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    Toast.makeText(NoticeActivity.this, "공지사항을 등록하였습니다.", Toast.LENGTH_SHORT).show();
                    sendFcm(registration_ids);
                    finish();
                }
            });
        }
    }

    private void sendFcm(List<String> registration_ids) {
        Gson gson = new Gson();

        String userName = PreferenceManager.getString(NoticeActivity.this, "name");
        String hospital = PreferenceManager.getString(NoticeActivity.this, "hospital");
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.registration_ids = registration_ids;
        notificationModel.notification.title = userName+"("+hospital+")";
        notificationModel.notification.text = userName+"("+hospital+")"+"님이 새로운 공지를 등록하였습니다.";
        notificationModel.notification.tag = "notice";
        notificationModel.notification.click_action = "GroupMessage";

        notificationModel.data.title = userName;
        notificationModel.data.text = userName+"님이 새로운 공지를 등록하였습니다.";
        notificationModel.data.tag = "notice";
        notificationModel.data.click_action = "GroupMessage";


        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));
        Request request = new Request.Builder().header("Content-Type", "apllication/json")
                .addHeader("Authorization", "key=AAAAjkt-NJ4:APA91bF8vZrFrqLIRfpPwE_WvUrGj4aQEP8xF9_UvvG4MZXA2iV-o7NPAJdGGYhlMl_JXP8KQiF_YWQeVhT0DE8BSppJUfYazA0QR7tjozAdpzMvX9xLSHJ1mkOevT4_OlohvlOYS_e-")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.TYPE_MULTI_PICKER && resultCode == RESULT_OK){
            recyclerView.setVisibility(View.VISIBLE);
            imagesList.clear();
            imgPath.clear();
            imagesList = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
            for(Image i : imagesList){
                imgPath.add(i.uri.toString());
            }
            noticeActivityRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private Bitmap resize(Context context, Uri uri, int resize) {
        Bitmap resizeBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;

            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize) {
                    break;
                }

                width /= 2;
                height /= 2;
                samplesize *= 2;
            }


            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap = bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return resizeBitmap;
    }

    private String getRealPathFromURI(Uri fileUri) {
        String result;
        Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
        if (cursor == null) {
            result = fileUri.getPath();
        } else {
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
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    class NoticeActivityRecyclerAdapter extends RecyclerView.Adapter<NoticeActivityRecyclerAdapter.NoticeViewHolder> {

        public NoticeActivityRecyclerAdapter() {

        }

        @NonNull
        @Override
        public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_activity, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NoticeViewHolder holder, final int position) {
            Log.d("이미지", position+"");

            switch (noticeName) {
                case "공지사항 등록":
                case "공지사항 수정":
                    holder.text.setText("클릭하여 삭제");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imgPath.remove(position);
                            imagesList.remove(position);
                            noticeActivityRecyclerAdapter.notifyDataSetChanged();
                            if(imgPath.size()==0){
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    });
                    break;
                case "공지사항":
                    holder.text.setText("클릭하여 확대");
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(NoticeActivity.this, BigPictureActivity.class);
                            intent.putExtra("uri", imgPath.get(position));
                            intent.putExtra("position", position);
                            intent.putStringArrayListExtra("list", imgPath);
                            intent.putExtra("name", name.getText().toString());
                            startActivity(intent);
                        }
                    });
                    break;
            }
            Glide.with(NoticeActivity.this).load(imgPath.get(position)).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            GradientDrawable gradientDrawable = (GradientDrawable) NoticeActivity.this.getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);
        }

        @Override
        public int getItemCount() {
            return imgPath.size();
        }


        private class NoticeViewHolder extends RecyclerView.ViewHolder {
            private TextView text;
            private ImageView imageView;
            NoticeViewHolder(View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.layout_img_text);
                imageView = itemView.findViewById(R.id.info_img);

            }
        }
    }
}
