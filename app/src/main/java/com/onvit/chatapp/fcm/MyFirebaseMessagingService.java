package com.onvit.chatapp.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.onvit.chatapp.R;
import com.onvit.chatapp.SplashActivity;

import java.util.List;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) { // 알림 받는부분, 포그라운드꺼만 받음.
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow();
//            }

        }
        if (remoteMessage.getNotification() != null) {

        }

        sendNotification(remoteMessage);
//        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        FirebaseDatabase.getInstance().getReference().child("lastChat").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                int count = 0;
//                for(DataSnapshot item : dataSnapshot.getChildren()){
//                    LastChat chatList = item.getValue(LastChat.class);
//                    count +=  chatList.getUsers().get(uid);
//                }
//                Log.d("채팅합", count+"");
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
    }
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }
//    private void scheduleJob() {
//        // [START dispatch_job]
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance().beginWith(work).enqueue();
//        // [END dispatch_job]
//    }
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    private void sendNotification(RemoteMessage remoteMessage) { // 포그라운드일때만 여기 거쳐감
        Intent intent = new Intent(this, SplashActivity.class);//알림 누르면 열리는 창
        String tag = remoteMessage.getData().get("tag");
        int id = 1;
        if(tag.equals("normalChat")){
            id=0;
        }else if(tag.equals("officerChat")){
            id=1;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.putExtra("fcm", "fcm");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT); // 원격으로 인텐트 실행하는거 앱이 꺼져있어도 실행하는거
        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_kcha)
                        .setContentTitle(remoteMessage.getData().get("title"))
                        .setContentText(remoteMessage.getData().get("text"))
                        .setAutoCancel(true) // 누르면 알림 없어짐
                        .setSound(defaultSoundUri)
                        .setDefaults(Notification.DEFAULT_ALL)
//                        .setFullScreenIntent(pendingIntent, true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_LOW);
//            NotificationManager.IMPORTANCE_HIGH => 팝업창뜨게하는거
            channel.setVibrationPattern(new long[]{0}); // 진동없애는거? 삭제하고 다시 깔아야 적용.
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);

        }

        notificationManager.notify(id /* ID of notification */, notificationBuilder.build());
            //배지카운트표시
//        Intent intent1 = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
//        intent1.putExtra("badge_count",count);
//        intent1.putExtra("badge_count_package_name", getPackageName());
//        intent1.putExtra("badge_count_class_name", getLauncherClassName());
//        sendBroadcast(intent1);
    }

    private String getLauncherClassName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager pm = getApplicationContext().getPackageManager();
        List<ResolveInfo> resoveInfos = pm.queryIntentActivities(intent,0);
        for(ResolveInfo resolveInfo : resoveInfos){
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if(pkgName.equalsIgnoreCase(getPackageName())){
                return resolveInfo.activityInfo.name;
            }
        }
        return  null;
    }
}
