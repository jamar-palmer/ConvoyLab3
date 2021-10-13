package edu.temple.convoy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    EditText pw;
    EditText username;
    RequestQueue requestQueue;

    NotificationManagerCompat notiManage;
    NotificationCompat.Builder notiBuild;
    TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        NotificationChannel channel1 = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel1 = new NotificationChannel("channel1", "Channel 1", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }

        notiManage = NotificationManagerCompat.from(this);
        Notification notification = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_launcher_background).setContentTitle("Test").setContentText("Testing")
                .setPriority(NotificationCompat.PRIORITY_HIGH).setCategory(NotificationCompat.CATEGORY_MESSAGE).build();

        //notiManage.notify(1, notification);


        pw = findViewById(R.id.editPass);
        username = findViewById(R.id.editUsername);
        textView = findViewById(R.id.textView4);

        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String username = settings.getString("username", null);
        if(username!=null){
            Intent launchIntent = new Intent(MainActivity.this, ConvoyActivity.class);
            startActivity(launchIntent);
            finito();
        }

        requestQueue = Volley.newRequestQueue(this);
    }

    public void makeAccount(View view) {

        Intent launchIntent = new Intent(MainActivity.this, RegistrationActivity.class);
        startActivity(launchIntent);

    }

    public void beginConvoy(View view) {

        String login = "https://kamorris.com/lab/convoy/account.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, login,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {

                        if(response.contains("SUCCESS")){

                            String[] split = response.split(":");
                            String[] again = split[2].split("\"");
                            String session= again[1];

                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("session",session);
                            editor.putString("username",username.getText().toString());
                            editor.apply();


                            Intent launchIntent = new Intent(MainActivity.this, ConvoyActivity.class);
                            startActivity(launchIntent);

                            finito();

                        }else{
                            Toast.makeText(getApplicationContext(), "Incorrect Login Information", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "LOGIN");
                params.put("username", username.getText().toString());
                params.put("password",pw.getText().toString());
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void finito(){
        finish();
    }
}