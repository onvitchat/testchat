package com.onvit.kchachatapp.chat;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.chat.vote.VoteListActivity;
import com.onvit.kchachatapp.contact.PersonInfoActivity;
import com.onvit.kchachatapp.model.ChatModel;
import com.onvit.kchachatapp.model.Img;
import com.onvit.kchachatapp.model.User;
import com.onvit.kchachatapp.model.Vote;
import com.onvit.kchachatapp.util.UserMap;
import com.onvit.kchachatapp.util.Utiles;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChatSetInfoActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout chat_info_linear_layout;
    RecyclerView recyclerView;
    TextView vote, file, img, plus, out;
    ArrayList<User> userList;
    private String uid;
    private String toRoom;
    private List<String> deleteKey = new ArrayList<>();
    private List<String> deleteKey2 = new ArrayList<>();
    private Map<String, User> users = new HashMap<>();
    private List<Img> img_list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_set_info);

        uid = UserMap.getUid();
        toRoom = getIntent().getStringExtra("room");
        chat_info_linear_layout = findViewById(R.id.chat_info_linear_layout);
        recyclerView = findViewById(R.id.peopleinfo_recyclerview);
        plus = findViewById(R.id.plus_ps);
        vote = findViewById(R.id.vote);
        file = findViewById(R.id.file);
        img = findViewById(R.id.img);
        out = findViewById(R.id.out);
        userList = getIntent().getParcelableArrayListExtra("userInfo");
        users = UserMap.getInstance();
        User myInfo = users.get(uid);
        img_list = getIntent().getParcelableArrayListExtra("imgList");
        Collections.sort(userList);
        userList.add(0, myInfo);

        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.gravity = Gravity.TOP | Gravity.END;


        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        getWindow().getAttributes().width = (int) (dm.widthPixels * 0.7);

        this.setFinishOnTouchOutside(true);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new PeopleInfoRecyclerAdapter(userList));

        vote.setOnClickListener(this);
        file.setOnClickListener(this);
        img.setOnClickListener(this);
        plus.setOnClickListener(this);
        out.setOnClickListener(this);
        plus.setText("대화멤버 " + userList.size());
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);//화면 사라지는 방향
    }

    @Override
    protected void onResume() {
        super.onResume();
        chat_info_linear_layout.setBackgroundColor(Color.WHITE);
        getIntent().removeExtra("on");
        Map<String, Object> map = new HashMap<>();
        map.put(uid, true);
        FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).child("users").updateChildren(map);
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (getIntent().getStringExtra("on") == null) {
            Map<String, Object> map = new HashMap<>();
            map.put(uid, false);
            FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).child("users").updateChildren(map);
            FirebaseDatabase.getInstance().getReference().child("lastChat").child(toRoom).child("existUsers").child(uid).child("exitTime").setValue(System.currentTimeMillis());
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.vote:
                intent = new Intent(ChatSetInfoActivity.this, VoteListActivity.class);
                intent.putExtra("room", getIntent().getStringExtra("room"));
                intent.putParcelableArrayListExtra("userList", userList);
                getIntent().putExtra("on", "on");
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                break;
            case R.id.file:
                intent = new Intent(ChatSetInfoActivity.this, FileActivity.class);
                intent.putExtra("room", getIntent().getStringExtra("room"));
                getIntent().putExtra("on", "on");
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                break;
            case R.id.img:
                intent = new Intent(ChatSetInfoActivity.this, ImgActivity.class);
                intent.putExtra("room", getIntent().getStringExtra("room"));
                intent.putParcelableArrayListExtra("userlist", userList);
                intent.putParcelableArrayListExtra("imgList", (ArrayList<? extends Parcelable>) img_list);
                getIntent().putExtra("on", "on");
                startActivity(intent);
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                break;
            case R.id.plus_ps:
                intent = new Intent(ChatSetInfoActivity.this, SelectPeopleActivity.class);
                intent.putExtra("room", getIntent().getStringExtra("room"));
                intent.putParcelableArrayListExtra("userlist", userList);
                intent.putExtra("plus", "plus");
                intent.putExtra("room", toRoom);
                getIntent().putExtra("on", "on");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fromleft, R.anim.toright);
                break;
            case R.id.out:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("채팅방의 모든 내용이 삭제됩니다.\n정말 나가시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final AlertDialog deleteDialog = Utiles.createLoadingDialog(ChatSetInfoActivity.this, "채팅내역을 삭제하는 중입니다.");
                        final Map<String, Object> map = new HashMap<>();
                        final Map<String, Object> map2 = new HashMap<>();
                        final Map<String, Object> voteMap = new HashMap<>();
                        FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).child("comments")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for (DataSnapshot i : dataSnapshot.getChildren()) {
                                            ChatModel.Comment comment = i.getValue(ChatModel.Comment.class);
                                            if (comment.getType().equals("img") || comment.getType().length() > 10) {
                                                deleteKey.add(i.getKey());
                                            }
                                            if (comment.getType().equals("file")) {
                                                int a = comment.getMessage().lastIndexOf("https");
                                                int b = comment.getMessage().substring(0, a).lastIndexOf(".");
                                                String ext = comment.getMessage().substring(0, a).substring(b + 1);
                                                deleteKey2.add(i.getKey() + "." + ext);
                                            }
                                        }
                                        map.put("users/" + uid, null);
                                        map2.put("existUsers/" + uid, null);
                                        voteMap.put(uid, null);

                                        FirebaseDatabase.getInstance().getReference().child("Vote").child(toRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot i : dataSnapshot.getChildren()) {
                                                    Vote vote = i.getValue(Vote.class);
                                                    Set<String> keys = vote.getContent().keySet();
                                                    Iterator<String> it = keys.iterator();
                                                    while (it.hasNext()) {
                                                        String key = it.next();
                                                        FirebaseDatabase.getInstance().getReference().child("Vote").child(toRoom).child(i.getKey())
                                                                .child("content").child(key).updateChildren(voteMap);
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                        FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                FirebaseDatabase.getInstance().getReference().child("lastChat").child(toRoom).updateChildren(map2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                ChatModel chatModel = dataSnapshot.getValue(ChatModel.class);
                                                                if (chatModel.users == null || chatModel.users.size() == 0) {
                                                                    FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).setValue(null);
                                                                    FirebaseDatabase.getInstance().getReference().child("lastChat").child(toRoom).setValue(null);
                                                                    for (String d : deleteKey) {
                                                                        FirebaseStorage.getInstance().getReference().child("Image Files").child(toRoom).child(d).delete();
                                                                    }
                                                                    for (String d : deleteKey2) {
                                                                        FirebaseStorage.getInstance().getReference().child("Document Files").child(toRoom).child(d).delete();
                                                                    }
                                                                    FirebaseDatabase.getInstance().getReference().child("Vote").child(toRoom).setValue(null);
                                                                } else {
                                                                    ChatModel.Comment comment = new ChatModel.Comment();

                                                                    comment.uid = uid;
                                                                    comment.message = String.format("%s(%s)님이 나갔습니다.", users.get(uid).getUserName(), users.get(uid).getHospital());
                                                                    comment.timestamp = new Date().getTime();
                                                                    comment.type = "io";
                                                                    comment.unReadCount = 0;
                                                                    FirebaseDatabase.getInstance().getReference().child("groupChat").child(toRoom).child("comments").push().setValue(comment);
                                                                }
                                                                setResult(99);
                                                                deleteDialog.dismiss();
                                                                finish();
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

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }).setNegativeButton("아니오", null);
                AlertDialog a = builder.create();
                a.show();
                break;
        }
    }

    class PeopleInfoRecyclerAdapter extends RecyclerView.Adapter<PeopleInfoRecyclerAdapter.CustomViewHolder> {
        ArrayList<User> userlist;

        private PeopleInfoRecyclerAdapter(ArrayList<User> userlist) {
            this.userlist = userlist;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_small, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            Log.d("홀더붙는순서(연락처)", position + "");
            //position0번 부터 붙음

            holder.lineText.setVisibility(View.GONE);
            if (position == 1) {// 본인이랑 다음사람이랑 구분선.
                holder.lineText.setVisibility(View.VISIBLE);
            }
            //사진에 곡률넣음.
            if (userlist.get(position).getUserProfileImageUrl().equals("noImg")) {
                Glide.with(holder.itemView.getContext()).load(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            } else {
                Glide.with(holder.itemView.getContext()).load(userlist.get(position).getUserProfileImageUrl()).placeholder(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            }
            GradientDrawable gradientDrawable = (GradientDrawable) ChatSetInfoActivity.this.getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);

            holder.textView.setText(userlist.get(position).getUserName());

            holder.textView_hospital.setText("[" + userlist.get(position).getHospital() + "]");

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(ChatSetInfoActivity.this, PersonInfoActivity.class);
                    intent.putExtra("info", userlist.get(position).getUid());
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userlist.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
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
