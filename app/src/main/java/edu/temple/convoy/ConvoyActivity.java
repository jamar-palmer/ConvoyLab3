package edu.temple.convoy;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ConvoyActivity extends FragmentActivity implements OnMapReadyCallback {

    LocationManager locationManager;
    LocationListener locationListener;
    Location prevLocation;
    TextView textView;
    RequestQueue requestQueue;
    Location lastKnown;
    LatLng currentLocal;

    GoogleMap mapAPI;
    SupportMapFragment mapFragment;
    Button button;
    String startJoin;
    JSONArray jsonArray;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("firstname");
            double latt = Double.valueOf(intent.getStringExtra("latitude"));
            double longg = Double.valueOf(intent.getStringExtra("longitude"));
            LatLng latLng2 = new LatLng(latt,longg);
            otherUsers(message, latLng2);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convoy);

        FileInputStream serviceAccount =
                null;
        try {
            serviceAccount = new FileInputStream("C:\\Users\\Jamar\\AndroidStudioProjects\\ConvoyApp\\app\\src\\main\\res\\convoyapp-ed387-firebase-adminsdk-axzmh-9c196e3fe4.json");
            //  FirebaseOptions options = new FirebaseOptions.Builder()
            //          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            //        .build();

            // FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPS"));

        requestQueue = Volley.newRequestQueue(this);
        textView = findViewById(R.id.txtId);
        button = findViewById(R.id.button4);

        //json
        String json = "{\"action\":\"UPDATE\", \"data\":[{\"username\":\"user1\", \"firstname\":\"firstname1\", \"lastname\":\"lastname1\", \"latitude\":72.3456, \"longitude\":125.345356}, {\"username\":\"user2\", \"firstname\":\"firstname2\", \"lastname\":\"lastname2\", \"latitude\":72.4434, \"longitude\":125.27543}, {\"username\":\"user3\", \"firstname\":\"firstname3\", \"lastname\":\"lastname3\", \"latitude\":72.42434, \"longitude\":125.25683}]}";

        try {
            JSONObject jobj =  new JSONObject(json);
            String jsonConvert = jobj.getString("data");
            jsonArray = new JSONArray(jsonConvert);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        mMessageReceiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(ConvoyActivity.this, intent.getExtras().getString("location"), Toast.LENGTH_SHORT).show();
            }
        };

        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", null);
        Boolean checking = settings.getBoolean("tokenSave", false);
        if(checking!=false){
            uploadTokenIfNotAlreadyRegistered();
        }

        if (check != null) {
            textView.setText("Convoy ID:" + check);

            String check2 = settings.getString("start", null);
            String username = settings.getString("username", "N/A");
            if(check2!= null && username.equals(check2)){
                startJoin = "start";
                button.setText("END CONVOY");
            }else{
                startJoin = "join";
            }

        }
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);
        mapFragment.getMapAsync(this);

        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (prevLocation != null) {
                    if (ActivityCompat.checkSelfPermission(ConvoyActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ConvoyActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    lastKnown = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
                    double lat = lastKnown.getLatitude();
                    double longi = lastKnown.getLongitude();
                    LatLng latLng = new LatLng(lat,longi);
                    updateConvoy(lat, longi);
                    mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.f));
                    currentLocal = latLng;


                    RemoteMessage message = new RemoteMessage.Builder("dKp_M7kZxjM:APA91bEpAfAF-j-rA7urAKC_ppIMO0KVd5SVmuh8p0P1yatzEpbarHU1b9cWD3lUeqAPsP1D8BZF2qJ4rlRqnfM7JYCxlQehWEfvi9zj7L-OdoUQrknZaFXxApxM40e9WZRGfDZd-8zX")
                            .addData("message", "Hello")
                            .build();
                    FirebaseMessaging.getInstance().send(message);

                }
                prevLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!haveGPSPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            doGPSStuff();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean haveGPSPermission() {

        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    private void doGPSStuff() {
        if (haveGPSPermission())
            //location updates only for 10 meters-
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        doGPSStuff();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doGPSStuff();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mapAPI = googleMap;
        mapAPI.setMyLocationEnabled(true);

        lastKnown = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), false));
        if(lastKnown != null){
            double lat = lastKnown.getLatitude();
            double longi = lastKnown.getLongitude();
            LatLng latLng = new LatLng(lat,longi);
            mapAPI.addMarker(new MarkerOptions().position(latLng).title("mm"));
            mapAPI.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.f));

        }

        //LatLng mm = new LatLng(19.389137, 76.031094);
        // mapAPI.moveCamera(CameraUpdateFactory.newLatLng(mm));
        // mapAPI.moveCamera(CameraUpdateFactory.newLatLngZoom(mm, 16));
    }

    public void startConvoy(View view) {
        if(!textView.getText().toString().contains("Convoy")) {
            Intent launchIntent = new Intent(ConvoyActivity.this, StartConvoyActivity.class);
            startActivity(launchIntent);
        }
    }

    public void endConvoy(View view) {
        if(textView.getText().toString().contains("Convoy")) {
            Intent launchIntent = new Intent(ConvoyActivity.this, EndConvoyActivity.class);
            startActivity(launchIntent);

        }
    }

    public void leaveConvoy(){
        String convoy = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){

                            SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            String convoyIdentity = settings.getString("convoyID", "N/A");
                            endTopic(convoyIdentity);

                            editor.putString("convoyID",null);
                            editor.putString("start",null);
                            editor.apply();
                            textView.setText(" ");
                            startJoin = " ";
                            Toast.makeText(ConvoyActivity.this, "You Hava Ended This Convoy", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(ConvoyActivity.this, "You Have Not Joined A Convoy", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String convoyIdentity = settings.getString("convoyID", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "END");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",convoyIdentity);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void endingConvoy(){
        String convoy = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){

                            SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            String convoyIdentity = settings.getString("convoyID", "N/A");
                            endTopic(convoyIdentity);

                            editor.putString("convoyID",null);
                            editor.putString("start",null);
                            editor.apply();
                            textView.setText(" ");
                            startJoin = " ";
                            Toast.makeText(ConvoyActivity.this, "You Hava Left This Convoy", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(ConvoyActivity.this, "You Have Not Joined A Convoy", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String convoyIdentity = settings.getString("convoyID", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "LEAVE");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",convoyIdentity);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void endTopic(String convoyTopic){

        FirebaseMessaging.getInstance().unsubscribeFromTopic(convoyTopic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ConvoyActivity.this, "Topic Successfully UnSubscribed", Toast.LENGTH_SHORT).show();
                if (!task.isSuccessful()) {
                    Toast.makeText(ConvoyActivity.this, "Topic UnSubscription Unsuccessful", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    public void logoutClick(View view) {

        leaveConvoy();
        String convoy = "https://kamorris.com/lab/convoy/account.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){


                            SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.clear();
                            editor.apply();

                            Intent launchIntent = new Intent(ConvoyActivity.this, MainActivity.class);
                            startActivity(launchIntent);
                            finito();

                        }else{
                            Toast.makeText(ConvoyActivity.this, "Logout Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "LOGOUT");
                params.put("username", username);
                params.put("session_key",session);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void joinClick(View view) {

        Intent launchIntent = new Intent(ConvoyActivity.this, JoinConvoyActivity.class);
        startActivity(launchIntent);
    }

    public void updateConvoy(Double lat, Double longi){
        String convoy = "https://kamorris.com/lab/convoy/convoy.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){
                            //Toast.makeText(ConvoyActivity.this, "Updated YAAAAAY", Toast.LENGTH_SHORT).show();


                        }else{
                            //Toast.makeText(ConvoyActivity.this, "Updated failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {


                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String convoyIdentity = settings.getString("convoyID", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "UPDATE");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",convoyIdentity);
                params.put("latitude",String.valueOf(lat));
                params.put("longitude",String.valueOf(longi));



                return params;
            }
        };

        requestQueue.add(strRequest);
    }

    public void updateAccount(){
        String convoy = "https://kamorris.com/lab/convoy/account.php";
        StringRequest strRequest = new StringRequest(Request.Method.POST, convoy,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(response.contains("SUCCESS")){
                            Toast.makeText(ConvoyActivity.this, "Updated IS THIS RIGHT" + response, Toast.LENGTH_SHORT).show();


                        }else{
                            Toast.makeText(ConvoyActivity.this, "Updated faileasdasdsadasdd", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(ConvoyActivity.this, error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {


                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String fcmKey = settings.getString("token", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "UPDATE");
                params.put("username", username);
                params.put("session_key",session);
                params.put("fcm_token",fcmKey);
                return params;
            }
        };

        requestQueue.add(strRequest);
    }
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", "N/A");
        if(!check.equals("N/A")){
            textView.setText("Convoy ID:" + check);

            String check2 = settings.getString("start", null);
            String username = settings.getString("username", "N/A");
            if(check2!= null && username.equals(check2)){
                startJoin = "start";
                button.setText("END CONVOY");
            }else{
                startJoin = "join";
                button.setText("LEAVE CONVOY");
            }
        }

        String leave = settings.getString("end", null);

        if(leave!=null){
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("end",null);
            editor.apply();
            if(startJoin.equals("start")){
                leaveConvoy();
            }else{
                endingConvoy();
            }
        }
    }


    public void finito(){
        finish();
    }


    public void recordClick(View view) {
        Intent launchIntent = new Intent(ConvoyActivity.this, AudioActivity.class);
        startActivity(launchIntent);
    }

    private void uploadTokenIfNotAlreadyRegistered() {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String token = settings.getString("token", null);
        String username = settings.getString("username", "N/A");
        String session = settings.getString("session", "N/A");
        Boolean checking = settings.getBoolean("tokenSave", false);

        if (token !=null && checking == false) {
            FirebaseMessaging.getInstance()
                    .getToken().addOnSuccessListener(new OnSuccessListener<String>() {
                @Override
                public void onSuccess(String s) {
                    Volley.newRequestQueue(ConvoyActivity.this).add(new StringRequest(Request.Method.POST, "https://kamorris.com/lab/convoy/convoy.php", response -> {
                        // If success, use SharedPreferences to guard against multiple attempts to register with the server
                    }, Throwable::printStackTrace) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> map = new HashMap<>();
                            map.put("action", "UPDATE");
                            map.put("username", username);
                            map.put("session_key", session);
                            map.put("fcm_token", s); // s is your token
                            return map;
                        }
                    });
                }
            });
        }

    }

    public void otherUsers(String username, LatLng latLng){
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String name = settings.getString("username", "N/A");
        if(!name.equals(username)){

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(latLng);
            builder.include(currentLocal);

            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 14);
            mapAPI.addMarker(new MarkerOptions().position(latLng).title(username));
            mapAPI.animateCamera(cu);
        }
    }

}