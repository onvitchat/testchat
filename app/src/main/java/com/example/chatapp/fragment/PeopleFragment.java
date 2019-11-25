package com.example.chatapp.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.example.chatapp.chat.MessageActivity;
import com.example.chatapp.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PeopleFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerAdapter());

        FloatingActionButton floatingActionButton = view.findViewById(R.id.peoplefragment_floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), SelectFriendActivity.class));
            }
        });
        return view;
    }

    class PeopleFragmentRecyclerAdapter extends RecyclerView.Adapter<PeopleFragmentRecyclerAdapter.CustomViewHolder> {

        List<User> userList;

        public PeopleFragmentRecyclerAdapter() {
            userList = new ArrayList<>();
            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 가입한 유저들의 정보를 가지고옴.
                    userList.clear();
                    User user = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(snapshot.getValue(User.class).getUid())) {
                            user = snapshot.getValue(User.class);
                            continue;
                        }
                        userList.add(snapshot.getValue(User.class));
                    }
                    // 유저들의 정보를 가나순으로 정렬하고 자신의 정보는 첫번째에 넣음.
                    Collections.sort(userList);
                    userList.add(0, user);
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {

            if(position==1){// 본인이랑 다음사람이랑 구분선.
                holder.lineText.setVisibility(View.VISIBLE);
            }
            holder.textView_comment.setVisibility(View.GONE);
            //사진에 곡률넣음.
            Glide.with(holder.itemView.getContext()).load(userList.get(position).getUserProfileImageUrl()).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            GradientDrawable gradientDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);

            holder.textView.setText(userList.get(position).getUserName());
            //사람클릭하면 채팅방으로 이동.
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //채팅상대의 uid를 가지고 넘어감.
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    intent.putExtra("uid", userList.get(position));
//                    intent.putExtra("uid", userList.get(position).getUid());
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft); // 화면나오는방향설정
                    startActivity(intent, activityOptions.toBundle());
                }
            });
            if (userList.get(position).getComment() != null) {
                holder.textView_comment.setText(userList.get(position).getComment());
                holder.textView_comment.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public TextView textView_comment;
            public TextView lineText;
            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textView_comment = view.findViewById(R.id.frienditem_textview_comment);
                lineText = view.findViewById(R.id.line_text);
            }
        }
    }
}
