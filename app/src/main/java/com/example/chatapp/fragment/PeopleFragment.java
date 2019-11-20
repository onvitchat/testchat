package com.example.chatapp.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PeopleFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people,container,false);
        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new PeopleFragmentRecyclerAdapter());
        return view;
    }

    class PeopleFragmentRecyclerAdapter extends RecyclerView.Adapter<PeopleFragmentRecyclerAdapter.CustomViewHolder>{

        List<User> userList;
        public PeopleFragmentRecyclerAdapter() {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend,parent,false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
            Glide.with(holder.itemView.getContext()).load(userList.get(position).getUserProfileImageUrl()).apply(new RequestOptions().circleCrop()).into(holder.imageView);
            holder.textView.setText(userList.get(position).getUserName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), MessageActivity.class);
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(),R.anim.fromright,R.anim.toleft); // 화면나오는방향설정
                    startActivity(intent,activityOptions.toBundle());
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
            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
            }
        }
    }

}
