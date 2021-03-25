package com.prism.poc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

/**
 * Created by Krishna Upadhya on 21/03/20.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        GenericUtil.log(" AlarmReceiver onReceive");

        if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(AppConstants.KEY_INTENT_SERVICE_TYPE))) {
            String intentType = intent.getStringExtra(AppConstants.KEY_INTENT_SERVICE_TYPE);
            if (AppConstants.START_UPLOADING_LOCATON.equalsIgnoreCase(intentType)) {
                GenericUtil.updateLocationBackGroundService(context);
            }
        }
    }


}
