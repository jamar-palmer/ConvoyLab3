package edu.temple.convoy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class JoinConvoyActivity extends AppCompatActivity {

    private String username;
    private String session;
    EditText editText;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_convoy);

        editText = findViewById(R.id.editConvoyId);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        username = settings.getString("username", "N/A");
        session = settings.getString("session", "N/A");

        //class empty for now
    }

    public void backClick(View view) {
        finish();
    }

    public void joinClick(View view) {

        String retreival = editText.getText().toString();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        requestQueue = Volley.newRequestQueue(this);

        String convoy = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){

                            String[] split = response.split(":");
                            String[] again = split[2].split("\"");
                            String conID= again[1];

                            SharedPreferences.Editor editor = settings.edit();
                            editor.putString("convoyID",conID);
                            editor.putString("join",username);
                            editor.apply();
                            Toast.makeText(JoinConvoyActivity.this,"Successfully Joined Convoy", Toast.LENGTH_SHORT).show();

                            //FCM topic
                            FirebaseMessaging.getInstance().subscribeToTopic(conID)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(JoinConvoyActivity.this, "Topic Successfully Subscribed", Toast.LENGTH_SHORT).show();
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(JoinConvoyActivity.this, "Topic Subscription Unsuccessful", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    });


                            //start service
                            Intent serviceIntent = new Intent(JoinConvoyActivity.this, ConvoyService.class);

                            startService(serviceIntent);

                            finish();


                        }else{
                            Toast.makeText(JoinConvoyActivity.this, "Issue joining Convoy", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(JoinConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "JOIN");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",retreival);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }
}