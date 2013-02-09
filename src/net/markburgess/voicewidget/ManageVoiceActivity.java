package net.markburgess.voicewidget;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import net.markburgess.voicewidget.R;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManageVoiceActivity extends ListActivity {
	
	private static MediaPlayer mPlayer = null;
	public static String PLAY_WIDGET_RECEIVER = "PlayWidget";
	private static final String LOG_TAG = ManageVoiceActivity.class.getSimpleName();
	private CustomAdapter adapter;
	private ListView listView;
	private String[] recordings;
	
	private void doFinal()
    {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
	
    @Override
    public void	onDestroy()
    {
    	super.onDestroy();
    	Log.d(LOG_TAG, "onDestroy()");
    	doFinal();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		recordings = Util.getRecordings();

		adapter = new CustomAdapter(getApplicationContext(), recordings);
		setListAdapter( adapter );
		 
		listView = getListView();
		listView.setTextFilterEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_manage_voice, menu);
		return true;
	}
	
	public void updateView()
	{
		listView.invalidateViews();
	}
	
	class CustomAdapter extends BaseAdapter implements OnClickListener 
	{
		private LayoutInflater inflater;
		private String[] recordings;
		
		private CustomAdapter(Context context, String[] recordings)
		{
			inflater = LayoutInflater.from(context);
			this.recordings = recordings;
		}

		@Override
		public int getCount() {
			if ( recordings == null )
			{
				return 0;
			}
			return recordings.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return recordings[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) 
			{
		       convertView = inflater.inflate(R.layout.recording_row, parent, false);
		       holder = new ViewHolder();
		       holder.label = (TextView) convertView.findViewById(R.id.label);
		       holder.playButton = (ImageButton) convertView.findViewById(R.id.play);
		       holder.deleteButton = (ImageButton) convertView.findViewById(R.id.delete);
		       holder.transcribeButton = (ImageButton) convertView.findViewById(R.id.transcribe);
		       //find and assign other views here...
		       convertView.setTag(holder);
			}
			else
			{
			    holder = (ViewHolder) convertView.getTag();
			}
			
			String idVal = (String)getItem(position);
			long l = 0;
			try
			{
				l = Long.parseLong(idVal);
				Date date = new Date(l);
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
				holder.label.setText(df.format(date));
			}
			catch (Exception e)
			{
				Log.e(LOG_TAG, "Invalid id found: " + idVal);
			}
			final Long idLong = (l > 0) ? new Long(l) : null;
			
			// assign values to all view elements..
			holder.playButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) 
				{
					boolean playing = false;
					if ( mPlayer != null )
					{
						if ( mPlayer.isPlaying() )
						{
							playing = true;
							stopPlaying();
						}
					}
					if ( !playing )
					{
						if ( idLong != null )
						{
							String fileName = Util.getFilePath(idLong);
							startPlaying(fileName);
						}
					}
				}
			});
			
			holder.deleteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) 
				{
					boolean success = Util.deleteDirectory(idLong);
					if ( !success )
					{
						Toast myToast = Toast.makeText(getApplicationContext(), "Error deleting memo", Toast.LENGTH_SHORT);
						myToast.show();
					}
					else
					{
						recordings = Util.getRecordings();
						updateView();
						Toast myToast = Toast.makeText(getApplicationContext(), "Memo deleted", Toast.LENGTH_SHORT);
						myToast.show();
					}
				}
			});
			
			holder.transcribeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) 
				{
					// goto TranscribeVoiceActivity and pass id of record
					Intent launchApp = new Intent(getApplicationContext(), TranscribeVoiceActivity.class);
	            	launchApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            	launchApp.putExtra("id", idLong);
	            	getApplicationContext().startActivity(launchApp);
				}
			});
			
			return convertView;
		}

		@Override
		public void onClick(View v) {
			String currentPos = v.getTag().toString();
			int position = Integer.parseInt(currentPos);
			Toast.makeText(getApplicationContext(), "Position: " + position, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void stopPlaying() 
    {
    	if ( mPlayer != null )
    	{
    		mPlayer.release();
    		mPlayer = null;
    	}
    }
	
    private void startPlaying(String mFileName) 
    {
    	Log.d(LOG_TAG, "Playing file: " + mFileName);
        mPlayer = new MediaPlayer();
        try 
        {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } 
        catch (IOException e) 
        {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }
	
	static class ViewHolder 
	{
        TextView label;
        ImageButton playButton;
        ImageButton deleteButton;
        ImageButton transcribeButton;
    }

}
