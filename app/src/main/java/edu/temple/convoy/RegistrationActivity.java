package edu.temple.convoy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText first;
    EditText last;
    EditText pw;
    EditText username;
    RequestQueue requestQueue;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        first = findViewById(R.id.editFirst);
        last = findViewById(R.id.editLast);
        pw = findViewById(R.id.editPassword);
        username = findViewById(R.id.editUser);
        textView = findViewById(R.id.txtAsk);

        requestQueue = Volley.newRequestQueue(this);

    }

    public void registerClick(View view) {

        //check for null values
        if(first.getText().toString().trim().isEmpty() || last.getText().toString().trim().isEmpty() || pw.getText().toString().trim().isEmpty() ||
                username.getText().toString().trim().isEmpty()){
            Toast.makeText(RegistrationActivity.this,"Please Fill In All Fields",Toast.LENGTH_SHORT).show();
        }else {

            String register = "https://kamorris.com/lab/convoy/account.php";

                    /*
                    if (books.size() == 0) {
                        Intent intent = new Intent();
                        setResult(RESULT_FIRST_USER, intent);
                        finish();
                    } else {
                        Intent intent = new Intent();
                        intent.putStringArrayListExtra("title", books);
                        intent.putStringArrayListExtra("author", authors);
                        intent.putIntegerArrayListExtra("id", ids);
                        intent.putStringArrayListExtra("pictures", coverURL);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                     */

            StringRequest strRequest = new StringRequest(Request.Method.POST, register,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();

                            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("user",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("username",username.getText().toString());
                            editor.putString("name",first.getText().toString());
                            editor.apply();

                            finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("action", "REGISTER");
                    params.put("username", username.getText().toString());
                    params.put("password", pw.getText().toString());
                    params.put("firstname", first.getText().toString());
                    params.put("lastname", last.getText().toString());
                    return params;
                }
            };

            requestQueue.add(strRequest);

        }
    }

    public void backClick(View view) {
        finish();
    }
}