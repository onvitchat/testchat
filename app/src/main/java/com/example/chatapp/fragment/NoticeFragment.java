package com.example.chatapp.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.MainActivity;
import com.example.chatapp.PreferenceManager;
import com.example.chatapp.R;
import com.example.chatapp.model.NoticeList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoticeFragment extends Fragment {
    private AppCompatActivity activity;
    private Toolbar chatToolbar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private List<NoticeList> noticeLists = new ArrayList<>();
    private DatabaseReference firebaseDatabase;
    private String uid;
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

        RecyclerView recyclerView = view.findViewById(R.id.fragment_notice_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        final NoticeFragmentRecyclerAdapter noticeFragmentRecyclerAdapter = new NoticeFragmentRecyclerAdapter(noticeLists);
        recyclerView.setAdapter(noticeFragmentRecyclerAdapter);

        firebaseDatabase.child("Notice").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                noticeLists.clear();
                for(DataSnapshot item : dataSnapshot.getChildren()){
                    NoticeList noticeList = item.getValue(NoticeList.class);
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
        actionBar.setTitle("공지사항");

        FloatingActionButton make = view.findViewById(R.id.plus_notice);
        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = getLayoutInflater();
                View notice = inflater.inflate(R.layout.insert_notice, null);
                builder.setView(notice);
                final EditText editTextTitle = notice.findViewById(R.id.edit_title);
                final EditText editTextContent = notice.findViewById(R.id.edit_content);
                builder.setPositiveButton("등록", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String title = editTextTitle.getText().toString();
                        String content = editTextContent.getText().toString();
                        if (title.equals("") || content.equals("")) {
                            Toast.makeText(getContext(), "제목과 내용을 입력하시기 바랍니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        NoticeList notice = new NoticeList();
                        notice.setTitle(title);
                        notice.setContent(content);
                        notice.setUid(uid);
                        SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd");
                        Date date = new Date();
                        String newDate = sd.format(date);
                        notice.setTime(newDate);
                        notice.setTimestamp(date.getTime());
                        notice.setName(PreferenceManager.getString(getContext(),"name")+"("+PreferenceManager.getString(getContext(),"hospital")+")");
                        Log.d("등록", notice.toString());
                        firebaseDatabase.child("Notice").push().setValue(notice);

                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }
        });

        return view;
    }

    class NoticeFragmentRecyclerAdapter extends RecyclerView.Adapter<NoticeFragmentRecyclerAdapter.NoticeViewHolder> {
        List<NoticeList> noticeList;

        public NoticeFragmentRecyclerAdapter(List<NoticeList> list) {
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
            holder.content.setVisibility(View.GONE);
            holder.delete.setVisibility(View.INVISIBLE);
            holder.update.setVisibility(View.INVISIBLE);
            holder.name.setText(noticeList.get(position).getName());
            holder.title.setText(noticeList.get(position).getTitle());
            holder.content.setText(noticeList.get(position).getContent());
            holder.time.setText(noticeList.get(position).getTime());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.btn.isChecked()) {
                        holder.btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));
                        holder.content.setVisibility(View.VISIBLE);
                    } else {
                        holder.btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp));
                        holder.content.setVisibility(View.GONE);
                    }
                }
            });

            if(noticeList.get(position).getUid().equals(uid)){
                holder.delete.setVisibility(View.VISIBLE);
                holder.update.setVisibility(View.VISIBLE);

                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("정말 삭제하시겠습니까?");
                        builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Map<String, Object> map = new HashMap<>();
                                map.put(noticeList.get(position).getCode(), null);
                                firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(),"삭제하였습니다.", Toast.LENGTH_SHORT).show();
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
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        LayoutInflater inflater = getLayoutInflater();
                        View notice = inflater.inflate(R.layout.insert_notice, null);
                        builder.setView(notice);
                        final EditText editTextTitle = notice.findViewById(R.id.edit_title);
                        editTextTitle.setText(noticeList.get(position).getTitle());
                        final EditText editTextContent = notice.findViewById(R.id.edit_content);
                        editTextContent.setText(noticeList.get(position).getContent());
                        builder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String title = editTextTitle.getText().toString();
                                String content = editTextContent.getText().toString();
                                if (title.equals("") || content.equals("")) {
                                    Toast.makeText(getContext(), "제목과 내용을 입력하시기 바랍니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                NoticeList notice = new NoticeList();
                                notice.setTitle(title);
                                notice.setContent(content);
                                notice.setUid(uid);
                                SimpleDateFormat sd = new SimpleDateFormat("yyyy/MM/dd");
                                Date date = new Date();
                                String newDate = sd.format(date);
                                notice.setTime(newDate);
                                notice.setName(noticeList.get(position).getName());
                                Map<String, Object> map = new HashMap<>();
                                map.put(noticeList.get(position).getCode(), notice);
                                firebaseDatabase.child("Notice").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getContext(), "수정하였습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        builder.setCancelable(false);
                        builder.show();
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
            TextView content;
            TextView time;
            ToggleButton btn;
            ImageView delete;
            ImageView update;
            public NoticeViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.notice_title);
                content = itemView.findViewById(R.id.notice_content);
                time = itemView.findViewById(R.id.notice_time);
                btn = itemView.findViewById(R.id.toggle_btn);
                name = itemView.findViewById(R.id.notice_name);
                delete = itemView.findViewById(R.id.delete);
                update = itemView.findViewById(R.id.update);
            }
        }
    }
}
