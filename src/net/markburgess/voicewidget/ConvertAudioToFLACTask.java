package net.markburgess.voicewidget;

import java.io.File;

import javaFlacEncoder.EncodingConfiguration;
import javaFlacEncoder.FLAC_FileEncoder;
import javaFlacEncoder.StreamConfiguration;

import net.markburgess.voicewidget.TranscribeTask;
import net.markburgess.voicewidget.TranscribeTaskResponse;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ConvertAudioToFLACTask extends AsyncTask<String, Void, Boolean> 
{
	
	private final String LOG_TAG = ConvertAudioToFLACTask.class.getSimpleName();
	private Context context;
	private Activity activity;
	private ProgressDialog progressDialog;
	private long id;

	public ConvertAudioToFLACTask(long id, Context ctx, Activity activity)
	{
		this.id=id;
		this.context = ctx;
		this.activity = activity;
	}

	
	@Override
	protected void onPreExecute()
	{
		// Show a progress dialog
		//progressDialog = new ProgressDialog(activity);
		//progressDialog.setMessage(activity.getString(R.string.converting_file));
		//progressDialog.setCancelable(false);
		//progressDialog.show();
	}

	@Override
	protected Boolean doInBackground(String... arg0) 
	{
		try
		{
			File inputFile = Util.getRecording(id);
			File outputFile = Util.getConvertedRecordingPlaceholder(id);
			Log.d(LOG_TAG, "Converting " + inputFile.getAbsolutePath() + " to " + outputFile.getAbsolutePath());
			FLAC_FileEncoder flacEncoder = new FLAC_FileEncoder();
			EncodingConfiguration eConf = new EncodingConfiguration();
		    StreamConfiguration sConf = new StreamConfiguration(1, 100, 1000, 16000, 16);
		    flacEncoder.setStreamConfig(sConf);
		    flacEncoder.setEncodingConfig(eConf);
			flacEncoder.encode(inputFile, outputFile);
			return Boolean.TRUE;
		} 
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error converting file for memo: " + id, e);
			return Boolean.FALSE;
		}
	}
	
	@Override
	protected void onPostExecute(Boolean success)
	{
		//progressDialog.dismiss();
		
		if ( success == null || !success.booleanValue() )
		{
			Toast error = Toast.makeText(context, activity.getString(R.string.error_converting_file), Toast.LENGTH_SHORT);
			error.show();
		}
		else
		{
			TranscribeTask task = new TranscribeTask(id, context, activity);
			task.execute();
		}
	}

}
