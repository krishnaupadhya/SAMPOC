package com.prism.poc;

/**
 * Created by Krishna Upadhya on 20/03/20.
 */
public interface IRecordPostLocationListener {
     void postLocationStarted();
     void onPostSuccess();
     void onPostFailure(Throwable e);
}
