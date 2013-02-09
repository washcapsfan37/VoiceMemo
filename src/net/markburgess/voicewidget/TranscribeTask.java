package net.markburgess.voicewidget;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TranscribeTask extends AsyncTask<String, Void, TranscribeTaskResponse>
{
	private final String LOG_TAG = TranscribeTask.class.getSimpleName();
	private Context context;
	private Activity activity;
	private ProgressDialog progressDialog;
	private long id;

	public TranscribeTask(long id, Context ctx, Activity activity)
	{
		this.id=id;
		context = ctx;
		this.activity = activity;
	}

	@Override
	protected void onPreExecute()
	{
		if ( !Util.isOnline(context) )
		{
			Log.i(LOG_TAG, "Not online, exitting...");
			Toast error = Toast.makeText(context, context.getString(R.string.error_no_network), Toast.LENGTH_SHORT);
			error.show();
			this.cancel(true);
			return;
		}

		// Show a progress dialog
		//progressDialog = new ProgressDialog(activity);
		//progressDialog.setMessage(context.getString(R.string.dialog_transcribe));
		//progressDialog.setCancelable(false);
		//progressDialog.show();
	}

	@Override
    protected TranscribeTaskResponse doInBackground(String... params)
    {
		File file = Util.getConvertedRecording(id);
		if ( file == null )
		{
			TranscribeTaskResponse error = new TranscribeTaskResponse(TranscribeTaskResponse.FILE_NOT_FOUND, null);
			this.cancel(true);
			return error;
		}
		TranscribeFileHttpClient client = new TranscribeFileHttpClient();
		try
		{
			String transcribedText = client.postFile(file, 1);
			Log.i(LOG_TAG, "Transcribed: " + transcribedText);
			TranscribeTaskResponse response = new TranscribeTaskResponse(TranscribeTaskResponse.SUCCESS, transcribedText);
			return response;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error transcribing file", e);
			TranscribeTaskResponse error = new TranscribeTaskResponse(TranscribeTaskResponse.SERVICE_ERROR, null);
			return error;
		}
	}

	@Override
	protected void onPostExecute(TranscribeTaskResponse response)
	{
		//progressDialog.dismiss();

		if ( response == null || response.responseCode != TranscribeTaskResponse.SUCCESS )
		{
			String message = null;
			if ( response == null )
			{
				message = context.getString(R.string.error_unknown);
			}
			else if ( response.responseCode == TranscribeTaskResponse.FILE_NOT_FOUND )
			{
				message = context.getString(R.string.error_file_not_found);
			}
			else if ( response.responseCode == TranscribeTaskResponse.SERVICE_ERROR )
			{
				message = context.getString(R.string.error_contact_service);
			}
			else 
			{
				message = context.getString(R.string.error_unknown);
			}
			Toast error = Toast.makeText(context, message, Toast.LENGTH_SHORT);
			error.show();
			return;
		}
		else
		{
			Intent updateIntent = new Intent(TranscribeVoiceActivity.TRANSCRIBE_ACTION);
			updateIntent.putExtra("message", response.message);
			activity.sendBroadcast(updateIntent);
		}
	}
	
}