package com.onvit.kchachatapp.chat.vote;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.model.User;

import java.util.ArrayList;

public class JoinFragment extends Fragment {
    ArrayList<User> user;
    public JoinFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sign_fragment, container, false);
        user = getArguments().getParcelableArrayList("join");
        Log.d("가입유저", user.toString());
        RecyclerView recyclerView = view.findViewById(R.id.peoplefragment_recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        JoinRecyclerAdapter pf = new JoinRecyclerAdapter();
        pf.notifyDataSetChanged();
        recyclerView.setAdapter(pf);
        return view;
    }

    class JoinRecyclerAdapter extends RecyclerView.Adapter<JoinRecyclerAdapter.CustomViewHolder> {

        public JoinRecyclerAdapter() {

        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sign, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            holder.lineText.setVisibility(View.GONE);
            holder.checkBox.setVisibility(View.GONE);
            //사진에 곡률넣음.
            if (user.get(position).getUserProfileImageUrl().equals("noImg")) {
                Glide.with(holder.itemView.getContext()).load(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            } else {
                Glide.with(holder.itemView.getContext()).load(user.get(position).getUserProfileImageUrl()).placeholder(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(holder.imageView);
            }
            GradientDrawable gradientDrawable = (GradientDrawable) getContext().getDrawable(R.drawable.radius);
            holder.imageView.setBackground(gradientDrawable);
            holder.imageView.setClipToOutline(true);

            holder.textView.setText(user.get(position).getUserName());

            holder.textView_hospital.setText("[" + user.get(position).getHospital() + "]");
        }

        @Override
        public int getItemCount() {
            return user.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public CheckBox checkBox;
            public TextView textView;
            public TextView textView_hospital;
            public TextView lineText;
            public CustomViewHolder(View view) {
                super(view);
                checkBox = view.findViewById(R.id.checkbox);
                imageView = view.findViewById(R.id.frienditem_imageview);
                textView = view.findViewById(R.id.frienditem_textview);
                textView_hospital = view.findViewById(R.id.frienditem_textview_hospital);
                lineText = view.findViewById(R.id.line_text);

            }
        }
    }

}
