package com.example.chatapp.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.ChatModel;
import com.example.chatapp.model.NotificationModel;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GroupMessageActivity extends AppCompatActivity {

    Map<String, User> users = new HashMap<>();
    String toRoom;
    String uid;
    EditText editText;
    int peopleCount = 0;
    List<ChatModel.Comment> comments = new ArrayList<>();
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_message);
        toRoom = getIntent().getStringExtra("toRoom");
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        editText = findViewById(R.id.groupMessageActivity_edittext);
        FirebaseDatabase.getInstance().getReference().child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    users.put(snapshot.getKey(), snapshot.getValue(User.class));
                }
                init();
                recyclerView = findViewById(R.id.groupMessageActivity_recyclerView);
                recyclerView.setAdapter(new GroupMessageRecyclerViewAdapter());
                recyclerView.setLayoutManager(new LinearLayoutManager(GroupMessageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void init() {
        Button button = findViewById(R.id.groupMessageActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChatModel.Comment comment = new ChatModel.Comment();
                comment.uid = uid;
                comment.message = editText.getText().toString();
                comment.timestamp = ServerValue.TIMESTAMP;
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(toRoom).child("comments").push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(toRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Map<String, Boolean> map = (Map<String, Boolean>) dataSnapshot.getValue();
                                for(String item : map.keySet()){
                                    if(item.equals(uid)){
                                        continue;
                                    }
                                    sendFcm(users.get(item).getPushToken());
                                    editText.setText("");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
    }

    void sendFcm(String pushToken) {
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = pushToken;
        notificationModel.notification.title = userName;
        notificationModel.notification.text = editText.getText().toString();
        notificationModel.data.title = userName;
        notificationModel.data.text = editText.getText().toString();

        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));
        Request request = new Request.Builder().header("Content-Type", "apllication/json")
                .addHeader("Authorization", "key=AAAAjzMp75Y:APA91bGpfG16UQ-NfQwb8xBdPoXRFah3nZxuiHaaRbYKyXC8Uz8UKPL_cX00_YWuK1ztuiEJMzLTb4En0AbLfRcw9O8KH0V5p7z0RWerFIXYfujEHPQBpcY28rMUhTVhe08mGqBCXooH")
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

    class GroupMessageRecyclerViewAdapter extends RecyclerView.Adapter<GroupMessageRecyclerViewAdapter.groupViewHolder> {

        public GroupMessageRecyclerViewAdapter() {
            getMessageList();
        }

        void getMessageList() {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(toRoom).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();
                    Map<String, Object> readUsersMap = new HashMap<>();
                    for (DataSnapshot item : dataSnapshot.getChildren()) {
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        comment_modify.readUsers.put(uid, true);
                        readUsersMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }
                    //25강 버그잡는부분 무한루프돈다고 하는데?
                    if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)) {
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(toRoom).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                //메세지가 갱신
                                notifyDataSetChanged();
                                recyclerView.scrollToPosition(comments.size() - 1);
                            }
                        });
                    } else {
                        notifyDataSetChanged();
                        recyclerView.scrollToPosition(comments.size() - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public GroupMessageRecyclerViewAdapter.groupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new groupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupMessageRecyclerViewAdapter.groupViewHolder holder, int position) {
            if (comments.get(position).uid.equals(uid)) {//내가보낸메세지
                holder.textView_message.setText(comments.get(position).message);
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                holder.linearLayout_to.setVisibility(View.INVISIBLE);
                holder.linearlayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, holder.textView_readCounter_left);
            } else {//상대방이보낸메세지
                Glide.with(holder.itemView.getContext()).load(users.get(comments.get(position).uid).getUserProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(holder.imageView_profile);
                holder.textView_name.setText(users.get(comments.get(position).uid).getUserName());
                holder.linearLayout_to.setVisibility(View.VISIBLE);
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble);
                holder.textView_message.setText(comments.get(position).message);
                holder.linearlayout_main.setGravity(Gravity.LEFT);
                setReadCounter(position, holder.textView_readCounter_right);
            }
            //메세지 시간 표시
            long unixTime = (long) comments.get(position).timestamp;
            Date date = new Date(unixTime);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            String time = simpleDateFormat.format(date);
            holder.textView_timestamp.setText(time);
        }

        @Override
        public int getItemCount() {
            return comments.size();
        }

        void setReadCounter(final int position, final TextView textView) { // 읽음 표시하는부분
            //서버에 무리가 가서 이런식으로 peopleCount를 만들었다고함.
            //만들기 전에는 메세지 수만큼 인원수를 서버에 물어본다고함.
            if (peopleCount == 0) {
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(toRoom).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, Boolean> users = (Map<String, Boolean>) dataSnapshot.getValue();
                        peopleCount = users.size();
                        int count = peopleCount - comments.get(position).readUsers.size();
                        if (count > 0) {
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(String.valueOf(count));
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            } else {
                int count = peopleCount - comments.get(position).readUsers.size();
                if (count > 0) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(String.valueOf(count));
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }
            }
        }


        private class groupViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_to;
            public LinearLayout linearlayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public groupViewHolder(@NonNull View view) {
                super(view);
                textView_message = view.findViewById(R.id.messageItem_textView_message);
                textView_name = view.findViewById(R.id.messageItem_textview_name);
                imageView_profile = view.findViewById(R.id.messageItem_imageview_profile);
                linearLayout_to = view.findViewById(R.id.messageItem_linearlayout_to);
                linearlayout_main = view.findViewById(R.id.messageItem_linearlayout_main);
                textView_timestamp = view.findViewById(R.id.messageItem_textView_timestamp);
                textView_readCounter_left = view.findViewById(R.id.messageItem_textview_readCounter_left);
                textView_readCounter_right = view.findViewById(R.id.messageItem_textview_readCounter_right);
            }
        }
    }
    @Override
    public void onBackPressed() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener); // 이벤트 제거.
        }
        Intent intent = new Intent(GroupMessageActivity.this, MainActivity.class);
        intent.putExtra("MessageActivity", "MessageActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }
}
