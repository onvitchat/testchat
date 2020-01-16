package com.onvit.chatapp.notice;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.onvit.chatapp.MainActivity;
import com.onvit.chatapp.notice.NoticeActivity;
import com.onvit.chatapp.R;
import com.onvit.chatapp.model.Notice;
import com.onvit.chatapp.model.User;
import com.vlk.multimager.utils.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class NoticeFragment extends Fragment {
    private AppCompatActivity activity;
    private Toolbar chatToolbar;
    private List<Notice> noticeLists = new ArrayList<>();
    private DatabaseReference firebaseDatabase;
    private String uid;
    private NoticeFragmentRecyclerAdapter noticeFragmentRecyclerAdapter;
    private RecyclerView recyclerView;
    private ArrayList<String> registration_ids = new ArrayList<>();

    public NoticeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        chatToolbar = view.findViewById(R.id.chat_toolbar);
        activity = (MainActivity) getActivity();
        activity.setSupportActionBar(chatToolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        NotificationManagerCompat.from(activity).cancel("notice", 0);
        NotificationManagerCompat.from(activity).cancel(2);


        recyclerView = view.findViewById(R.id.fragment_notice_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        noticeFragmentRecyclerAdapter = new NoticeFragmentRecyclerAdapter(noticeLists);
        recyclerView.setAdapter(noticeFragmentRecyclerAdapter);

        firebaseDatabase.child("Notice").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noticeLists.clear();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    Notice noticeList = item.getValue(Notice.class);
                    noticeList.setCode(item.getKey());
                    noticeLists.add(noticeList);
                }
                Collections.reverse(noticeLists);
                noticeFragmentRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        firebaseDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    User user = item.getValue(User.class);

                    Log.d("메세지", user.toString());
                    if (user.getUid().equals(uid)) {
                        continue;
                    }
                    registration_ids.add(user.getPushToken());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        actionBar.setTitle("공지사항");

        FloatingActionButton make = view.findViewById(R.id.plus_notice);
        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NoticeActivity.class);
                intent.putExtra("insert", "insert");
                intent.putStringArrayListExtra("userList", registration_ids);
                ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                startActivity(intent, activityOptions.toBundle());
            }
        });

        return view;
    }

    class NoticeFragmentRecyclerAdapter extends RecyclerView.Adapter<NoticeFragmentRecyclerAdapter.NoticeViewHolder> {
        List<Notice> noticeList;

        public NoticeFragmentRecyclerAdapter(List<Notice> list) {
            noticeList = list;
        }

        @NonNull
        @Override
        public NoticeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent, false);
            return new NoticeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NoticeViewHolder holder, final int position) {
            holder.time.setVisibility(View.VISIBLE);
            if (position > 0) {
                if (noticeList.get(position).getTime().equals(noticeList.get(position - 1).getTime())) {
                    holder.time.setVisibility(View.GONE);
                }
            }
            holder.delete.setVisibility(View.INVISIBLE);
            holder.update.setVisibility(View.INVISIBLE);
            holder.name.setText(noticeList.get(position).getName());
            holder.title.setText(noticeList.get(position).getTitle());

            holder.time.setText(noticeList.get(position).getTime());

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), NoticeActivity.class);
                    intent.putExtra("view", "view");
                    intent.putExtra("title", noticeList.get(position).getTitle());
                    intent.putExtra("content", noticeList.get(position).getContent());
                    intent.putExtra("time", noticeList.get(position).getTime());
                    intent.putExtra("name", noticeList.get(position).getName());
                    if(noticeList.get(position).getImg()!=null){
                        ArrayList<String> list = new ArrayList<>();
                        Set<String> keys = noticeList.get(position).getImg().keySet();
                        for (String key1 : keys) {
                            Object value = noticeList.get(position).getImg().get(key1);
                            list.add(value.toString());
                        }
                        intent.putStringArrayListExtra("img", list);
                    }
                    ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                    startActivity(intent, activityOptions.toBundle());
                }
            });

            if (noticeList.get(position).getUid().equals(uid)) {
                holder.delete.setVisibility(View.VISIBLE);
                holder.update.setVisibility(View.VISIBLE);

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                        builder.setTitle("정말 삭제하시겠습니까?");
                        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Map<String, Object> map = new HashMap<>();
                                map.put(noticeList.get(position).getCode(), null);
                                if(noticeList.get(position).getImg()!=null && noticeList.get(position).getImg().get("noImg")==null){
                                    Set<String> keys = noticeList.get(position).getImg().keySet();
                                    Log.d("코드", noticeList.get(position).getCode()+"");
                                    for(String key : keys){
                                        Log.d("코드값", key);
                                        FirebaseStorage.getInstance().getReference().child("Notice Img").child(noticeList.get(position).getCode()).child(key).delete();
                                    }

                                }
                                firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), "삭제하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        builder.show();
                    }
                });
                holder.update.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ArrayList<Image> imgList = new ArrayList<>();
                        Intent intent = new Intent(getActivity(), NoticeActivity.class);
                        intent.putExtra("modify", "modify");
                        intent.putStringArrayListExtra("userList", registration_ids);
                        intent.putExtra("title", noticeList.get(position).getTitle());
                        intent.putExtra("content", noticeList.get(position).getContent());
                        intent.putExtra("code", noticeList.get(position).getCode());
                        if(noticeList.get(position).getImg()!=null){
                            ArrayList<String> list = new ArrayList<>();
                            ArrayList<String> deleteKey = new ArrayList<>();
                            Set<String> keys = noticeList.get(position).getImg().keySet();
                            for (String key1 : keys) {
                                if(key1.equals("noImg")){
                                    break;
                                }else{
                                    long id = Long.parseLong(key1);
                                    Uri uri = Uri.parse(noticeList.get(position).getImg().get(key1));
                                    String path = "path";
                                    boolean b = true;
                                    Image image = new Image(id,uri,path,b);
                                    imgList.add(image);
                                    Object value = noticeList.get(position).getImg().get(key1);
                                    list.add(value.toString());
                                    deleteKey.add(id+"");
                                }

                            }
                            intent.putStringArrayListExtra("img", list);
                            intent.putStringArrayListExtra("deleteKey", deleteKey);
                            intent.putParcelableArrayListExtra("imgList", imgList);
                        }
                        ActivityOptions activityOptions = ActivityOptions.makeCustomAnimation(view.getContext(), R.anim.fromright, R.anim.toleft);
                        startActivity(intent, activityOptions.toBundle());
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return noticeList.size();
        }


        private class NoticeViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView name;
            TextView time;
            ImageView delete;
            ImageView update;
            LinearLayout layout;
            public NoticeViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.notice_title);
                time = itemView.findViewById(R.id.notice_time);
                name = itemView.findViewById(R.id.notice_name);
                delete = itemView.findViewById(R.id.delete);
                update = itemView.findViewById(R.id.update);
                layout = itemView.findViewById(R.id.layout_title);
            }
        }
    }
}
