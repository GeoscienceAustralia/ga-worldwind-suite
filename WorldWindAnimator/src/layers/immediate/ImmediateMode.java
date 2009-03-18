package layers.immediate;

public class ImmediateMode
{
	public static boolean immediate = false;

	public static boolean isImmediate()
	{
		return immediate;
	}

	public static void setImmediate(boolean immediate)
	{
		ImmediateMode.immediate = immediate;
	}
}
