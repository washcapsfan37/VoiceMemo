package net.markburgess.voicewidget;

import java.io.IOException;
import java.util.Date;

import net.markburgess.voicewidget.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

public class VoiceMemo extends AppWidgetProvider {
	
	public static String LOG_TAG = VoiceMemo.class.getSimpleName();

    private static MediaRecorder mRecorder = null;
    private static boolean mStartRecording = true;
    
	public static String RECORD_WIDGET_RECEIVER = "RecordWidget";
    
    public VoiceMemo() {
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds)
    {
    	Log.d(LOG_TAG, "onDeleted()");
    	doFinal();
    }
    
    @Override
    public void	onDisabled(Context context)
    {
    	Log.d(LOG_TAG, "onDisabled()");
    	doFinal();
    }
    
    private void doFinal()
    {
    	if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
    }
    
    @Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) 
	{
		Log.d(LOG_TAG, "onUpdate()");
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.voicememo);

		Intent recIntent = new Intent(context, VoiceMemo.class);
		recIntent.setAction(RECORD_WIDGET_RECEIVER);
		PendingIntent recPendingIntent = PendingIntent.getBroadcast(context, 0, recIntent, 0);
		remoteViews.setOnClickPendingIntent(R.id.record_btn, recPendingIntent);
		
		for (int appWidgetId : appWidgetIds) {
			appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
		}
	}
    
    @Override
    public void onReceive(Context context, Intent intent) 
    {
		Log.d(LOG_TAG, "onReceive()");
		if ( intent == null || intent.getAction() == null )
		{
			Log.d(LOG_TAG, "Received null intent, should not happen");
			super.onReceive(context, intent);
			return;
		}
		
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.voicememo);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, VoiceMemo.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

		if ( RECORD_WIDGET_RECEIVER.equals(intent.getAction()) )
		{
			Log.d(LOG_TAG, "record():: " + mStartRecording);
			long id = (new Date()).getTime(); 
			onRecord(mStartRecording, id);
            if (mStartRecording) 
            {
            	remoteViews.setTextViewText(R.id.record_btn, "Recording...");
            } 
            else 
            {
            	remoteViews.setTextViewText(R.id.record_btn, "Record");
            }
            for (int appWidgetId : allWidgetIds)
            {
            	appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
            if ( mStartRecording == false )
            {
            	// release media recorder
            	doFinal();
            	
            	// launch activity
            	Intent launchApp = new Intent(context, ManageVoiceActivity.class);
            	launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	launchApp.putExtra("id", id);
            	context.startActivity(launchApp);
            }
            mStartRecording = !mStartRecording;
            Log.d(LOG_TAG, "record():: " + mStartRecording);
		}
		super.onReceive(context, intent);
	}
    
    private void onRecord(boolean start, long id) 
    {
        if (start) 
        {
        	Log.d(LOG_TAG, "Start record.");
            startRecording(id);
        } 
        else 
        {
        	Log.d(LOG_TAG, "Stop record.");
            stopRecording();
        }
    }

    private void startRecording(long id) 
    {
    	boolean success = Util.createStorageDir(id);
    	
    	if ( !success )
    	{
    		return;
    	}
    	String mFileName = Util.getFilePath(id);
    	Log.d(LOG_TAG, "Creating file: " + mFileName);
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setAudioEncodingBitRate(16);

        try 
        {
            mRecorder.prepare();
        } 
        catch (IOException e) 
        {
            Log.e(LOG_TAG, "prepare() failed");
        }
        catch (IllegalStateException e) 
        {
        	Log.e(LOG_TAG, "prepare() failed");
        }
        
        mRecorder.start();
    }
	
    private void stopRecording() 
    {
    	if ( mRecorder != null )
    	{
    		mRecorder.stop();
    		mRecorder.release();
    		mRecorder = null;
    	}
    }
		
}