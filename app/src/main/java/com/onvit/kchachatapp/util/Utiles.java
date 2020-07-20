package com.onvit.kchachatapp.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.onvit.kchachatapp.R;
import com.onvit.kchachatapp.model.NotificationModel;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utiles {
    private static long returnTime;
    public static final int RESIZE = 500;
    public static AlertDialog createLoadingDialog(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.loading, null);
        TextView tx = view.findViewById(R.id.loading_text);
        tx.setText(text);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }

    public static void sendFcm(List<String> registration_ids, String message, Context context, String toRoom, String uri) {
        Gson gson = new Gson();
        String userName = PreferenceManager.getString(context, "name");
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.registration_ids = registration_ids;
//        notificationModel.notification.title = userName;
//        notificationModel.notification.text = message;
//        notificationModel.notification.tag = toRoom;
//        notificationModel.notification.click_action = "GroupMessage";
        if (message.length() > 30) {
            message = message.substring(0, 30) + "...";
        }
        notificationModel.data.title = userName;
        notificationModel.data.body = message;
        notificationModel.data.tag = toRoom;
        notificationModel.data.click_action = "GroupMessage";
        notificationModel.data.uri = uri;
        notificationModel.content_available = true;
        notificationModel.priority = "high";
        notificationModel.delay_while_idle = false;
        notificationModel.time_to_live = 0;

        RequestBody requestBody = RequestBody.create(gson.toJson(notificationModel), MediaType.parse("application/json; charset=utf8"));
        Request request = new Request.Builder().header("Content-Type", "apllication/json")
                .addHeader("Authorization", "key=" + PreferenceManager.getString(context, "serverKey") + "")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {

            }
        });
    }

    public static Toast customToast(Activity context, String text) {
        Toast toast = new Toast(context);
        View custom = View.inflate(context, R.layout.custom_toast, null);
        TextView textView = custom.findViewById(R.id.message_toast);
        textView.setText(text);
        toast.setView(custom);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        return toast;
    }

    public static boolean blockDoubleClick() {
        if (System.currentTimeMillis() - returnTime < 1000) {
            return true;
        } else {
            returnTime = System.currentTimeMillis();
            return false;
        }
    }


}
