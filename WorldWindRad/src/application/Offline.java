package application;

public class Offline
{
	private static boolean offlineVersion = false;

	public static void main(String[] args)
	{
		offlineVersion = true;
		Executable.main(args);
	}

	public static boolean isOfflineVersion()
	{
		return offlineVersion;
	}
}
