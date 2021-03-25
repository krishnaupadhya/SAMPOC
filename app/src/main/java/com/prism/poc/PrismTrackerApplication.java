package com.prism.poc;

import android.app.Application;

/**
 * Created by Krishna Upadhya on 19/03/20.
 */
public class PrismTrackerApplication extends Application {
    private static PrismTrackerApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static PrismTrackerApplication getInstance() {
        return instance;
    }

}
