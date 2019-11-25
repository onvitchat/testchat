package com.example.chatapp.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MessageActivity extends AppCompatActivity {
    int peopleCount = 0; // 채팅방 참여 인원
    private String toUid; // 메세지 받는 상대 uid
    private Button button;
    private EditText editText;
    private String uid; // 클라이언트 uid
    private String chatRoomUid; // 채팅방 고유 키 값
    private RecyclerView recyclerView;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private User user;
    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        uid = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 클라이언트 uid
//        toUid = getIntent().getStringExtra("uid"); // 메세지 받는 상대 uid
        user = getIntent().getParcelableExtra("uid");
        toUid = user.getUid();
        Log.d("user", user.toString());
        button = findViewById(R.id.messageActivity_button);
        editText = findViewById(R.id.messageActivity_editText);

        recyclerView = findViewById(R.id.messageActivity_recyclerView);

//        FirebaseDatabase.getInstance().getReference().child("Users").child(toUid).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //채팅상대방의 정보를 가지고옴.
//                user = dataSnapshot.getValue(User.class);
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString() != null && !editText.getText().toString().replace(" ","").equals("")) {
                    if (chatRoomUid == null) {
                        button.setEnabled(false);
                        ChatModel chatModel = new ChatModel(); // 채팅방 참여 유저들과 채팅내용을 담을 객체.
                        chatModel.users.put(uid, true);
                        chatModel.users.put(toUid, true);
                        // push를 해줘야 특정 primarykey같은것이 생겨 그곳에 내용이 쌓이게됨. 해당 채팅방의 고유 키값이 생기고 채팅방 참여 유저들의 정보를 넣음.
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                checkChatRoom();
                            }
                        });
                    } else {
                        ChatModel.Comment comment = new ChatModel.Comment();
                        comment.uid = uid;
                        comment.message = editText.getText().toString();
                        comment.timestamp = ServerValue.TIMESTAMP;
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                        sendFcm();
                        editText.setText("");
//                            .addOnCompleteListener(new OnCompleteListener<Void>() { // 이렇게 하면 좀 느림.
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            editText.setText("");
//                        }
//                    });
                    }
                } else {
                    return;
                }


            }
        });
        checkChatRoom();
    }

    void sendFcm() {
        Gson gson = new Gson();

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = user.getPushToken();
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

    void checkChatRoom() { // 대화방 중복체크.
        // chatrooms / users / 클라이언트uid => true 인 chatroom을 가지고옴. (쓸데없이 클라이언트가 속해있는 모든채팅방을 가지고 오는거같음, 구조를 변경하거나 값을 받아오는 방법 바꿔보기)
        FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {// 클라이언트가 속해 있는 채팅방의 정보가 dataSnapshot으로 넘어옴.
                        if (dataSnapshot.getValue() == null) {
//                            ChatModel newRoom = new ChatModel();
//                            newRoom.users.put(uid, true);
//                            newRoom.users.put(toUid, true);
//                            FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(newRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    checkChatRoom();
//                                }
//                            });
                            return;
                        }
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            //chatrooms안의 대화방 하나하나
                            ChatModel chatModel = item.getValue(ChatModel.class);
                            Log.d("chat2", "==" + chatModel.toString());
                            //클라이언트와 대화상대만 있는 채팅방
                            if (chatModel.users.containsKey(toUid) && chatModel.users.size() == 2) {
                                chatRoomUid = item.getKey();
                                if (editText.getText().toString() == null || editText.getText().toString().equals("")) {
                                    //이전 대화목록이 있을대 목록 불러오는곳.
                                    recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                                    recyclerView.setAdapter(new RecyclerViewAdapter());
                                } else {
                                    //처음 대화방을 만들고 채팅보낼때
                                    ChatModel.Comment comment = new ChatModel.Comment();
                                    comment.uid = uid;
                                    comment.message = editText.getText().toString();
                                    comment.timestamp = ServerValue.TIMESTAMP;
                                    FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").push().setValue(comment);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this));
                                    recyclerView.setAdapter(new RecyclerViewAdapter());
                                    sendFcm();
                                    editText.setText("");
                                    button.setEnabled(true);

                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener); // 이벤트 제거.
        }
        Intent intent = new Intent(MessageActivity.this, MainActivity.class);
        intent.putExtra("MessageActivity", "MessageActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder> {

        List<ChatModel.Comment> comments;

        public RecyclerViewAdapter() {
            comments = new ArrayList<>();
            getMessageList();
        }

        void getMessageList() {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments");
            valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    comments.clear();

                    Map<String, Object> readUsersMap = new HashMap<>();

                    for (DataSnapshot item : dataSnapshot.getChildren()) {// item = ChatModel.Comment
                        String key = item.getKey();
                        ChatModel.Comment comment_origin = item.getValue(ChatModel.Comment.class);
                        ChatModel.Comment comment_modify = item.getValue(ChatModel.Comment.class);
                        comment_modify.readUsers.put(uid, true);
                        readUsersMap.put(key, comment_modify);
                        comments.add(comment_origin);
                    }
                    //25강 버그잡는부분 무한루프돈다고 하는데?
                    if (!comments.get(comments.size() - 1).readUsers.containsKey(uid)) {
                        FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("comments").updateChildren(readUsersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
        public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
            return new MessageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

            if (comments.get(position).uid.equals(uid)) {//내가보낸메세지
                holder.textView_message.setText(comments.get(position).message);
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble);
                holder.linearLayout_to.setVisibility(View.INVISIBLE);
                holder.linearlayout_main.setGravity(Gravity.RIGHT);
                setReadCounter(position, holder.textView_readCounter_left);
            } else {//상대방이보낸메세지
                Glide.with(holder.itemView.getContext()).load(user.getUserProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(holder.imageView_profile);
                holder.textView_name.setText(user.getUserName());
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

        void setReadCounter(final int position, final TextView textView) { // 읽음 표시하는부분
            textView.setVisibility(View.INVISIBLE);
            //서버에 무리가 가서 이런식으로 peopleCount를 만들었다고함.
            //만들기 전에는 메세지 수만큼 인원수를 서버에 물어본다고함.
            if (peopleCount == 0) {
                FirebaseDatabase.getInstance().getReference().child("chatrooms").child(chatRoomUid).child("users").addListenerForSingleValueEvent(new ValueEventListener() {
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

        @Override
        public int getItemCount() {
            return comments.size();
        }


        private class MessageViewHolder extends RecyclerView.ViewHolder {
            public TextView textView_message;
            public TextView textView_name;
            public ImageView imageView_profile;
            public LinearLayout linearLayout_to;
            public LinearLayout linearlayout_main;
            public TextView textView_timestamp;
            public TextView textView_readCounter_left;
            public TextView textView_readCounter_right;

            public MessageViewHolder(View view) {
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
}
