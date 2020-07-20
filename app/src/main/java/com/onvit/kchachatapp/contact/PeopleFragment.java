package com.onvit.kchachatapp.contact;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onvit.kchachatapp.MainActivity;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.model.User;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PeopleFragment extends Fragment {
    private ValueEventListener valueEventListener;
    private List<User> userList;
    private PeopleFragmentRecyclerAdapter pf;

    public PeopleFragment() {
        userList = UserMap.getUserList();
        pf = new PeopleFragmentRecyclerAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_people, container, false);
        Toolbar chatToolbar = view.findViewById(R.id.chat_toolbar);
        AppCompatActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(chatToolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("연락처 목록");
            }
        }
        final RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(pf);
        valueEventListener = new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pf.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(valueEventListener);
        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").removeEventListener(valueEventListener);
        }
    }

    class PeopleFragmentRecyclerAdapter extends RecyclerView.Adapter<PeopleFragmentRecyclerAdapter.CustomViewHolder> {

        private PeopleFragmentRecyclerAdapter() {
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            //position0번 부터 붙음
            holder.lineText.setVisibility(View.GONE);

            if (position == 1) {// 본인이랑 다음사람이랑 구분선.
                holder.lineText.setVisibility(View.VISIBLE);
            }
            //사진에 곡률넣음.
            if (userList.get(position).getUserProfileImageUrl().equals("noImg")) {
                Glide.with(holder.itemView.getContext()).load(R.drawable.standard_profile).into(holder.imageView);
            } else {
                Glide.with(holder.itemView.getContext()).load(userList.get(position).getUserProfileImageUrl())
                        .placeholder(R.drawable.standard_profile).into(holder.imageView);
            }
            holder.textView.setText(userList.get(position).getUserName());
            String hospital = userList.get(position).getHospital();
            holder.textView_hospital.setText(hospital);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //중복클릭방지
                    if (Utiles.blockDoubleClick()) {
                        return;
                    }
                    Intent intent = new Intent(getContext(), PersonInfoActivity.class);
                    intent.putExtra("info", userList.get(position).getUid());
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView imageView;
            private TextView textView;
            private TextView textView_hospital;
            private TextView lineText;

            private CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textView_hospital = view.findViewById(R.id.frienditem_textview_hospital);
                lineText = view.findViewById(R.id.line_text);
            }
        }
    }
}
