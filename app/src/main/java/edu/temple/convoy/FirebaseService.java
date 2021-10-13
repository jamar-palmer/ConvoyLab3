package edu.temple.convoy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirebaseService extends FirebaseMessagingService {

    private LocalBroadcastManager broadcaster;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // broadcaster = LocalBroadcastManager.getInstance(this);
        //Intent intent = new Intent();
        //intent.putExtra("location", remoteMessage.getData().toString());
        //broadcaster.sendBroadcast(intent);

        Log.d("TAG", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //json
            String json = "{\"action\":\"UPDATE\", \"data\":[{\"username\":\"user1\", \"firstname\":\"firstname1\", \"lastname\":\"lastname1\", \"latitude\":72.3456, \"longitude\":125.345356}, {\"username\":\"user2\", \"firstname\":\"firstname2\", \"lastname\":\"lastname2\", \"latitude\":72.4434, \"longitude\":125.27543}, {\"username\":\"user3\", \"firstname\":\"firstname3\", \"lastname\":\"lastname3\", \"latitude\":72.42434, \"longitude\":125.25683}]}";
            String[] payloading = remoteMessage.getData().toString().split("=");
            Intent passing = new Intent("GPS");

            try {

                JSONObject jobj =  new JSONObject(payloading[1]);
                String jsonConvert = jobj.getString("data");
                JSONArray jsonArray = new JSONArray(jsonConvert);
                for(int i = 0; i < jsonArray.length(); i++){
                    String jsonArrayConvert = jsonArray.getString(i);
                    JSONObject jobjDetail =  new JSONObject(jsonArrayConvert);
                    passing.putExtra("firstname",jobjDetail.get("username").toString());
                    passing.putExtra("latitude",jobjDetail.get("latitude").toString());
                    passing.putExtra("longitude",jobjDetail.get("longitude").toString());
                    LocalBroadcastManager.getInstance(this).sendBroadcast(passing);

                    Log.d("TAG4", "MHEY LISTEN: " + jobjDetail.get("longitude"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


            Log.d("TAG3", "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                //  scheduleJob();
            } else {
                // Handle message within 10 seconds
                // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TAG2", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d("TAG21", "Message Notification title: " + remoteMessage.getNotification().getTitle());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("token",s);
        editor.apply();

        String username = settings.getString("username", "N/A");
        String session = settings.getString("session", "N/A");

        Log.d("TAGggg", "token: " + s);
        Volley.newRequestQueue(this).add(new StringRequest(Request.Method.POST, "https://kamorris.com/lab/convoy/convoy.php", response -> {
            // If success, use SharedPreferences to guard against multiple attempts to register with the server
            // eg. getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE).edit().putBoolean(TOKEN_SAVED, true).apply();
            editor.putBoolean("tokenSave",true);
            editor.apply();

        }, Throwable::printStackTrace) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("action", "UPDATE");
                map.put("username", username);
                map.put("session_key",session);
                map.put("fcm_token", s); // s is your token
                return map;
            }
        });

    }
}
