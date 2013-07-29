package au.gov.ga.worldwind.androidremote.client;

public class StrictModeHelper
{
	public static void disable()
	{
		try
		{
			android.os.StrictMode.ThreadPolicy policy =
					new android.os.StrictMode.ThreadPolicy.Builder().permitAll().build();
			android.os.StrictMode.setThreadPolicy(policy);
		}
		catch (Exception e)
		{
		}
	}
}
