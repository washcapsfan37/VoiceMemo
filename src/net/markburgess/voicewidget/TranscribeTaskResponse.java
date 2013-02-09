package net.markburgess.voicewidget;

public class TranscribeTaskResponse
{
	public static final int SUCCESS = 1;
	public static final int FILE_NOT_FOUND = 2;
	public static final int SERVICE_ERROR = 3;
	public static final int NO_NETWORK_ERROR = 4;
	
	public int responseCode;
	public String message;
	
	public TranscribeTaskResponse(int responseCode, String message)
	{
		this.responseCode = responseCode;
		this.message = message;
	}
}