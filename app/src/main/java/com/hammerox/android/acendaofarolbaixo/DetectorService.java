package com.hammerox.android.acendaofarolbaixo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;

/**
 * Created by Mauricio on 10-Aug-16.
 */
public class DetectorService extends Service
        implements OnActivityUpdatedListener {

    private boolean mNotifyUser;

    public static final String LOG_TAG = "onActivityUpdated";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        // Run as foreground
        runAsForeground();

        // Start variables
//        mTextLog = new StringBuilder();
        mNotifyUser = true;

        SmartLocation smartLocation = new SmartLocation.Builder(this).logging(true).build();
        smartLocation.activity().start(this);
        Log.d(LOG_TAG, "DetectorService RUNNING");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        if (detectedActivity != null) {
//            showLog(detectedActivity);
            alarmAlgorithm(detectedActivity);
        } else {
//            mTextView.setText("Null activity");
            Log.d(LOG_TAG, "Null activity");
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop detector
        SmartLocation.with(this).activity().stop();

//        // FOR DEBUGGING: Send log
//        sendEmailLog();

        stopForeground(true);
        Log.d(LOG_TAG, "DetectorService STOPPED");
    }


    public void launchAlarm() {
        // Launch alarm screen
        Intent alarmIntent = new Intent(this, AlarmActivity.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(alarmIntent);

        // Save date and time
        FileManager.insertDateAndTime(this);

//        mTextLog.append("USER NOTIFIED")
//                .append("\n");
    }


    public void alarmAlgorithm(DetectedActivity detectedActivity) {
        int activityType = detectedActivity.getType();

        if (detectedActivity.getConfidence() == 100) {
            switch (activityType) {
                case DetectedActivity.IN_VEHICLE:
                    if (mNotifyUser) launchAlarm();
                    mNotifyUser = false;
                    break;
                case DetectedActivity.STILL:
                case DetectedActivity.TILTING:
                    // Do nothing
                    break;
                default:
                    mNotifyUser = true;
                    Log.d(LOG_TAG, "User is now prone to receive notification");
                    break;
            }
        }
    }


    private void runAsForeground(){
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent= PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_beam)
                .setContentTitle("Acenda o Farol Baixo")
                .setContentText("Detector LIGADO. Clique para desligar")
                .setContentIntent(pendingIntent).build();

        startForeground(NOTIFICATION_ID, notification);

    }


//    private StringBuilder mTextLog;
//    private long mTimeNow;
//    private long mLastTime;
//    private String[] debugEmailAddress = new String[]{"EMAIL_ADDRESS"};
//
//
//    public void showLog(DetectedActivity detectedActivity) {
//        long timeDiff = 0L;
//        mTimeNow = System.currentTimeMillis();
//
//        if (mLastTime != 0L) {
//            timeDiff = (mTimeNow - mLastTime) / DateUtils.SECOND_IN_MILLIS;
//        }
//
//        mTextLog.append(formatTime.format(mTimeNow))
//                .append(" - ")
//                .append(detectedActivity.toString().substring(23))
//                .append(" - ")
//                .append(timeDiff)
//                .append("\n");
//        Log.d(LOG_TAG, detectedActivity.toString() + " seconds: " + timeDiff);
//
//        mLastTime = mTimeNow;
//    }
//
//
//    public void sendEmailLog() {
//        try {
//            mTimeNow = System.currentTimeMillis();
//
//            Intent emailIntent = new Intent(Intent.ACTION_SEND);
//            emailIntent.setType("message/rfc822");
//            emailIntent.putExtra(Intent.EXTRA_EMAIL  , debugEmailAddress);
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "LOG " + formatDate.format(mTimeNow));
//            emailIntent.putExtra(Intent.EXTRA_TEXT   , mTextLog.toString());
//
//            Intent sendIntent = Intent.createChooser(emailIntent, "Send e-mail with...");
//            sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            getApplicationContext().startActivity(sendIntent);
//
//            Log.d(LOG_TAG, "Finished sending email...");
//        }
//        catch (android.content.ActivityNotFoundException ex) {
//            ex.printStackTrace();
//        }
//    }


}
