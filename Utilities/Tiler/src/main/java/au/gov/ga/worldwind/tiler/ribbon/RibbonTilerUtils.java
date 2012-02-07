package au.gov.ga.worldwind.tiler.ribbon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility methods for the ribbon tiler
 */
public class RibbonTilerUtils {

	public static void saveIntArrayToFile(int[] array, File file)
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos = new DataOutputStream(fos);
			for (int i : array)
			{
				dos.writeInt(i);
			}
			dos.close();
			fos.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static int[] loadIntArrayFromFile(File file)
	{
		if (!file.exists())
		{
			return null;
		}

		int size = (int) (file.length() / 4l);
		try
		{
			FileInputStream fis = new FileInputStream(file);
			DataInputStream dis = new DataInputStream(fis);
			int[] array = new int[size];
			for (int i = 0; i < size; i++)
			{
				array[i] = dis.readInt();
			}
			return array;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
}
