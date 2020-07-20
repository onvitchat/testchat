package com.onvit.kchachatapp.ad;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onvit.kchachatapp.MainActivity;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.model.ADlist;
import com.onvit.kchachatapp.util.Utiles;

import java.util.ArrayList;
import java.util.List;

public class ShoppingFragment extends Fragment {
    private ShoppingFragmentRecyclerAdapter ShoppingFragmentRecyclerAdapter;
    private List<ADlist> advertisement = new ArrayList<>();
    private  AppCompatActivity activity;
    public ShoppingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shopping, container, false);
        Toolbar chatToolbar = view.findViewById(R.id.chat_toolbar);
        activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(chatToolbar);
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("구매");
            }
        }
        final RecyclerView recyclerView = view.findViewById(R.id.fragment_shop_recycler);
        ShoppingFragmentRecyclerAdapter = new ShoppingFragmentRecyclerAdapter(advertisement);
        recyclerView.setAdapter(ShoppingFragmentRecyclerAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        //광고
        FirebaseDatabase.getInstance().getReference().child("ADlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                advertisement.clear();
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    ADlist aDlist = item.getValue(ADlist.class);
                    advertisement.add(aDlist);
                }
                ShoppingFragmentRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return view;
    }

    class ShoppingFragmentRecyclerAdapter extends RecyclerView.Adapter<ShoppingFragmentRecyclerAdapter.CustomViewHolder> {
        private List<ADlist> advertisement;

        private ShoppingFragmentRecyclerAdapter(List<ADlist> advertisement) {
            this.advertisement = advertisement;
        }

        @NonNull
        @Override
        public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advertisement, parent, false);
            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CustomViewHolder holder, final int position) {
            final String item = advertisement.get(position).getThumbnail();
            Glide.with(activity).load(item).placeholder(R.drawable.ic_shopping).override(180, 180).into(holder.imageView);
            Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.blink_animation);
            holder.content.startAnimation(animation);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //중복클릭방지
                    if(Utiles.blockDoubleClick()){
                        return;
                    }
                    Intent intent = new Intent(getContext(), ADActivity.class);
                    intent.putExtra("ad", advertisement.get(position).getOriginal());
                    startActivity(intent);
                }
            });
            holder.content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //중복클릭방지
                    if(Utiles.blockDoubleClick()){
                        return;
                    }
                    Intent intent = new Intent(getContext(), ADActivity.class);
                    intent.putExtra("ad", advertisement.get(position).getOriginal());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return advertisement.size();
        }


        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView content;

            private CustomViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.item_tv_title);
                content = itemView.findViewById(R.id.item_tv_content);
            }
        }
    }

}