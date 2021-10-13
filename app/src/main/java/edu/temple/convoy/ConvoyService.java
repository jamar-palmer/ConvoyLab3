package edu.temple.convoy;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class ConvoyService extends Service {
    LocationManager locationManager;
    LocationListener locationListener;
    Location prevLocation;
    Location lastKnown;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent notifictionIntent = new Intent(this, ConvoyActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifictionIntent, 0);
        locationManager = getSystemService(LocationManager.class);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (prevLocation != null) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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


                    //  Toast.makeText(getApplicationContext(), "Lattitude: " + String.valueOf(lat) +
                    //          "\nLongitude: " + String.valueOf(longi), Toast.LENGTH_SHORT).show();

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


        Notification notification = new NotificationCompat.Builder(this, "channel1")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Convoy Service")
                .setContentText("Convoy App is running")
                .setContentIntent(pendingIntent)
                .build();


        //location updates only for 10 meters-
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);

        startForeground(1, notification);



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
