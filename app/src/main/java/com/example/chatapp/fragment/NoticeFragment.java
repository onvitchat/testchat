package com.example.chatapp.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MainActivity;
import com.example.chatapp.R;
import com.example.chatapp.model.NoticeList;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class NoticeFragment extends Fragment {
    private AppCompatActivity activity;
    private Toolbar chatToolbar;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private List<NoticeList> noticeLists = new ArrayList<>();
    public NoticeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice,container,false);
        chatToolbar = view.findViewById(R.id.chat_toolbar);
        activity = (MainActivity) getActivity();
        activity.setSupportActionBar(chatToolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setTitle("공지사항");

        RecyclerView recyclerView = view.findViewById(R.id.fragment_notice_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
        recyclerView.setAdapter(new NoticeFragmentRecyclerAdapter(noticeLists));

        return view;
    }

    class NoticeFragmentRecyclerAdapter extends RecyclerView.Adapter<NoticeFragmentRecyclerAdapter.NoticeViewHolder> {
        List<NoticeList> noticeList;
        public NoticeFragmentRecyclerAdapter(List<NoticeList> list) {
            noticeList = list;
            for(int i=0; i<10; i++){
                NoticeList notice = new NoticeList();
                notice.setTitle("공지사항 올립니다.공지사항 올립니다.공지사항 올립니다.공지사항 올립니다.공지사항 올립니다.");
                notice.setContent("공지사항 내용은 뭐로할까!! 공지사항 내용은 뭐로할까!! 공지사항 내용은 뭐로할까!! 공지사항 내용은 뭐로할까!! 공지사항 내용은 뭐로할까!!");
                notice.setTime("2019/12/11");
                noticeList.add(notice);
            }

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
            holder.title.setText(noticeList.get(position).getTitle());
            holder.content.setText(noticeList.get(position).getContent());
            holder.time.setText(noticeList.get(position).getTime());
            holder.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(holder.btn.isChecked()){
                        holder.btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));
                        holder.content.setVisibility(View.VISIBLE);
                    }else{
                        holder.btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp));
                        holder.content.setVisibility(View.GONE);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return noticeList.size();
        }


        private class NoticeViewHolder extends RecyclerView.ViewHolder {
            TextView title;
            TextView content;
            TextView time;
            ToggleButton btn;
            public NoticeViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.notice_title);
                content = itemView.findViewById(R.id.notice_content);
                time = itemView.findViewById(R.id.notice_time);
                btn = itemView.findViewById(R.id.toggle_btn);
            }
        }
    }
}
