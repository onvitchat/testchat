package com.example.chatapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapp.model.User;
import com.google.firebase.auth.FirebaseAuth;

public class PersonInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar infoToolbar;
    private String info;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);

        infoToolbar = findViewById(R.id.chat_toolbar);
        user = getIntent().getParcelableExtra("info");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (user.getUid().equals(uid)) {
            info = "내 정보";
        } else {
            info = user.getUserName()+"님의 정보";
        }

        setSupportActionBar(infoToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(info);
        actionBar.setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.info_img);
        //사진에 곡률넣음.
        if(user.getUserProfileImageUrl().equals("noImg")){
            Glide.with(this).load(R.drawable.standard_profile).apply(new RequestOptions().centerCrop()).into(imageView);
        }else{
            Glide.with(this).load(user.getUserProfileImageUrl()).apply(new RequestOptions().centerCrop()).into(imageView);
        }
        GradientDrawable gradientDrawable = (GradientDrawable) this.getDrawable(R.drawable.radius);
        imageView.setBackground(gradientDrawable);
        imageView.setClipToOutline(true);


        LinearLayout modify = findViewById(R.id.info_modi);
        LinearLayout call = findViewById(R.id.info_call);
        modify.setOnClickListener(this);
        call.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fromleft, R.anim.toright);//화면 사라지는 방향
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()){
            case R.id.info_modi:
//                intent = new Intent(PersonInfoActivity.this, SignUpActivity.class);
//                intent.putExtra("modify", user);
//                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                startActivity(intent);
                break;
            case R.id.info_call:
               intent = new Intent(Intent.ACTION_DIAL);
               intent.setData(Uri.parse("tel:"+user.getTel()));
               if(intent.resolveActivity(getPackageManager())!=null){
                   startActivity(intent);
               }
                break;
        }
    }
}
