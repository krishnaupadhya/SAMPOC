package com.prism.poc;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.prism.poc.events.LocationPostEvent;
import com.prism.poc.locationhistory.LocationHistoryUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Krishna Upadhya on 2019-06-05.
 */
public class UploadLocationService extends Service implements IRecordPostLocationListener {

    private static final String TAG = "UploadLocationService ";
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS ;
    /**
     * The current location.
     */
    private Location mCurrentLocation;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;
    private RemoteViews remoteViews;

    public UploadLocationService() {
        //nothing to do
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        GenericUtil.log(TAG + "onCreate() onCreate");
    }

    @Override
    public void onDestroy() {
        GenericUtil.log(TAG + " onDestroy");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        String serviceType = intent.getStringExtra(AppConstants.KEY_INTENT_SERVICE_TYPE);
        // if user starts the service
        if (AppConstants.START_UPLOADING_LOCATON.equals(serviceType)) {
            startForeground(AppConstants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
            startGettingUserLocation();
        }else if (AppConstants.STOP_UPLOADING_LOCATON.equals(serviceType)) {
            stopService();
        }
        else {
            stopForeground(true);
            stopSelf();
        }

        return START_NOT_STICKY;
    }


    private void startGettingUserLocation() {
        try {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                }
            };

            LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(1f);
            RecordUserLocationUtil.getInstance().setListener(this);
            try {
                mFusedLocationClient
                        .getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    mCurrentLocation = task.getResult();
                                    EventBus.getDefault().post(new LocationPostEvent(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                                    RecordUserLocationUtil.getInstance().sendLocation(mCurrentLocation);
                                    GenericUtil.log(TAG + mCurrentLocation);
                                    if (!GenericUtil.isDistanceWithinRange(mCurrentLocation)) {
                                        LocationHistoryUtils.getInstance().saveLocationToLocal(mCurrentLocation);
                                    }
                                } else {
                                    GenericUtil.log(TAG + "  Failed to get location.");
                                }
                            }
                        });
            } catch (SecurityException ex) {
                GenericUtil.log(TAG + "  Failed to get location. "+ex.getLocalizedMessage());
                RecordUserLocationUtil.getInstance().setListener(null);
                stopService();
            }

            GenericUtil.log(TAG + "  requestLocationUpdates");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, null);

        } catch (Exception ex) {
            GenericUtil.log(TAG + "  Failed to get location. "+ex.getLocalizedMessage());
            RecordUserLocationUtil.getInstance().setListener(null);
            stopService();
        }

    }

    public void stopLocationUpdates() {
        // Removing location updates
        if (mFusedLocationClient != null) {
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });
        }
    }


    private Notification prepareNotification() {
        // handle build version above android oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            CharSequence name = getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MapsActivity.class);
        notificationIntent.setAction(AppConstants.START_UPLOADING_LOCATON);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);

        Intent stopUploadingEvent = new Intent(this,UploadLocationService.class);
        stopUploadingEvent.putExtra(AppConstants.KEY_INTENT_SERVICE_TYPE, AppConstants.CLOSE_NOTIFICATION);

        remoteViews.setOnClickPendingIntent(
                R.id.close_image,
                PendingIntent.getService(
                        this, 2, stopUploadingEvent
                        , PendingIntent.FLAG_UPDATE_CURRENT
                )
        );

        // adding action to extend button
        Intent extendIntent = new Intent(this,DashboardActivity.class);
        extendIntent.setAction(AppConstants.START_UPLOADING_LOCATON);

        // notification builder
        notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setColor(getColor(android.R.color.transparent));
        }
        notificationBuilder.setColorized(true);

        notificationBuilder
                .setCustomContentView(remoteViews)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setVibrate(new long[]{0L})
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }

    @Override
    public void postLocationStarted() {
        //nothing to do
    }

    @Override
    public void onPostSuccess() {
        GenericUtil.log(TAG + " onPostSuccess");
        stopService();
    }

    private void stopService() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        GenericUtil.log(TAG + " stopService");
        RecordUserLocationUtil.getInstance().setListener(null);
        mNotificationManager.cancel(AppConstants.NOTIFICATION_ID_FOREGROUND_SERVICE);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public void onPostFailure(Throwable e) {
        GenericUtil.log(TAG + " onPostFailure ");
        stopService();
    }

}
