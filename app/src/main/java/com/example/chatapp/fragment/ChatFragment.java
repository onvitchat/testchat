package com.example.chatapp.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.chat.GroupMessageActivity;
import com.example.chatapp.chat.MessageActivity;
import com.example.chatapp.model.ChatModel;
import com.example.chatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

public class ChatFragment extends Fragment {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.chatfragment_recyclerview);
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter();
        recyclerView.setAdapter(chatRecyclerViewAdapter);
        chatRecyclerViewAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        return view;
    }


    class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ChatViewHolder> {

        private List<ChatModel> chatModels = new ArrayList<>();
        private List<String> keys = new ArrayList<>();
        private String uid;
        private ArrayList<String> toUsers = new ArrayList<>();

        public ChatRecyclerViewAdapter() {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();   // 클라이언트uid

            //해당하는방접속
            //FirebaseDatabase 중 chatroosms에 접근. chatrooms의 하위요소중 users안의 해당 uid중 값이 true인것들을 정렬시킴. 오름차순 및 사전순으로 정렬됨.
            // => 즉, 클라이언트가 속한 채팅방들을 정렬시킨다.
            FirebaseDatabase.getInstance().getReference().child("chatrooms").orderByChild("users/" + uid).equalTo(true)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) { // 해당되는 chatrooms들의 키값들이 넘어옴.
                            chatModels.clear();
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                chatModels.add(item.getValue(ChatModel.class));
                                keys.add(item.getKey());
                            }
                            Collections.reverse(chatModels); // 최신순으로 정렬
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }

        @NonNull
        @Override
        public ChatRecyclerViewAdapter.ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ChatRecyclerViewAdapter.ChatViewHolder holder, final int position) {
            String toUid = null;
            //채팅방에 있는 유저들의 uid를 가지고 옴.
            for (String user : chatModels.get(position).users.keySet()) {
                if (!user.equals(uid)) {
                    toUid = user;
                    toUsers.add(toUid);
                }
            }
            final User[] user = {null};
            FirebaseDatabase.getInstance().getReference().child("Users").child(toUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //각 채팅방에 상대 사진과 이름을 표시
                    user[0] = dataSnapshot.getValue(User.class);
                    Glide.with(holder.itemView.getContext()).load(user[0].getUserProfileImageUrl()).apply(new RequestOptions().centerCrop()).into(holder.imageView);
                    GradientDrawable gradientDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.radius);
                    holder.imageView.setBackground(gradientDrawable);
                    holder.imageView.setClipToOutline(true);
                    holder.textView_title.setText(user[0].getUserName());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //메세지를 내림 차순으로 정렬 후 마지막 메세지의 키 값을 가져옴.
            Map<String, ChatModel.Comment> commentMap = new TreeMap<>(Collections.<String>reverseOrder());//역순으로 정렬시킨다.
            commentMap.putAll(chatModels.get(position).comments); // 현재 채팅방의 comments를 시간의 역순으로 정렬. firebasedatabase의 데이터는 시간을 기반으로 정렬되어있음.
            if (commentMap.size() == 0 || commentMap == null) {
                //채팅방만 만들어져있고 채팅내용이 없는 경우.
            } else {
                String lastMessageKey = (String) commentMap.keySet().toArray()[0];
                holder.textView_last_message.setText(chatModels.get(position).comments.get(lastMessageKey).message);
                //채팅방목록에 마지막 메세지 시간 표시
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                long unixTime = (long) chatModels.get(position).comments.get(lastMessageKey).timestamp;
                Date date = new Date(unixTime);
                holder.textView_timestamp.setText(simpleDateFormat.format(date));
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = null;
                    if (chatModels.get(position).users.size() > 2) {
                        intent = new Intent(view.getContext(), GroupMessageActivity.class);
                        intent.putExtra("toRoom",keys.get(position));
                    } else {
                        intent = new Intent(view.getContext(), MessageActivity.class);
                        intent.putExtra("uid", user[0]);
                    }
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());

                }
            });


        }

        @Override
        public int getItemCount() {
            return chatModels.size();
        }

        private class ChatViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView_title;
            public TextView textView_last_message;
            public TextView textView_timestamp;

            public ChatViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.chatitem_imageview);
                textView_title = view.findViewById(R.id.chatitem_textview_title);
                textView_last_message = view.findViewById(R.id.chatitem_textview_lastMessage);
                textView_timestamp = view.findViewById(R.id.chatitem_textview_timestamp);
            }
        }
    }


}
