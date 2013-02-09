package net.markburgess.voicewidget;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TranscribeVoiceActivity extends Activity 
{
	private final String LOG_TAG = TranscribeVoiceActivity.class.getSimpleName();
	private BroadcastReceiver _refreshReceiver = new UpdateBroadcastReceiver();
	public static final String TRANSCRIBE_ACTION = "TranscribeAction";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_transcribe_memo);
		
		IntentFilter filter = new IntentFilter(TRANSCRIBE_ACTION);
	    this.registerReceiver(_refreshReceiver, filter); 
		
		ImageButton button= (ImageButton) findViewById(R.id.returnBtn);
		button.setOnClickListener(new View.OnClickListener() 
		{
		    @Override
		    public void onClick(View v) 
		    {
		        finish();
		    }
		});
		
		Intent intent = getIntent();
		if ( intent != null && intent.getExtras() != null )
		{
			long id = intent.getLongExtra("id", -1);
			Log.d(LOG_TAG, "Transcribing memo file: " + id);
			
			ConvertAudioToFLACTask task = new ConvertAudioToFLACTask(id, getApplicationContext(), TranscribeVoiceActivity.this);
			task.execute();
		}
		else
		{
			Toast error = Toast.makeText(getApplicationContext(), getString(R.string.error_file_not_found), Toast.LENGTH_SHORT);
			error.show();
		}
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    this.unregisterReceiver(this._refreshReceiver);
	}
	
	private void updateView(String message)
	{
		TextView text = (TextView)findViewById(R.id.responseText);
		text.setText(message);
	}
	
	public class UpdateBroadcastReceiver extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) 
		{
			if ( intent.getAction() != null )
			{
				if (TRANSCRIBE_ACTION.equals(intent.getAction()))
				{
					if ( intent.getExtras() != null )
					{
						String response = intent.getExtras().getString("message");
						if ( StringUtils.isBlank(response) )
						{
							Toast error = Toast.makeText(context, getString(R.string.empty_transcribe), Toast.LENGTH_SHORT);
							error.show();
							return;
						}
						updateView(response);
					}
				}
			}
			
		}
	
	}
	
}
