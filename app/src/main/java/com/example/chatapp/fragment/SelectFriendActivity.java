package com.example.chatapp.fragment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.R;
import com.example.chatapp.chat.MessageActivity;
import com.example.chatapp.model.ChatModel;
import com.example.chatapp.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity {
    ChatModel chatModel = new ChatModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_friend);
        RecyclerView recyclerView = findViewById(R.id.selectFriendActivity_recyclerview);
        recyclerView.setAdapter(new SelectFriendtRecyclerAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button button = findViewById(R.id.selectFriendActivity_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                chatModel.users.put(myUid, true);
                FirebaseDatabase.getInstance().getReference().child("chatrooms").push().setValue(chatModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

            }
        });
    }

    class SelectFriendtRecyclerAdapter extends RecyclerView.Adapter<SelectFriendtRecyclerAdapter.CustomViewHolder>{

        List<User> userList;
        public SelectFriendtRecyclerAdapter() {
            userList = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    userList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(snapshot.getValue(User.class).getUid())){
                            continue;
                        }
                        userList.add(snapshot.getValue(User.class));
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_select,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            Glide.with(holder.itemView.getContext()).load(userList.get(position).getUserProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(holder.imageView);
            holder.textView.setText(userList.get(position).getUserName());
            if(userList.get(position).getComment()!=null){
                holder.textView_comment.setText(userList.get(position).getComment());
            }
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        chatModel.users.put(userList.get(position).getUid(), true);
                    }else{
                        chatModel.users.remove(userList.get(position));
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return userList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public CheckBox checkBox;
            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textView_comment = view.findViewById(R.id.frienditem_textview_comment);
                checkBox = view.findViewById(R.id.frienditem_checkbox);
            }
        }
    }
}
