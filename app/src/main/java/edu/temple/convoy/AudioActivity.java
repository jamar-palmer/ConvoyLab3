package edu.temple.convoy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.ArrayList;
import java.util.Calendar;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AudioActivity extends AppCompatActivity implements RecyclerAdapter.onRecordClickListener {

    private static int MICRO_PERMISSION = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Boolean playing;
    Button button;
    private DownloadManager mgr;
     long downloadStat;

    private static final int REQUEST_PERMISSIONS = 100;
    private String filePath;
    private File filing;
    RequestQueue requestQueue;
    RecyclerView recyclerView;
    ArrayList<File> files;
    RecyclerAdapter recyclerAdapter;
    Handler handler;

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            if(downloadStat==id){
                Toast.makeText(AudioActivity.this, "Download Finished", Toast.LENGTH_SHORT).show();
            }

            // Get extra data included in the Intent
            if(intent.getStringExtra("message").contains("record")){
                String message = intent.getStringExtra("firstname");
                String audioClip = intent.getStringExtra("messages");

                downloadFile(audioClip, message);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        playing = false;
        requestQueue = Volley.newRequestQueue(this);
        recyclerView = findViewById(R.id.recycleAudio);
        button = findViewById(R.id.btnRecording);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("GPS"));

        files = new ArrayList<File>();

        File[] dFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
        for(int i =0; i< dFiles.length; i++){
            files.add(dFiles[i]);
        }
        handler = new Handler();

         recyclerAdapter = new RecyclerAdapter(AudioActivity.this, this, files);
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AudioActivity.this));
        mgr=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);



        if(micPresent()){
            getPerms();
        }
        sendRecording();
    }

    public void startRecClick(View view) {
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        String check = settings.getString("convoyID", null);

        if(check!=null && !playing){

            try{
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setOutputFile(getRecording());
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.prepare();
                mediaRecorder.start();
                Toast.makeText(AudioActivity.this, "RECORDING STARTED", Toast.LENGTH_SHORT).show();
                playing=true;
                button.setText("STOP RECORDING");
            }catch(Exception e){
                 e.printStackTrace();
            }

        }else if(check!=null && playing){
            Toast.makeText(AudioActivity.this, "RECORDING STOPPED", Toast.LENGTH_SHORT).show();
            uploadBitmap(filing);


            files.add(filing);
             recyclerAdapter.notifyDataSetChanged();

            filing =null;
            button.setText("BEGIN RECORDING");
            playing=false;
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        else{
            Toast.makeText(AudioActivity.this, "Must Be In Active Convoy To Record", Toast.LENGTH_SHORT).show();

        }
    }

    public void cancelClick(View view) {
        finish();
    }

    private boolean micPresent(){
        if(this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }else{
            return false;
        }
    }

    public void downloadFile(String urlname, String username){
        Date currentTime = Calendar.getInstance().getTime();
        String time = currentTime.toString();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlname))
                .setTitle(username + " ~" + time).setDescription("New file").setAllowedOverMetered(true).setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, username+ "~" + time + ".mp3")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        downloadStat = mgr.enqueue(request);
        Toast.makeText(getApplicationContext(),"Download Started", Toast.LENGTH_LONG).show();

        handler.postDelayed(new Runnable() {
            public void run() {

                File[] newest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).listFiles();
                files.add(newest[newest.length-1]);
                recyclerAdapter.notifyDataSetChanged();

            }
        }, 1000);


    }

    private void getPerms(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MICRO_PERMISSION);
        }
    }

    //file storage
    private String getRecording(){
        SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
        Date currentTime = Calendar.getInstance().getTime();
        String time = currentTime.toString();
        String username = settings.getString("username", "N/A");

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        File musicDir = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(musicDir, username + "~" + time);
        filing = file;
        filePath = file.getPath();
        return file.getPath();
    }

    public void playRecord(File file) {

        try{
            Log.d("TAG123123",file.getName()  );
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getPath());

            mediaPlayer.prepare();
            mediaPlayer.start();
            Toast.makeText(AudioActivity.this, "RECORDING PLAYING", Toast.LENGTH_SHORT).show();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void sendRecording(){

        //get permissions
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(AudioActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(AudioActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE))) {

            } else {
                ActivityCompat.requestPermissions(AudioActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
            }
        } else {
            Log.e("Else", "Else");
        }
    }

    public byte[] getFileDataFromDrawable(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }


    private void uploadBitmap(final File file) {

        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, "https://kamorris.com/lab/convoy/convoy.php",
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            //Toast.makeText(AudioActivity.this, new String(response.data), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("GotError",""+error.getMessage());
                    }
                }) {


            @Override
            protected Map<String, String> getParams()
            {

                SharedPreferences settings = getApplicationContext().getSharedPreferences("user", MODE_PRIVATE);
                String username = settings.getString("username", "N/A");
                String session = settings.getString("session", "N/A");
                String convoyIdentity = settings.getString("convoyID", "N/A");

                Map<String, String> params = new HashMap<String, String>();
                params.put("action", "MESSAGE");
                params.put("username", username);
                params.put("session_key",session);
                params.put("convoy_id",convoyIdentity);

                return params;

            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("message_file", new DataPart(imagename + ".mp3", getFileDataFromDrawable(file)));
                return params;
            }

        };

        //adding the request to volley
        requestQueue.add(volleyMultipartRequest);
    }

    @Override
    public void onRecordClick(File audioPlay) {
        playRecord(audioPlay);
    }
}