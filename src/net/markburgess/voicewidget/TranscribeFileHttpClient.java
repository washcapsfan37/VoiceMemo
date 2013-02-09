package net.markburgess.voicewidget;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class TranscribeFileHttpClient 
{
	private final static String URL = "https://www.google.com/speech-api/v1/recognize?client=chromium&lang=en-US&maxresults=";
	private final static String LOG_TAG = TranscribeFileHttpClient.class.getSimpleName();
	
	private static String executeRequest(HttpRequestBase requestBase) throws Exception
	{
		String responseString = "" ;
		InputStream responseStream = null ;
		HttpClient client = new DefaultHttpClient();
		try
		{
			HttpResponse response = client.execute(requestBase);
			if (response != null)
			{
				HttpEntity responseEntity = response.getEntity() ;
				if (responseEntity != null)
				{
					responseStream = responseEntity.getContent() ;
					if (responseStream != null)
					{
						BufferedReader br = new BufferedReader (new InputStreamReader (responseStream)) ;
						String responseLine = br.readLine() ;
						String tempResponseString = "" ;
						while (responseLine != null)
						{
							tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator");
							responseLine = br.readLine();
						}
						br.close();
						if (tempResponseString.length() > 0)
						{
							responseString = tempResponseString ;
						}
					}
				}
			}
		} 
		catch (UnsupportedEncodingException e) 
		{
			throw e;
		} 
		catch (ClientProtocolException e) 
		{
			throw e;
		} 
		catch (IllegalStateException e) 
		{
			throw e;
		} 
		catch (IOException e) 
		{
			throw e;
		}
		finally
		{
			if (responseStream != null)
			{
				try 
				{
					responseStream.close() ;
				} 
				catch (IOException e) {}
			}
		}
		
		client.getConnectionManager().shutdown() ;
		return responseString ;
	}
	
	public String postFile(File file, int maxResults) throws Exception
	{
		HttpPost postRequest = new HttpPost (URL + maxResults) ;
		try
		{
			MultipartEntity multiPartEntity = new MultipartEntity () ;
			FileBody fileBody = new FileBody(file, "application/octect-stream");
			multiPartEntity.addPart("attachment", fileBody) ;
			postRequest.setEntity(multiPartEntity) ;
			postRequest.setHeader("Content-Type", "audio/x-flac; rate=16000;");
			return executeRequest (postRequest) ;
		}
		catch (Exception e)
		{
			Log.e(LOG_TAG, "Error posting file to url", e);
			return null;
		}
	}
}
