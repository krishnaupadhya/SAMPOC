package com.prism.poc.events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Location;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.prism.poc.GenericUtil;
import com.prism.poc.R;
import com.prism.poc.RecordUserLocationUtil;
import com.prism.poc.locationhistory.LocationHistoryUtils;

import org.greenrobot.eventbus.EventBus;


public class LocationWorker extends Worker {

    private static final String TAG = "MyWorker";

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    /**
     * The current location.
     */
    private Location mCurrentLocation;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    private Context mContext;
    /**
     * Callback for changes in location.
     */
    private LocationCallback mLocationCallback;

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        GenericUtil.log("LocationWorker doWork: Done");
        createNotification();
        try {

            if (!GenericUtil.isLocationEnabled(mContext)) {
                GenericUtil.log("LocationWorker doWork GPS Disabled ");
                return Result.success();
            }

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
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
            try {
                mFusedLocationClient
                        .getLastLocation()
                        .addOnCompleteListener(new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                if (task.isSuccessful() && task.getResult() != null) {
                                    mCurrentLocation = task.getResult();
                                    GenericUtil.log("LocationWorker doWork: " + mCurrentLocation);
                                    if (!GenericUtil.isDistanceWithinRange(mCurrentLocation)) {
                                        LocationHistoryUtils.getInstance().saveLocationToLocal(mCurrentLocation);
                                    }
                                    EventBus.getDefault().post(new LocationPostEvent(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                } else {
                                    GenericUtil.log("LocationWorker doWork: Failed to get location.");
                                }
                            }
                        });
            } catch (SecurityException ex) {
                GenericUtil.log("LocationWorker doWork: Lost location permission." + ex.getMessage());
            }

            try {
                GenericUtil.log("LocationWorker doWork: requestLocationUpdates");
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, null);
            } catch (SecurityException ex) {
                GenericUtil.log("LocationWorker doWork:  Lost location permission. Could not request updates." + ex.getMessage());
            }

        } catch (Exception ex) {
            GenericUtil.log("LocationWorker doWork:" + ex.getMessage());
            GenericUtil.handleException(ex);
        }

        return Result.success();
    }

    public void createNotification() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = mContext.getString(R.string.app_name);
            String description = mContext.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(mContext.getString(R.string.app_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, mContext.getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle(mContext.getString(R.string.location_update))
                .setContentText(String.format(mContext.getString(R.string.shared_location_notification), GenericUtil.getCurrentTime()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(mContext.getString(R.string.shared_location_notification), GenericUtil.getCurrentTime())));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1001, builder.build());

    }
}