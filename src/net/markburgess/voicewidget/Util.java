package net.markburgess.voicewidget;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class Util 
{
	
	private final static String LOG_TAG = Util.class.getSimpleName();
	private final static String STORAGE_PATH = "voicememo";
	private final static String FILE_NAME = "voicememo.mp4";
	private final static String CONVERTED_FILE_NAME = "voicememo.flac";
	
	public static String getFilePath(long id)
	{
		return getFileStoragePath() + "/" + id + "/" + FILE_NAME;
	}
	
	public static String getStorageDir(long id)
	{
		return getFileStoragePath() + "/" + id;
	}
	
	public static String getFileStoragePath()
	{
		return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + STORAGE_PATH;
	}
	
	private static boolean createDirectory(String baseDirPath)
	{
		Log.i(LOG_TAG, "Creating directory: " + baseDirPath);
		boolean success = true;
		try
		{
			File dir = new File(baseDirPath);
			if ( dir.exists() )
			{
				Log.i(LOG_TAG, "Directory " + baseDirPath + " already exists.");
				return success;
			}
			dir.mkdirs();
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error creating directory: " + baseDirPath, e);
			success = false;
		}
		return success;
	}
	
	public static File getRecording(long id)
	{
		String dirPath = Util.getStorageDir(id) + "/" + FILE_NAME;
		try
		{
			File file = new File(dirPath);
			if ( file.exists() )
			{
				return file;
			}
			else
			{
				Log.i(LOG_TAG, "File " + id + " does not exist.");
				return null;
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error retrieving file: " + dirPath, e);
			return null;
		}
	}
	
	public static File getConvertedRecording(long id)
	{
		String dirPath = Util.getStorageDir(id) + "/" + CONVERTED_FILE_NAME;
		try
		{
			File file = new File(dirPath);
			if ( file.exists() )
			{
				return file;
			}
			else
			{
				Log.i(LOG_TAG, "File " + id + " does not exist.");
				return null;
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error retrieving file: " + dirPath, e);
			return null;
		}
	}
	
	public static File getConvertedRecordingPlaceholder(long id)
	{
		String dirPath = Util.getStorageDir(id) + "/" + CONVERTED_FILE_NAME;
		try
		{
			File file = new File(dirPath);
			return file;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error retrieving file: " + dirPath, e);
			return null;
		}
	}
	
	public static boolean deleteDirectory(long id)
	{
		String baseDirPath = Util.getStorageDir(id);
		Log.i(LOG_TAG, "Deleting directory: " + baseDirPath);
		boolean success = true;
		try
		{
			File dir = new File(baseDirPath);
			if ( dir.exists() )
			{
				FileUtils.deleteDirectory(dir);
			}
			else
			{
				Log.i(LOG_TAG, "Directory " + id + " does not exist.");
			}
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error deleting directory: " + baseDirPath, e);
			success = false;
		}
		return success;
	}
	
	public static boolean createStorageDir(long id)
	{
		String dirPath = Util.getStorageDir(id);
		boolean success = createDirectory(dirPath);
		return success;
	}
	
	public static String[] getRecordings()
	{
		String mFileDir = Util.getFileStoragePath();
		ArrayList<String> records = new ArrayList<String>();
		try
		{
			File dir = new File(mFileDir);
			String[] files = dir.list();
			for (String fileName : files)
			{
				try
				{
					File file = new File(Util.getFileStoragePath() + "/" + fileName);
					if (file.isDirectory())
					{
						long l = Long.parseLong(fileName);
						records.add(fileName);
					}
				}
				catch (Exception e){}  // not recording or invalid 
			}
		}
		catch (Exception e) {}
		Collections.sort(records, new RecordingComparator());
		return records.toArray(new String[records.size()]);
	}
	
	public static class RecordingComparator implements Comparator<String>
	{
		@Override
	    public int compare(String o1, String o2) {
			Long l1 = Long.valueOf(0);
			Long l2 = Long.valueOf(0);
			try
			{
				l1 = Long.parseLong(o1);
				l2 = Long.parseLong(o2);
			}
			catch (Exception e){}
	        return l2.compareTo(l1);
	    }
	}
	
	public static boolean isOnline(Context context)
	{
		Log.d(LOG_TAG, "isOnline");
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		if ( networkInfo != null )
		{
			String status = networkInfo.getState().toString();
			Log.d(LOG_TAG, "status:: " + status);
			if ( networkInfo != null && networkInfo.isConnected() )
			{
				return true;
			}
		}
		return false;
	}

}
