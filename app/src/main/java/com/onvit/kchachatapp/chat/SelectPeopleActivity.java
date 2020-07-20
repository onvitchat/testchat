package com.onvit.kchachatapp.chat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.model.ChatModel;
import com.onvit.kchachatapp.model.Img;
import com.onvit.kchachatapp.model.LastChat;
import com.onvit.kchachatapp.model.User;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectPeopleActivity extends AppCompatActivity {
    List<User> userlist;
    private ValueEventListener valueEventListener;
    private List<User> selectUserList = new ArrayList<>();
    private PeopleFragmentRecyclerAdapter pf = new PeopleFragmentRecyclerAdapter();
    private PlusPeopleRecyclerAdapter plusPeopleRecyclerAdapter;
    private String uid;
    private ArrayList<User> pList = new ArrayList<>();
    private Button b;
    private EditText e;
    private DatabaseReference databaseReference;
    private Map<String, User> allUsers = new HashMap<>();
    private String toRoom;
    private RecyclerView plusRecyclerView;
    private ImageView back_arrow;
    private TextView chat_p_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);
        uid = UserMap.getUid();

        back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        chat_p_count = findViewById(R.id.chat_p_count);
        RecyclerView recyclerView = findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(pf);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        b = findViewById(R.id.create_chat);
        e = findViewById(R.id.chat_name);
        allUsers = UserMap.getInstance();
        if (getIntent().getStringExtra("plus") == null) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String chatName = e.getText().toString().trim();
                    if (chatName.equals("")) {
                        Utiles.customToast(SelectPeopleActivity.this, "채팅방이름을 입력해주세요.").show();
                        return;
                    }
                    if (chatName.contains(".") || chatName.contains("#") || chatName.contains("$") || chatName.contains("[") || chatName.contains("]")) {
                        Utiles.customToast(SelectPeopleActivity.this, "'.' '#' '$' '[',']'는 사용할 수 없습니다.").show();
                        return;
                    }
                    final AlertDialog d = Utiles.createLoadingDialog(SelectPeopleActivity.this, "채팅방을 생성하는 중입니다.");
                    databaseReference.child("groupChat").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            d.dismiss();
                            List<Integer> count = new ArrayList<>();
                            for (DataSnapshot i : dataSnapshot.getChildren()) {
                                if (chatName.equals(i.getKey())) {
                                    Utiles.customToast(SelectPeopleActivity.this, "중복된 채팅방이름이 존재합니다.").show();
                                    return;
                                }
                                ChatModel chatModel = i.getValue(ChatModel.class);
                                count.add(chatModel.id);
                            }
                            Collections.sort(count);

                            int c = 3;
                            if (count.size() > 0) {
                                c = count.get(count.size() - 1) + 1;
                            }
                            pList.add(allUsers.get(uid));
                            ChatModel chatModel = new ChatModel();
                            LastChat lastChat = new LastChat();
                            lastChat.setChatName(chatName);
                            String message = "";
                            Map<String, LastChat.timeInfo> existUser = new HashMap<>();
                            for (User u : pList) {
                                LastChat.timeInfo timeInfo = new LastChat.timeInfo();
                                timeInfo.setUnReadCount(0);
                                timeInfo.setInitTime(System.currentTimeMillis());
                                timeInfo.setExitTime(System.currentTimeMillis());
                                chatModel.users.put(u.getUid(), false);
                                existUser.put(u.getUid(), timeInfo);
                                message = message + String.format("%s(%s)님, ", u.getUserName(), u.getHospital());
                            }
                            final String message2 = message.substring(0, message.length() - 2) + "이 채팅방에 참여하였습니다.";
                            chatModel.id = c;
                            lastChat.setExistUsers(existUser);
                            pList.remove(allUsers.get(uid));
                            databaseReference.child("groupChat").child(chatName).setValue(chatModel);
                            databaseReference.child("lastChat").child(chatName).setValue(lastChat).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    List<Img> img_list = new ArrayList<>();
                                    Utiles.customToast(SelectPeopleActivity.this, "채팅방을 생성하였습니다.").show();
                                    ChatModel.Comment comment = new ChatModel.Comment();
                                    comment.uid = uid;
                                    comment.message = message2;
                                    comment.timestamp = System.currentTimeMillis();
                                    comment.type = "io";
                                    comment.unReadCount = 0;
                                    databaseReference.child("groupChat").child(chatName).child("comments").push().setValue(comment);
                                    Intent intent = new Intent(SelectPeopleActivity.this, GroupMessageActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("toRoom", chatName); // 방이름
                                    intent.putExtra("chatCount", 0);// 채팅숫자
                                    intent.putParcelableArrayListExtra("userInfo", pList);
                                    UserMap.getComments().clear();
                                    intent.putParcelableArrayListExtra("imgList", (ArrayList<? extends Parcelable>) img_list);
                                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(SelectPeopleActivity.this, R.anim.frombottom, R.anim.totop);

                                    startActivity(intent, activityOptions.toBundle());
                                    finish();
                                }
                            });

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        } else {
            userlist = getIntent().getParcelableArrayListExtra("userlist");
            toRoom = getIntent().getStringExtra("room");
            String chatName = getIntent().getStringExtra("room");
            e.setText(chatName);
            e.setFocusable(false);
            e.setClickable(false);
            b.setText("초대");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pList.size() == 0) {
                        Utiles.customToast(SelectPeopleActivity.this, "초대할 인원을 선택하세요.").show();
                        return;
                    }
                    String message = "";
                    if (allUsers.get(uid) != null) {
                        message = String.format("%s(%s)님이 ", allUsers.get(uid).getUserName(), allUsers.get(uid).getHospital());
                    }
                    Map<String, Object> map = new HashMap<>();
                    Map<String, Object> map2 = new HashMap<>();
                    LastChat.timeInfo timeInfo = new LastChat.timeInfo();
                    timeInfo.setUnReadCount(0);
                    timeInfo.setExitTime(System.currentTimeMillis());
                    timeInfo.setInitTime(System.currentTimeMillis());
                    for (User u : pList) {
                        map.put("users/" + u.getUid(), false);
                        map2.put("existUsers/" + u.getUid(), timeInfo);
                        message = message + String.format("%s(%s)님, ", u.getUserName(), u.getHospital());
                    }
                    message = message.substring(0, message.length() - 2) + "을 초대하였습니다.";
                    databaseReference.child("groupChat").child(getIntent().getStringExtra("room")).updateChildren(map);
                    ChatModel.Comment comment = new ChatModel.Comment();
                    Date date = new Date();
                    comment.uid = uid;
                    comment.message = message;
                    comment.timestamp = date.getTime();
                    comment.type = "io";
                    comment.unReadCount = 0;
                    databaseReference.child("groupChat").child(toRoom).child("comments").push().setValue(comment);
                    databaseReference.child("lastChat").child(getIntent().getStringExtra("room")).updateChildren(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }
            });
        }

        plusRecyclerView = findViewById(R.id.plus_p_recycler_view);
        plusPeopleRecyclerAdapter = new PlusPeopleRecyclerAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SelectPeopleActivity.this, RecyclerView.HORIZONTAL, false);
        //리사이클러뷰 역정렬
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        plusRecyclerView.setLayoutManager(linearLayoutManager);
        plusRecyclerView.setAdapter(plusPeopleRecyclerAdapter);
    }

    //뒤로가기 눌렀을때
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromright, R.anim.toleft);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").removeEventListener(valueEventListener); // 이벤트 제거.
        }
    }

    class PeopleFragmentRecyclerAdapter extends RecyclerView.Adapter<PeopleFragmentRecyclerAdapter.CustomViewHolder> {

        public PeopleFragmentRecyclerAdapter() {
            valueEventListener = new ValueEventListener() { // Users데이터의 변화가 일어날때마다 콜백으로 호출됨.
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // 가입한 유저들의 정보를 가지고옴.
                    selectUserList.clear();
                    User user = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        user = snapshot.getValue(User.class);
                        if (user.getUid().equals(uid)) {
                            continue;
                        }
                        selectUserList.add(user);
                    }
                    if (getIntent().getStringExtra("plus") != null) {
                        for (User u : userlist) {
                            selectUserList.remove(u);
                        }
                    }
                    // 유저들의 정보를 가나순으로 정렬하고 자신의 정보는 첫번째에 넣음.
                    Collections.sort(selectUserList);
                    notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(valueEventListener);
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {
            //position0번 부터 붙음
            holder.check.setVisibility(View.VISIBLE);
            holder.check.setChecked(false);

            if (pList.contains(selectUserList.get(position))) {
                holder.check.setChecked(true);
            }

            holder.lineText.setVisibility(View.GONE);

            //사진에 곡률넣음.
            if (selectUserList.get(position).getUserProfileImageUrl().equals("noImg")) {
                Glide.with(holder.itemView.getContext()).load(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            } else {
                Glide.with(holder.itemView.getContext()).load(selectUserList.get(position).getUserProfileImageUrl()).placeholder(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            }
            GradientDrawable gradientDrawable = (GradientDrawable) SelectPeopleActivity.this.getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);

            holder.textView.setText(selectUserList.get(position).getUserName());

            holder.textView_hospital.setText("[" + selectUserList.get(position).getHospital() + "]");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.check.isChecked()) {
                        holder.check.setChecked(false);
                        pList.remove(selectUserList.get(position));
                        plusPeopleRecyclerAdapter.notifyDataSetChanged();
                        chat_p_count.setText(pList.size() + "명");
                        if (pList.size() == 0) {
                            chat_p_count.setText("");
                            plusRecyclerView.setVisibility(View.GONE);
                        }
                    } else {
                        holder.check.setChecked(true);
                        plusRecyclerView.setVisibility(View.VISIBLE);
                        pList.add(selectUserList.get(position));
                        plusPeopleRecyclerAdapter.notifyDataSetChanged();
                        plusRecyclerView.scrollToPosition(plusPeopleRecyclerAdapter.getItemCount() - 1);
                        chat_p_count.setText(pList.size() + "명");
                    }
                }
            });
            holder.check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.check.isChecked()) {
                        plusRecyclerView.setVisibility(View.VISIBLE);
                        pList.add(selectUserList.get(position));
                        plusPeopleRecyclerAdapter.notifyDataSetChanged();
                        plusRecyclerView.scrollToPosition(plusPeopleRecyclerAdapter.getItemCount() - 1);
                        chat_p_count.setText(pList.size() + "명");
                    } else {
                        pList.remove(selectUserList.get(position));
                        plusPeopleRecyclerAdapter.notifyDataSetChanged();
                        chat_p_count.setText(pList.size() + "명");
                        if (pList.size() == 0) {
                            chat_p_count.setText("");
                            plusRecyclerView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return selectUserList.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private TextView textView;
            private TextView textView_hospital;
            private TextView lineText;
            private CheckBox check;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textView_hospital = view.findViewById(R.id.frienditem_textview_hospital);
                lineText = view.findViewById(R.id.line_text);
                check = view.findViewById(R.id.check);
            }
        }
    }


    class PlusPeopleRecyclerAdapter extends RecyclerView.Adapter<PlusPeopleRecyclerAdapter.CustomViewHolder> {

        public PlusPeopleRecyclerAdapter() {

        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plus_people, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final CustomViewHolder holder, final int position) {
            String uri = pList.get(position).getUserProfileImageUrl();
            if (uri.equals("noImg")) {
                Glide.with(holder.itemView.getContext()).load(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.profile);
            } else {
                Glide.with(holder.itemView.getContext()).load(uri).placeholder(R.drawable.standard_profile)
                        .apply(new RequestOptions().centerCrop()).into(holder.profile);
            }
            GradientDrawable gradientDrawable = (GradientDrawable) SelectPeopleActivity.this.getDrawable(R.drawable.radius);
            holder.profile.setBackground(gradientDrawable);
            holder.profile.setClipToOutline(true);

            holder.name.setText(pList.get(position).getUserName());
            holder.hospital.setText(pList.get(position).getHospital());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pList.remove(position);
                    notifyDataSetChanged();
                    chat_p_count.setText(pList.size() + "명");
                    if (pList.size() == 0) {
                        chat_p_count.setText("");
                        plusRecyclerView.setVisibility(View.GONE);
                    }
                    pf.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return pList.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView cancel, profile;
            TextView name, hospital;

            public CustomViewHolder(View v) {
                super(v);
                cancel = v.findViewById(R.id.cancel_view);
                profile = v.findViewById(R.id.image_view_profile);
                name = v.findViewById(R.id.text_view_name);
                hospital = v.findViewById(R.id.text_view_hospital);
            }
        }
    }
}
