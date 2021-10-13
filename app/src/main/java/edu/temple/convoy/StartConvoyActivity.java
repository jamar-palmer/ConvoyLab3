package edu.temple.convoy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class StartConvoyActivity extends AppCompatActivity {

    private String username;
    private String session;
    RequestQueue requestQueue;
    TextView txtConvoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_convoy);

        txtConvoy = findViewById(R.id.txtConvoyMessage);
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        username = settings.getString("username", "N/A");
        session = settings.getString("session", "N/A");
        String convoyCheck = settings.getString("convoyID", null);

        if(convoyCheck!=null){
            txtConvoy.setText("Convoy ID: "+ convoyCheck);
        }

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
                            editor.putString("start",username);
                            editor.apply();
                            txtConvoy.setText("Convoy ID: "+ conID);

                            //FCM topic
                            FirebaseMessaging.getInstance().subscribeToTopic(conID)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(StartConvoyActivity.this, "Topic Successfully Subscribed", Toast.LENGTH_SHORT).show();
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(StartConvoyActivity.this, "Topic Subscription Unsuccessful", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                    });


                            //start service
                            Intent serviceIntent = new Intent(StartConvoyActivity.this, ConvoyService.class);

                            startService(serviceIntent);


                        }else{
                            Toast.makeText(StartConvoyActivity.this, "Issue retrieving Convoy ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(StartConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "CREATE");
                params.put("username", username);
                params.put("session_key",session);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void backClick(View view) {
        finish();
    }
}