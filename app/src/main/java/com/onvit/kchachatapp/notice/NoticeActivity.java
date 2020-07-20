package com.onvit.kchachatapp.notice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.GradientDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.fcm.DownloadUri;
import com.onvit.kchachatapp.model.Notice;
import com.onvit.kchachatapp.model.User;
import com.onvit.kchachatapp.util.PreferenceManager;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class NoticeActivity extends AppCompatActivity implements View.OnClickListener {
    String noticeName;
    Button insertNotice, insertImg;
    LinearLayout layoutTime, layoutBtn;
    EditText title, content;
    TextView time, name;
    ImageView img;
    String code;
    Uri imgUri;
    RecyclerView recyclerView;
    NoticeActivityRecyclerAdapter noticeActivityRecyclerAdapter;
    ArrayList<String> imgPath = new ArrayList<>();
    ArrayList<Image> imagesList = new ArrayList<>();
    ArrayList<String> deleteKey = new ArrayList<>();
    InputStream inputStream;
    private DatabaseReference firebaseDatabase;
    private String uid;
    private ArrayList<String> registration_ids = new ArrayList<>();
    private Map<String, User> userMap;

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
        uid = UserMap.getUid();
        userMap = UserMap.getInstance();

        if (getIntent().getStringExtra("modify") != null) {
            noticeName = "공지사항 수정";
            insertNotice.setText("수정완료");
            title.setText(getIntent().getStringExtra("title"));
            content.setText(getIntent().getStringExtra("content"));
            code = getIntent().getStringExtra("code");
            ArrayList<String> list = getIntent().getStringArrayListExtra("img");
            imagesList = getIntent().getParcelableArrayListExtra("imgList");
            deleteKey = getIntent().getStringArrayListExtra("deleteKey");
            if (list != null) {
                for (String s : list) {
                    imgPath.add(s);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

        } else if (getIntent().getStringExtra("insert") != null) {
            noticeName = "공지사항 등록";
        }
        Toolbar chatToolbar = findViewById(R.id.notice_toolbar);
        chatToolbar.setBackgroundResource(R.color.notice);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(noticeName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        insertNotice.setOnClickListener(this);
        insertImg.setOnClickListener(this);

        noticeActivityRecyclerAdapter = new NoticeActivityRecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(NoticeActivity.this, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(noticeActivityRecyclerAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromtop, R.anim.tobottom);//화면 사라지는 방향
    }

    @Override
    public void onClick(View view) {
        Button button = (Button) view;
        String text = button.getText().toString();

        switch (text) {
            case "등록완료":
                NoticeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(NoticeActivity.this);
                View noticeView = View.inflate(NoticeActivity.this, R.layout.notice, null);
                final TextView tx = noticeView.findViewById(R.id.progress_notice);
                builder.setView(noticeView);
                final AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                tx.setText("공지사항을 등록하는 중입니다.");
                tx.post(new Runnable() { // 약간의 딜레이를 주어서 뷰처리 먼저 실행하고 난 다음에 아래꺼 처리하게.
                    @Override
                    public void run() {
                        insertNotices(title, content, dialog, tx);

                    }
                });
                break;
            case "수정완료":
                NoticeActivity.this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(NoticeActivity.this);
                View noticeView2 = View.inflate(NoticeActivity.this, R.layout.notice, null);
                final TextView tx2 = noticeView2.findViewById(R.id.progress_notice);
                builder2.setView(noticeView2);
                final AlertDialog dialog2 = builder2.create();
                dialog2.setCanceledOnTouchOutside(false);
                dialog2.setCancelable(false);
                dialog2.show();
                tx2.setText("공지사항을 수정하는 중입니다.");
                tx2.post(new Runnable() {
                    @Override
                    public void run() {
                        modifyNotice(title, content, dialog2, tx2);
                    }
                });
                break;
            case "이미지첨부":
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
            Utiles.customToast(NoticeActivity.this, "제목과 내용을 입력하시기 바랍니다.").show();
            return;
        }

        final Map<String, String> uriList = new HashMap<>();
        if (imagesList != null && imagesList.size() > 0) {
            final int[] flag = {0};
            final DatabaseReference userMessageKeyRef = firebaseDatabase.child("Notice").push();
            for (Image i : imagesList) {
                imgUri = i.uri;
                if (i._id < 100000) {
                    i._id = new Date().getTime();
                }
                final String id = i._id + "";
                String uri = imgUri.toString();
                Bitmap bitmap = resize(this, imgUri);
                String key = userMessageKeyRef.getKey();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final StorageReference storageReference;
                if (key != null) {
                    storageReference = FirebaseStorage.getInstance().getReference().child("Notice Img").child(key).child(id);
                } else {
                    Utiles.customToast(NoticeActivity.this, "오류.").show();
                    return;
                }
                UploadTask uploadTask = null;
                if (!uri.startsWith("https://firebasestorage")) {//사진수정했을때
                    String filePath = getRealPathFromURI(imgUri);
                    ExifInterface exif;
                    try {
                        exif = new ExifInterface(filePath);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        Bitmap newBitmap = rotateBitmap(bitmap, orientation);
                        if (newBitmap != null) {
                            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] bytes = baos.toByteArray();
                            uploadTask = storageReference.putBytes(bytes);
                        } else {
                            Utiles.customToast(NoticeActivity.this, "오류.").show();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        inputStream = new DownloadUri().execute(uri).get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    uploadTask = storageReference.putStream(inputStream);
                }
                if (uploadTask != null) {
                    uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                Utiles.customToast(NoticeActivity.this, "오류").show();
                                dialog.dismiss();
                                throw Objects.requireNonNull(task.getException());
                            }
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        //storageReference 에 저장한 이미지 uri를 불러옴
                        @Override
                        public void onComplete(@NonNull final Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri taskResult = task.getResult();
                                String imageUri = String.valueOf(taskResult);
                                uriList.put(id, imageUri);
                                flag[0]++;
                                String text = "이미지를 업로드 중입니다(" + flag[0] + "/" + imagesList.size() + ")";
                                tx.setText(text);
                                if (flag[0] == imagesList.size()) {
                                    final Notice notice = new Notice();
                                    notice.setTitle(title);
                                    notice.setContent(content);
                                    notice.setUid(uid);
                                    SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
                                    Date date = new Date();
                                    String newDate = sd.format(date);
                                    notice.setTime(newDate);
                                    notice.setTimestamp(date.getTime());
                                    notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
                                    notice.setImg(uriList);
                                    firebaseDatabase.child("Notice").child(code).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                            firebaseDatabase.child("Notice").child(userMessageKeyRef.getKey()).setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    dialog.dismiss();
                                                    NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    Utiles.customToast(NoticeActivity.this, "수정하였습니다.").show();
                                                    if (deleteKey.size() > 0) {
                                                        for (String key : deleteKey) {
                                                            FirebaseStorage.getInstance().getReference().child("Notice Img").child(code).child(key).delete();
                                                        }
                                                    }
                                                    getFcmListAndPush();
                                                    finish();
                                                }
                                            });
                                        }
                                    });
                                }

                            }
                        }
                    });
                } else {
                    Utiles.customToast(NoticeActivity.this, "오류.").show();
                    return;
                }
            }
        } else {
            uriList.put("noImg", "noImg");
            final Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setUid(uid);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
            Date date = new Date();
            String newDate = sd.format(date);
            notice.setTime(newDate);
            notice.setTimestamp(date.getTime());
            notice.setName(PreferenceManager.getString(NoticeActivity.this, "name") + "(" + PreferenceManager.getString(NoticeActivity.this, "hospital") + ")");
            notice.setImg(uriList);
            firebaseDatabase.child("Notice").child(code).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    firebaseDatabase.child("Notice").push().setValue(notice).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            dialog.dismiss();
                            NoticeActivity.this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Utiles.customToast(NoticeActivity.this, "수정하였습니다.").show();
                            if (deleteKey.size() > 0) {
                                for (String key : deleteKey) {
                                    FirebaseStorage.getInstance().getReference().child("Notice Img").child(code).child(key).delete();
                                }
                            }
                            getFcmListAndPush();
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
            Utiles.customToast(NoticeActivity.this, "제목과 내용을 입력하시기 바랍니다.").show();
            return;
        }
        final Map<String, String> imgUriList = new HashMap<>();
        if (imagesList != null && imagesList.size() > 0) {
            final DatabaseReference userMessageKeyRef = firebaseDatabase.child("Notice").push();
            for (Image i : imagesList) {
                imgUri = i.uri;
                final String id = new Date().getTime() + "";
                Bitmap bitmap = resize(this, imgUri);
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
                            throw Objects.requireNonNull(task.getException());
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    //storageReference 에 저장한 이미지 uri를 불러옴
                    @Override
                    public void onComplete(@NonNull final Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri taskResult = task.getResult();
                            String imageUri = String.valueOf(taskResult);
                            imgUriList.put(id, imageUri);
                            flag[0]++;
                            String text = "이미지를 업로드 중입니다(" + flag[0] + "/" + imagesList.size() + ")";
                            tx.setText(text);
                            Log.d("이미지 업로드", flag[0] + "");
                            if (flag[0] == imagesList.size()) {
                                Log.d("이미지 업로드", "시작");
                                Notice notice = new Notice();
                                notice.setTitle(title);
                                notice.setContent(content);
                                notice.setUid(uid);
                                SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
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
                                        Utiles.customToast(NoticeActivity.this, "공지사항을 등록하였습니다.").show();
                                        getFcmListAndPush();
                                        finish();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        } else {
            imgUriList.put("noImg", "noImg");
            Notice notice = new Notice();
            notice.setTitle(title);
            notice.setContent(content);
            notice.setUid(uid);
            SimpleDateFormat sd = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA);
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
                    Utiles.customToast(NoticeActivity.this, "공지사항을 등록하였습니다.").show();
                    getFcmListAndPush();
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.TYPE_MULTI_PICKER && resultCode == RESULT_OK) {
            recyclerView.setVisibility(View.VISIBLE);
            ArrayList<Image> img;
            if (data != null) {
                img = data.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
                for (Image i : img) {
                    imgPath.add(i.uri.toString());
                    imagesList.add(i);
                }
            }
            noticeActivityRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private Bitmap resize(Context context, Uri uri) {
        Bitmap resizeBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;

            int samplesize = 1;

            while (width / 2 >= Utiles.RESIZE || height / 2 >= Utiles.RESIZE) {//2번

                width /= 2;
                height /= 2;
                samplesize *= 2;
            }


            options.inSampleSize = samplesize;
            resizeBitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

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

    private void getFcmListAndPush() {
        List<User> userList = UserMap.getUserList();
        for (User k : userList) {
            if (k.getUid().equals(uid)) {
                Log.d("아이디", k.getUid() + ":" + uid);
                continue;
            }
            if (!k.getPushToken().equals("")) {
                registration_ids.add(k.getPushToken());
            }
        }
        Utiles.sendFcm(registration_ids, "새로운 공지가 등록되었습니다.", NoticeActivity.this, "notice", userMap.get(uid).getUserProfileImageUrl());
    }


    class NoticeActivityRecyclerAdapter extends RecyclerView.Adapter<NoticeActivityRecyclerAdapter.NoticeViewHolder> {

        private NoticeActivityRecyclerAdapter() {

        }

        @NonNull
        @Override
        public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice_activity, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NoticeViewHolder holder, final int position) {

            switch (noticeName) {
                case "공지사항 등록":
                case "공지사항 수정":
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            imgPath.remove(position);
                            imagesList.remove(position);
                            noticeActivityRecyclerAdapter.notifyDataSetChanged();
                            if (imgPath.size() == 0) {
                                recyclerView.setVisibility(View.GONE);
                            }
                        }
                    });
                    break;
            }
            Glide.with(NoticeActivity.this).load(imgPath.get(position)).placeholder(R.drawable.ic_base_img_24dp).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            GradientDrawable gradientDrawable = (GradientDrawable) NoticeActivity.this.getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);
            holder.cancel_view.bringToFront();
        }

        @Override
        public int getItemCount() {
            return imgPath.size();
        }


        private class NoticeViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView, cancel_view;

            NoticeViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.info_img);
                cancel_view = itemView.findViewById(R.id.cancel_view);
            }
        }
    }
}